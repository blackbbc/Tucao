// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.archivepatcher.applier;

import com.google.archivepatcher.shared.JreDeflateParameters;
import com.google.archivepatcher.shared.TypedRange;
import com.google.archivepatcher.shared.UnitTestZipArchive;
import com.google.archivepatcher.shared.UnitTestZipEntry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for {@link PartiallyCompressingOutputStream}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class PartiallyCompressingOutputStreamTest {
  private ByteArrayOutputStream outBuffer;
  private PartiallyCompressingOutputStream stream;

  // The preamble comes before any compressed bytes in the stream
  private static byte[] PREAMBLE_BYTES = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

  // First range for compression
  private static JreDeflateParameters PARAMS1 = JreDeflateParameters.of(1, 0, true);
  private static UnitTestZipEntry ENTRY1 =
      UnitTestZipArchive.makeUnitTestZipEntry(
          "/foo", PARAMS1.level, PARAMS1.nowrap, "foo-level1", null);
  private static long OFFSET1 = PREAMBLE_BYTES.length;
  private static long LENGTH1 = ENTRY1.getUncompressedBinaryContent().length;
  private static TypedRange<JreDeflateParameters> COMPRESS_RANGE_1 =
      new TypedRange<JreDeflateParameters>(OFFSET1, LENGTH1, PARAMS1);

  private static byte[] GAP1_BYTES = new byte[] {37};

  // Second range for compression, with a gap in between. Note this changes nowrap and level
  private static JreDeflateParameters PARAMS2 = JreDeflateParameters.of(6, 0, false);
  private static UnitTestZipEntry ENTRY2 =
      UnitTestZipArchive.makeUnitTestZipEntry(
          "/bar", PARAMS2.level, PARAMS2.nowrap, "bar-level6", null);
  private static long OFFSET2 = OFFSET1 + LENGTH1 + GAP1_BYTES.length;
  private static long LENGTH2 = ENTRY2.getUncompressedBinaryContent().length;
  private static TypedRange<JreDeflateParameters> COMPRESS_RANGE_2 =
      new TypedRange<JreDeflateParameters>(OFFSET2, LENGTH2, PARAMS2);

  private byte[] fuse(byte[]... arrays) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    for (byte[] array : arrays) {
      buffer.write(array);
    }
    return buffer.toByteArray();
  }

  @Before
  public void setup() {
    outBuffer = new ByteArrayOutputStream();
  }

  @Test
  public void testWrite_Nothing() throws IOException {
    // Test the case where there are no compression ranges at all and nothing is written.
    stream =
        new PartiallyCompressingOutputStream(
            Collections.<TypedRange<JreDeflateParameters>>emptyList(), outBuffer, 32768);
    byte[] input = new byte[] {};
    stream.write(input);
    stream.flush();
    stream.close();
    Assert.assertArrayEquals(input, outBuffer.toByteArray());
  }

  @Test
  public void testWrite_NoneCompressed() throws IOException {
    // Test the case where there are no compression ranges at all.
    stream =
        new PartiallyCompressingOutputStream(
            Collections.<TypedRange<JreDeflateParameters>>emptyList(), outBuffer, 32768);
    byte[] input = new byte[] {1, 77, 66, 44, 22, 11};
    byte[] expected = input.clone();
    stream.write(input);
    stream.flush();
    Assert.assertArrayEquals(expected, outBuffer.toByteArray());
  }

  @Test
  public void testWrite_AllCompressed() throws IOException {
    // Test the case where a single compression range covers the entire input
    TypedRange<JreDeflateParameters> range =
        new TypedRange<JreDeflateParameters>(
            0, ENTRY1.getUncompressedBinaryContent().length, PARAMS1);
    stream =
        new PartiallyCompressingOutputStream(Collections.singletonList(range), outBuffer, 32768);
    stream.write(ENTRY1.getUncompressedBinaryContent());
    stream.flush();
    Assert.assertArrayEquals(ENTRY1.getCompressedBinaryContent(), outBuffer.toByteArray());
  }

  @Test
  public void testWrite_GapAndCompression() throws IOException {
    // Write uncompressed data followed by compressed data
    stream =
        new PartiallyCompressingOutputStream(
            Collections.singletonList(COMPRESS_RANGE_1), outBuffer, 32768);
    byte[] input = fuse(PREAMBLE_BYTES, ENTRY1.getUncompressedBinaryContent());
    byte[] expected = fuse(PREAMBLE_BYTES, ENTRY1.getCompressedBinaryContent());
    stream.write(input);
    stream.flush();
    Assert.assertArrayEquals(expected, outBuffer.toByteArray());
  }

  @Test
  public void testWrite_GapAndCompressionAndGap() throws IOException {
    // Write uncompressed data followed by compressed data and another bit of uncompressed data
    stream =
        new PartiallyCompressingOutputStream(
            Collections.singletonList(COMPRESS_RANGE_1), outBuffer, 32768);
    byte[] input = fuse(PREAMBLE_BYTES, ENTRY1.getUncompressedBinaryContent(), GAP1_BYTES);
    byte[] expected = fuse(PREAMBLE_BYTES, ENTRY1.getCompressedBinaryContent(), GAP1_BYTES);
    stream.write(input);
    stream.flush();
    Assert.assertArrayEquals(expected, outBuffer.toByteArray());
  }

  @Test
  public void testWrite_MixedSequence_Thrash() throws IOException {
    // Write uncompressed data, compressed data, uncompressed data, compressed data (new params)
    // Thrash by writing one byte at a time to pound on edge-casey code
    stream =
        new PartiallyCompressingOutputStream(
            Arrays.asList(COMPRESS_RANGE_1, COMPRESS_RANGE_2), outBuffer, 32768);
    byte[] input =
        fuse(
            PREAMBLE_BYTES,
            ENTRY1.getUncompressedBinaryContent(),
            GAP1_BYTES,
            ENTRY2.getUncompressedBinaryContent());
    byte[] expected =
        fuse(
            PREAMBLE_BYTES,
            ENTRY1.getCompressedBinaryContent(),
            GAP1_BYTES,
            ENTRY2.getCompressedBinaryContent());
    for (int x = 0; x < input.length; x++) {
      stream.write(input[x] & 0xff);
      stream.flush();
    }
    stream.close();
    Assert.assertArrayEquals(expected, outBuffer.toByteArray());
  }
}
