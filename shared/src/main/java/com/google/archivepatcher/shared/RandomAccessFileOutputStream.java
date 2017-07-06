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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * An {@link OutputStream} backed by a file that will be written serially. Allows pre-allocating
 * the space for a stream as a file and then writing to that file as a stream. Call {@link #flush()}
 * to force the data to be written to the backing storage.
 */
public class RandomAccessFileOutputStream extends OutputStream {
  /**
   * The backing {@link RandomAccessFile}.
   */
  private final RandomAccessFile raf;

  /**
   * Constructs a new instance that will immediately open the specified file for writing and set
   * the length to the specified value.
   * @param outputFile the file to wrap
   * @param expectedSize if greater than or equal to zero, the size to set the file to immediately;
   * otherwise, the file size is not set
   * @throws IOException if unable to open the file for writing or set the size
   */
  public RandomAccessFileOutputStream(File outputFile, long expectedSize) throws IOException {
    this.raf = getRandomAccessFile(outputFile);
    if (expectedSize >= 0) {
      raf.setLength(expectedSize);
      if (raf.length() != expectedSize) {
        throw new IOException("Unable to set the file size");
      }
    }
  }

  /**
   * Given a {@link File}, get a writeable {@link RandomAccessFile} reference for it.
   * @param file the file
   * @return as described
   * @throws IOException if unable to open the file
   */
  protected RandomAccessFile getRandomAccessFile(File file) throws IOException {
    return new RandomAccessFile(file, "rw");
  }

  @Override
  public void write(int b) throws IOException {
    raf.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    raf.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    raf.getChannel().force(true);
  }

  @Override
  public void close() throws IOException {
    flush();
    raf.close();
  }
}
