// Copyright 2015 Google Inc. All rights reserved.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Tests for {@link DeflateCompressor}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class DeflateCompressorTest {
  /**
   * Test data for compression. Uses the {@link DefaultDeflateCompatibilityWindow}'s corpus because
   * it is already set up to produce different outputs for each compression level.
   */
  private final static byte[] CONTENT = new DefaultDeflateCompatibilityWindow().getCorpus();

  /**
   * Helper class for storing the compressed and uncompressed form of something together.
   */
  private static class Content {
    public byte[] compressed;
    public byte[] uncompressed;
  }

  private DeflateCompressor compressor;
  private ByteArrayInputStream rawContentIn;
  private ByteArrayOutputStream compressedContentOut;

  /**
   * Uncompress some content with Java's built-in {@link Inflater} as a sanity check against our own
   * code.
   * @param nowrap value to set for the nowrap parameter for the {@link Inflater}
   * @param compressedData
   * @return the uncompressed data as a byte array
   * @throws IOException if anything goes wrong
   */
  private byte[] uncompressWithJavaInflater(boolean nowrap, byte[] compressedData)
      throws IOException {
    Inflater inflater = new Inflater(nowrap);
    InflaterInputStream inflaterIn =
        new InflaterInputStream(new ByteArrayInputStream(compressedData), inflater);
    byte[] buffer = new byte[32768];
    ByteArrayOutputStream uncompressedOut = new ByteArrayOutputStream();
    int numRead = 0;
    while ((numRead = inflaterIn.read(buffer)) >= 0) {
      uncompressedOut.write(buffer, 0, numRead);
    }
    return uncompressedOut.toByteArray();
  }

  @Before
  public void setUp() {
    compressor = new DeflateCompressor();
    rawContentIn = new ByteArrayInputStream(CONTENT);
    compressedContentOut = new ByteArrayOutputStream();
  }

  @Test
  public void testCompress() throws IOException {
    compressor.compress(rawContentIn, compressedContentOut);
    Assert.assertTrue(compressedContentOut.size() > 0);
    Assert.assertTrue(compressedContentOut.size() < CONTENT.length);
    byte[] uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);
  }

  @Test
  public void testCorrectDefaults() {
    // Sanity check to ensure that defaults are as we want them to be. Arguably crufty but nobody
    // should change these without some thought, particularly the wrapping choice should not be
    // changed in the compressor without also changing it in the *un*compressor.
    Assert.assertTrue(compressor.isNowrap());
    Assert.assertEquals(Deflater.DEFAULT_COMPRESSION, compressor.getCompressionLevel());
    Assert.assertEquals(Deflater.DEFAULT_STRATEGY, compressor.getStrategy());
  }

  @Test
  public void testNowrap() throws IOException {
    // Start with nowrap 'on' (should be the default)
    Assert.assertTrue(compressor.isNowrap());
    compressor.compress(rawContentIn, compressedContentOut);
    byte[] compressedWithNowrapOn = compressedContentOut.toByteArray();
    byte[] uncompressedWithNowrapOn =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedWithNowrapOn);
    Assert.assertArrayEquals(CONTENT, uncompressedWithNowrapOn);

    // Now twiddle nowrap to 'off' and do it again.
    compressor.setNowrap(false);
    Assert.assertFalse(compressor.isNowrap());
    rawContentIn = new ByteArrayInputStream(CONTENT);
    compressedContentOut = new ByteArrayOutputStream();
    compressor.compress(rawContentIn, compressedContentOut);
    byte[] compressedWithNowrapOff = compressedContentOut.toByteArray();
    Assert.assertFalse(compressedWithNowrapOn.length == compressedWithNowrapOff.length);
    byte[] uncompressedWithNowrapOff =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedWithNowrapOff);
    Assert.assertArrayEquals(CONTENT, uncompressedWithNowrapOff);
  }

  @Test
  public void testStrategy() throws IOException {
    // Compression level has to be > 2 to get different output out of strategies 0 and 1.
    compressor.setCompressionLevel(9);

    // Compress with all valid strategies to ensure they all work.
    Content[] content = new Content[3];
    for (int strategy = 0; strategy <= 2; strategy++) {
      rawContentIn = new ByteArrayInputStream(CONTENT);
      compressedContentOut = new ByteArrayOutputStream();
      compressor.setStrategy(strategy);
      Assert.assertEquals(strategy, compressor.getStrategy());
      compressor.compress(rawContentIn, compressedContentOut);
      content[strategy] = new Content();
      content[strategy].compressed = compressedContentOut.toByteArray();
      content[strategy].uncompressed =
          uncompressWithJavaInflater(compressor.isNowrap(), content[strategy].compressed);
      Assert.assertArrayEquals(CONTENT, content[strategy].uncompressed);
    }

    // No two outputs should be the same.
    for (int outer = 0; outer < content.length; outer++) {
      for (int inner = 0; inner < content.length; inner++) {
        if (inner != outer) {
          Assert.assertFalse(
              "strategy " + outer + " == " + inner,
              Arrays.equals(content[outer].compressed, content[inner].compressed));
        }
      }
    }
  }

  @Test
  public void testCompressionLevel() throws IOException {
    // Compress at all levels to ensure they all work.
    Content[] content = new Content[10]; // Note that level 0 (store) is not used.
    for (int level = 1; level <= 9; level++) {
      rawContentIn = new ByteArrayInputStream(CONTENT);
      compressedContentOut = new ByteArrayOutputStream();
      compressor.setCompressionLevel(level);
      Assert.assertEquals(level, compressor.getCompressionLevel());
      compressor.compress(rawContentIn, compressedContentOut);
      content[level] = new Content();
      content[level].compressed = compressedContentOut.toByteArray();
      content[level].uncompressed =
          uncompressWithJavaInflater(compressor.isNowrap(), content[level].compressed);
      Assert.assertArrayEquals(CONTENT, content[level].uncompressed);
    }

    // No two outputs should be the same. Again note that level 0 (store) is unused, so start at 1.
    for (int outer = 1; outer < content.length; outer++) {
      for (int inner = 1; inner < content.length; inner++) {
        if (inner != outer) {
          Assert.assertFalse(
              "strategy " + outer + " == " + inner,
              Arrays.equals(content[outer].compressed, content[inner].compressed));
        }
      }
    }
  }

  @Test
  public void testSetInputBufferSize() throws IOException {
    Assert.assertNotEquals(17, compressor.getInputBufferSize()); // Ensure test is valid
    compressor.setInputBufferSize(17); // Arbitrary non-default value
    Assert.assertEquals(17, compressor.getInputBufferSize());
    compressor.compress(rawContentIn, compressedContentOut);
    byte[] uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);
  }

  @Test
  public void testSetOutputBufferSize() throws IOException {
    Assert.assertNotEquals(17, compressor.getOutputBufferSize()); // Ensure test is valid
    compressor.setOutputBufferSize(17); // Arbitrary non-default value
    Assert.assertEquals(17, compressor.getOutputBufferSize());
    compressor.compress(rawContentIn, compressedContentOut);
    byte[] uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);
  }

  @Test
  public void testCreateOrResetDeflater_Uncached() {
    compressor.setCaching(false);
    Deflater deflater1 = compressor.createOrResetDeflater();
    Deflater deflater2 = compressor.createOrResetDeflater();
    Assert.assertNotSame(deflater1, deflater2);
  }

  @Test
  public void testCreateOrResetDeflater_Cached() {
    compressor.setCaching(true);
    Deflater deflater1 = compressor.createOrResetDeflater();
    Deflater deflater2 = compressor.createOrResetDeflater();
    Assert.assertSame(deflater1, deflater2);
  }

  @Test
  public void testRelease() {
    compressor.setCaching(true);
    Deflater deflater1 = compressor.createOrResetDeflater();
    compressor.release();
    Deflater deflater2 = compressor.createOrResetDeflater();
    Assert.assertNotSame(deflater1, deflater2);
  }

  @Test
  public void testReusability() throws IOException {
    // Checks that the compressor produces correct output when cached, i.e. that it is being
    // properly reset between runs.
    compressor.setCaching(true);
    compressor.compress(rawContentIn, compressedContentOut);
    byte[] uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);

    // Caching is on, try to reuse it without any changes.
    rawContentIn = new ByteArrayInputStream(CONTENT);
    compressedContentOut = new ByteArrayOutputStream();
    compressor.compress(rawContentIn, compressedContentOut);
    uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);

    // Caching is still on, tweak the compression level and try again.
    Assert.assertNotEquals(7, compressor.getCompressionLevel()); // Ensure test is valid
    compressor.setCompressionLevel(7);
    rawContentIn = new ByteArrayInputStream(CONTENT);
    compressedContentOut = new ByteArrayOutputStream();
    compressor.compress(rawContentIn, compressedContentOut);
    uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);

    // Caching is still on, tweak the strategy and try again.
    Assert.assertNotEquals(1, compressor.getStrategy()); // Ensure test is valid
    compressor.setStrategy(Deflater.FILTERED);
    rawContentIn = new ByteArrayInputStream(CONTENT);
    compressedContentOut = new ByteArrayOutputStream();
    compressor.compress(rawContentIn, compressedContentOut);
    uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);

    // Caching is still on, reverse the wrapping style and try again. Changing the wrapping style
    // invalidates the cached compressor because the wrapping style cannot be changed, this is a
    // special code path we need to exercise.
    compressor.setNowrap(!compressor.isNowrap());
    rawContentIn = new ByteArrayInputStream(CONTENT);
    compressedContentOut = new ByteArrayOutputStream();
    compressor.compress(rawContentIn, compressedContentOut);
    uncompressed =
        uncompressWithJavaInflater(compressor.isNowrap(), compressedContentOut.toByteArray());
    Assert.assertArrayEquals(CONTENT, uncompressed);
  }
}
