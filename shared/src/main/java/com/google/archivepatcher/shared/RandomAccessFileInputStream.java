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
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * An {@link InputStream} backed by a file that is assumed to be unchanging, such that it is
 * suitable for random read access. This allows efficient and trivial implementation of the
 * {@link #mark(int)} and {@link #reset()} functions using file operations.
 * <p>
 * This class deliberately breaks from the {@link InputStream} contract because it is intended for
 * use cases where some of that behavior doesn't make sense. Specifically:
 * <ul>
 *  <li>There is no read limit, and the value passed to {@link #mark(int)} is ignored.</li>
 *  <li>The {@link #reset()} method will only throw an exception if the stream is closed or if
 *      {@link #mark(int)} has never been called. It does <em>not</em> have the concept of an
 *      error from an invalidated read limit, because there is no read limit.</li>
 * </ul>
 */
public class RandomAccessFileInputStream extends InputStream {
  /**
   * The backing {@link RandomAccessFile}.
   */
  private final RandomAccessFile raf;

  /**
   * The current mark in the file, if set; otherwise -1.
   */
  private long mark = -1;

  /**
   * The offset at which the reading range starts.
   */
  private long rangeOffset;

  /**
   * The number of bytes in the reading range.
   */
  private long rangeLength;

  /**
   * The length of the file at the moment it was opened.
   */
  private final long fileLength;

  /**
   * Constructs a new stream for the given file, which will be opened in read-only mode for random
   * access cross the entire file. Equivalent to calling
   * {@link #RandomAccessFileInputStream(File, long, long)} with 0 and {@link File#length()} as the
   * range parameters.
   * @param file the file to read
   * @throws IOException if unable to open the file for read
   */
  public RandomAccessFileInputStream(File file) throws IOException {
    this(file, 0, file.length());
  }

  /**
   * Constructs a new stream for the given file, which will be opened in read-only mode for random
   * access within a specific range.
   * @param file the file to read
   * @param rangeOffset the offset at which the valid range starts
   * @param rangeLength the number of bytes in the range
   * @throws IOException if unable to open the file for read
   */
  public RandomAccessFileInputStream(File file, long rangeOffset, long rangeLength)
      throws IOException {
    raf = getRandomAccessFile(file);
    fileLength = file.length();
    setRange(rangeOffset, rangeLength);
  }

  /**
   * Given a {@link File}, get a read-only {@link RandomAccessFile} reference for it.
   * @param file the file
   * @return as described
   * @throws IOException if unable to open the file
   */
  protected RandomAccessFile getRandomAccessFile(File file) throws IOException {
    return new RandomAccessFile(file, "r");
  }

  /**
   * Sets the range to the specified values and seeks to the beginning of that range immediately.
   * Any previously-existing mark is discarded. Also calls {@link #reset()}.
   * @param rangeOffset the offset at which the valid range starts, must be a non-negative value
   * @param rangeLength the number of bytes in the range, must be a non-negative value
   * @throws IOException if anything goes wrong
   */
  public void setRange(long rangeOffset, long rangeLength) throws IOException {
    if (rangeOffset < 0) {
      throw new IllegalArgumentException("rangeOffset must be >= 0");
    }
    if (rangeLength < 0) {
      throw new IllegalArgumentException("rangeLength must be >= 0");
    }
    if (rangeOffset + rangeLength > fileLength) {
      throw new IllegalArgumentException("Read range exceeds file length");
    }
    if (rangeOffset + rangeLength < 0) {
      throw new IllegalArgumentException("Insane input size not supported");
    }
    this.rangeOffset = rangeOffset;
    this.rangeLength = rangeLength;
    mark = rangeOffset;
    reset();
    mark = -1;
  }

  @Override
  public int available() throws IOException {
    long rangeRelativePosition = raf.getFilePointer() - rangeOffset;
    long result = rangeLength - rangeRelativePosition;
    if (result > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    return (int) result;
  }

  /**
   * Returns the current position in the stream.
   * @return as described
   * @throws IOException if something goes wrong
   */
  public long getPosition() throws IOException {
    return raf.getFilePointer();
  }

  @Override
  public void close() throws IOException {
    raf.close();
  }

  @Override
  public int read() throws IOException {
    if (available() <= 0) {
      return -1;
    }
    return raf.read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (len <= 0) {
      return 0;
    }
    int available = available();
    if (available <= 0) {
      return -1;
    }
    int result = raf.read(b, off, Math.min(len, available));
    return result;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public long skip(long n) throws IOException {
    if (n <= 0) {
      return 0;
    }
    int available = available();
    if (available <= 0) {
      return 0;
    }
    int skipAmount = (int) Math.min(available, n);
    raf.seek(raf.getFilePointer() + skipAmount);
    return skipAmount;
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  /**
   * The readlimit argument is ignored for this implementation, as there is no concept of a buffer
   * to be limited.
   */
  @Override
  public void mark(int readlimit) {
    try {
      mark = raf.getFilePointer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void reset() throws IOException {
    if (mark < 0) {
      throw new IOException("mark not set");
    }
    raf.seek(mark);
  }

  /**
   * Returns the total length of the underlying file at the moment it was opened.
   * @return as described
   */
  public long length() {
    return fileLength;
  }
}
