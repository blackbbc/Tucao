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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Tests for {@link RandomAccessFileInputStream}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class RandomAccessFileInputStreamTest {
  /**
   * The object under test.
   */
  private RandomAccessFileInputStream stream = null;

  /**
   * Test data written to the file.
   */
  private byte[] testData = null;

  /**
   * The temp file.
   */
  private File tempFile = null;

  @Before
  public void setup() throws IOException {
    testData = new byte[128];
    for (int x = 0; x < 128; x++) {
      testData[x] = (byte) x;
    }
    tempFile = File.createTempFile("ra-fist", "tmp");
    tempFile.deleteOnExit();
    try {
      FileOutputStream out = new FileOutputStream(tempFile);
      out.write(testData);
      out.flush();
      out.close();
    } catch (IOException e) {
      try {
        tempFile.delete();
      } catch (Exception ignoreD) {
        // Nothing
      }
      throw new RuntimeException(e);
    }
    stream = new RandomAccessFileInputStream(tempFile);
  }

  @After
  public void tearDown() {
    try {
      stream.close();
    } catch (Exception ignored) {
      // Nothing to do
    }
    try {
      tempFile.delete();
    } catch (Exception ignored) {
      // Nothing to do
    }
  }

  @Test
  public void testRead_OneByte() throws IOException {
    for (int x = 0; x < testData.length; x++) {
      Assert.assertEquals(x, stream.read());
    }
    Assert.assertEquals(-1, stream.read());
  }

  @Test
  public void testRead_WithBuffer() throws IOException {
    int bytesLeft = testData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[10];
    while (bytesLeft > 0) {
      int numRead = stream.read(buffer);
      if (numRead > 0) {
        bytesLeft -= numRead;
        out.write(buffer, 0, numRead);
      }
    }
    Assert.assertEquals(-1, stream.read(buffer, 0, 1));
    Assert.assertArrayEquals(testData, out.toByteArray());
  }

  @Test
  public void testRead_WithBuffer_NegativeLength() throws IOException {
    Assert.assertEquals(0, stream.read(new byte[] {}, 0, -1));
  }

  @Test
  public void testRead_WithPartialBuffer() throws IOException {
    int bytesLeft = testData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[10];
    while (bytesLeft > 0) {
      int numRead = stream.read(buffer, 0, 2); // At most 2 bytes of the buffer can be used
      if (numRead > 0) {
        bytesLeft -= numRead;
        out.write(buffer, 0, numRead);
      }
    }
    Assert.assertEquals(-1, stream.read());
    Assert.assertArrayEquals(testData, out.toByteArray());
  }

  @Test
  public void testMarkSupported() {
    Assert.assertTrue(stream.markSupported());
  }

  @Test
  public void testMarkAndReset_WithOffsetFile() throws IOException {
    // Reset the stream, this time one byte in to exercise non-zero offset values
    stream.close();
    stream = new RandomAccessFileInputStream(tempFile, 1, testData.length - 2);
    // Set a mark after the first byte, which should be 1. Read a second byte, which should be 2.
    Assert.assertEquals(1, stream.read());
    stream.mark(1337 /* any value here, it is ignored */);
    Assert.assertEquals(2, stream.read());
    // Reset the stream, it should be back to 1 now.
    stream.reset();
    Assert.assertEquals(2, stream.read());
  }

  @Test
  public void testSkip() throws IOException {
    // Skip values <= 0 should always produce 0 and not actually skip anything.
    Assert.assertEquals(0, stream.skip(-1));
    Assert.assertEquals(0, stream.skip(0));
    // Skip the first 5 bytes and read the 6th, which should have the value 5.
    Assert.assertEquals(5, stream.skip(5));
    Assert.assertEquals(5, stream.read());
    // 6 bytes have been read, so the max skip is testDataLength - 6. Ensure this is true.
    Assert.assertEquals(testData.length - 5 - 1, stream.skip(testData.length));
    // At the end of the file, skip should always return 0.
    Assert.assertEquals(0, stream.skip(17));
  }

  @Test
  public void testAvailable() throws IOException {
    // Available always knows the answer precisely unless the file length exceeds Integer.MAX_VALUE
    Assert.assertEquals(testData.length, stream.available());
    stream.read(new byte[17]);
    Assert.assertEquals(testData.length - 17, stream.available());
    stream.read(new byte[testData.length]);
    Assert.assertEquals(0, stream.available());
    stream.read();
    Assert.assertEquals(0, stream.available());
  }

  @Test
  public void testSetRange() throws IOException {
    // Mess with the stream range multiple times
    stream.setRange(1, 3);
    Assert.assertEquals(1, stream.read());
    Assert.assertEquals(2, stream.read());
    Assert.assertEquals(3, stream.read());
    Assert.assertEquals(-1, stream.read());
    stream.setRange(99, 2);
    Assert.assertEquals(99, stream.read());
    Assert.assertEquals(100, stream.read());
    Assert.assertEquals(-1, stream.read());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetRange_TooLong() throws IOException {
    stream.setRange(0, testData.length + 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetRange_NegativeOffset() throws IOException {
    stream.setRange(-1, testData.length);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetRange_NegativeLength() throws IOException {
    stream.setRange(0, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetRange_LongOverflow() throws IOException {
    stream.setRange(Long.MAX_VALUE, 1); // Oh dear.
  }

  @Test(expected = IOException.class)
  public void testReset_NoMarkSet() throws IOException {
    stream.reset();
  }

  @Test
  public void testMark_IOExceptionInRaf() throws IOException {
    stream =
        new RandomAccessFileInputStream(tempFile, 0, testData.length) {
          @Override
          protected RandomAccessFile getRandomAccessFile(File file) throws IOException {
            return new RandomAccessFile(file, "r") {
              @Override
              public long getFilePointer() throws IOException {
                throw new IOException("Blah314159");
              }
            };
          }
        };
    try {
      stream.mark(0);
      Assert.fail("Executed code that should have failed.");
    } catch (Exception e) {
      Assert.assertEquals("Blah314159", e.getCause().getMessage());
    }
  }

  @Test
  public void testClose() throws IOException {
    stream.close();
    try {
      stream.read();
      Assert.fail("read after close");
    } catch (IOException expected) {
      // Good.
    }
  }

  @Test
  public void testLength() {
    Assert.assertEquals(testData.length, stream.length());
  }

  @Test
  public void testConstructorWithSpecificLength() throws IOException {
    stream = new RandomAccessFileInputStream(tempFile, 5, 2);
    Assert.assertEquals(5, stream.read());
    Assert.assertEquals(6, stream.read());
    Assert.assertEquals(-1, stream.read());
  }

  @Test
  public void testGetPosition() throws IOException {
    stream = new RandomAccessFileInputStream(tempFile, 5, 2);
    Assert.assertEquals(5, stream.getPosition());
    stream.read();
    Assert.assertEquals(6, stream.getPosition());
    stream.setRange(0, 1);
    Assert.assertEquals(0, stream.getPosition());
  }
}
