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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Tests for {@link RandomAccessFileOutputStream}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class RandomAccessFileOutputStreamTest {
  /**
   * The object under test.
   */
  private RandomAccessFileOutputStream stream = null;

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
    tempFile = File.createTempFile("ra-fost", "tmp");
    tempFile.deleteOnExit();
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
  public void testCreateAndSize() throws IOException {
    stream = new RandomAccessFileOutputStream(tempFile, 11L);
    Assert.assertEquals(11, tempFile.length());
  }

  @Test(expected = IOException.class)
  public void testCreateAndFailToSize() throws IOException {
    stream =
        new RandomAccessFileOutputStream(tempFile, 11L) {
          @Override
          protected RandomAccessFile getRandomAccessFile(File file) throws IOException {
            return new RandomAccessFile(file, "rw") {
              @Override
              public void setLength(long newLength) throws IOException {
                // Do nothing, to trigger failure case in the constructor.
              }
            };
          }
        };
  }

  @Test
  public void testWrite() throws IOException {
    stream = new RandomAccessFileOutputStream(tempFile, 1L);
    stream.write(7);
    stream.flush();
    stream.close();
    FileInputStream in = null;
    try {
      in = new FileInputStream(tempFile);
      Assert.assertEquals(7, in.read());
    } finally {
      try {
        in.close();
      } catch (Exception ignored) {
        // Nothing
      }
    }
  }

  @Test
  public void testWriteArray() throws IOException {
    stream = new RandomAccessFileOutputStream(tempFile, 1L);
    stream.write(testData, 0, testData.length);
    stream.flush();
    stream.close();
    FileInputStream in = null;
    DataInputStream dataIn = null;
    try {
      in = new FileInputStream(tempFile);
      dataIn = new DataInputStream(in);
      byte[] actual = new byte[testData.length];
      dataIn.readFully(actual);
      Assert.assertArrayEquals(testData, actual);
    } finally {
      if (dataIn != null) {
        try {
          dataIn.close();
        } catch (Exception ignored) {
          // Nothing
        }
      }
      if (in != null) {
        try {
          in.close();
        } catch (Exception ignored) {
          // Nothing
        }
      }
    }
  }
}
