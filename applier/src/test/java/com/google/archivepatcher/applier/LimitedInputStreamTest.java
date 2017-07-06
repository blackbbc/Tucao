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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;

/**
 * Tests for {@link PartiallyCompressingOutputStream}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class LimitedInputStreamTest {
  /**
   * An input stream that never ends and always does nothing.
   */
  private static class ForeverInputStream extends InputStream {
    @Override
    public int read() throws IOException {
      return 0;
    }

    @Override
    public int read(byte[] b) throws IOException {
      return b.length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return len;
    }
  }

  @SuppressWarnings("resource")
  @Test
  public void testRead_WithLimit0() throws IOException {
    LimitedInputStream stream = new LimitedInputStream(new ForeverInputStream(), 0);
    Assert.assertEquals(-1, stream.read());
  }

  @SuppressWarnings("resource")
  @Test
  public void testRead_WithLimit1() throws IOException {
    LimitedInputStream stream = new LimitedInputStream(new ForeverInputStream(), 1);
    Assert.assertEquals(0, stream.read());
    Assert.assertEquals(-1, stream.read());
  }

  @SuppressWarnings("resource")
  @Test
  public void testRead_WithLimit100() throws IOException {
    LimitedInputStream stream = new LimitedInputStream(new ForeverInputStream(), 100);
    Assert.assertEquals(100, stream.read(new byte[1000]));
    Assert.assertEquals(-1, stream.read());
  }

  @SuppressWarnings("resource")
  @Test(expected = IllegalArgumentException.class)
  public void testSetLimit_BadValue() {
    new LimitedInputStream(new ForeverInputStream(), -1);
  }
}
