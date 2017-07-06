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

package com.google.archivepatcher.shared;

import com.google.archivepatcher.shared.PartiallyUncompressingPipe.Mode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests for {@link PartiallyUncompressingPipe}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class PartiallyUncompressingPipeTest {
  private ByteArrayOutputStream outBuffer;
  private PartiallyUncompressingPipe stream;

  @Before
  public void setup() {
    outBuffer = new ByteArrayOutputStream();
    stream = new PartiallyUncompressingPipe(outBuffer, 32768);
  }

  @Test
  public void testWriteAll_Uncompressed() throws IOException {
    byte[] expectedBytes = new byte[] {1, 2, 3, 4, 5};
    stream.pipe(new ByteArrayInputStream(expectedBytes), Mode.COPY);
    Assert.assertArrayEquals(expectedBytes, outBuffer.toByteArray());
  }

  @Test
  public void testWriteAll_Compressed_NoWrapTrue() throws IOException {
    UnitTestZipEntry entry = UnitTestZipArchive.makeUnitTestZipEntry("/foo", 7, "frobozz", null);
    stream.pipe(
        new ByteArrayInputStream(entry.getCompressedBinaryContent()), Mode.UNCOMPRESS_NOWRAP);
    Assert.assertArrayEquals(entry.getUncompressedBinaryContent(), outBuffer.toByteArray());
  }

  @Test
  public void testWriteAll_Compressed_NoWrapFalse() throws IOException {
    UnitTestZipEntry entry = UnitTestZipArchive.makeUnitTestZipEntry("/foo", 6, "frobozz", null);

    // Make a compressor with nowrap set to *false* (unusual) and pump the uncompressed entry
    // content through it.
    DeflateCompressor compressor = new DeflateCompressor();
    compressor.setNowrap(false);
    ByteArrayOutputStream compressBuffer = new ByteArrayOutputStream();
    compressor.compress(
        new ByteArrayInputStream(entry.getUncompressedBinaryContent()), compressBuffer);

    // Now use the compressed data as input to the PartiallyUncompressingPipe.
    stream.pipe(new ByteArrayInputStream(compressBuffer.toByteArray()), Mode.UNCOMPRESS_WRAPPED);
    Assert.assertArrayEquals(entry.getUncompressedBinaryContent(), outBuffer.toByteArray());
  }

  @Test
  public void testWriteAll_Multiple() throws IOException {
    // A series of uncompressed, compressed, uncompressed, compressed, uncompressed bytes.
    UnitTestZipEntry entryA =
        UnitTestZipArchive.makeUnitTestZipEntry("/bar", 3, "dragon lance", null);
    UnitTestZipEntry entryB =
        UnitTestZipArchive.makeUnitTestZipEntry("/baz", 8, "kender & hoopak", null);
    ByteArrayOutputStream expected = new ByteArrayOutputStream();

    // Write everything
    byte[] expectedBytes1 = new byte[] {1, 2, 3, 4, 5};
    expected.write(expectedBytes1);
    stream.pipe(new ByteArrayInputStream(expectedBytes1), Mode.COPY);

    stream.pipe(
        new ByteArrayInputStream(entryA.getCompressedBinaryContent()), Mode.UNCOMPRESS_NOWRAP);
    expected.write(entryA.getUncompressedBinaryContent());

    byte[] expectedBytes3 = new byte[] {6, 7, 8, 9, 0};
    stream.pipe(new ByteArrayInputStream(expectedBytes3), Mode.COPY);
    expected.write(expectedBytes3);

    stream.pipe(
        new ByteArrayInputStream(entryB.getCompressedBinaryContent()), Mode.UNCOMPRESS_NOWRAP);
    expected.write(entryB.getUncompressedBinaryContent());

    byte[] expectedBytes5 = new byte[] {127, 127, 127, 127, 127, 127};
    stream.pipe(new ByteArrayInputStream(expectedBytes5), Mode.COPY);
    expected.write(expectedBytes5);

    Assert.assertArrayEquals(expected.toByteArray(), outBuffer.toByteArray());
  }
}
