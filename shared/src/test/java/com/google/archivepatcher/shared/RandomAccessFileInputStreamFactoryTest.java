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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Tests for {@link RandomAccessFileInputStreamFactory}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class RandomAccessFileInputStreamFactoryTest {
  /**
   * The object under test.
   */
  private RandomAccessFileInputStreamFactory factory = null;

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
    FileOutputStream out = new FileOutputStream(tempFile);
    out.write(testData);
    out.flush();
    out.close();
    tempFile.deleteOnExit();
    factory = new RandomAccessFileInputStreamFactory(tempFile, 0, testData.length);
  }

  @After
  public void tearDown() {
    try {
      tempFile.delete();
    } catch (Exception ignored) {
      // Nothing to do
    }
  }

  @Test
  public void testNewStream_MakesIdenticalStreams() throws IOException {
    RandomAccessFileInputStream rafis1 = factory.newStream();
    RandomAccessFileInputStream rafis2 = factory.newStream();
    try {
      Assert.assertNotSame(rafis1, rafis2);
      for (int x = 0; x < testData.length; x++) {
        Assert.assertEquals(x, rafis1.read());
        Assert.assertEquals(x, rafis2.read());
      }
      Assert.assertEquals(-1, rafis1.read());
      Assert.assertEquals(-1, rafis2.read());
    } finally {
      try {
        rafis1.close();
      } catch (Exception ignored) {
        // Nothing
      }
      try {
        rafis2.close();
      } catch (Exception ignored) {
        // Nothing
      }
    }
  }
}
