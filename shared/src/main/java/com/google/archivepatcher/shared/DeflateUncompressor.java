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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Implementation of {@link Uncompressor} based on Java's built-in {@link Inflater}. Uses no-wrap by
 * default along with a 32k read buffer and a 32k write buffer. Buffers are allocated on-demand and
 * discarded after use. {@link Inflater} instances, which may be expensive, are also created
 * on-demand; This can be changed by using {@link #setCaching(boolean)}.
 */
public class DeflateUncompressor implements Uncompressor {
  /**
   * Whether to skip the standard zlib header and checksum fields when
   * reading. Defaults to true.
   */
  private boolean nowrap = true;

  /**
   * The size of the buffer used for reading data in during
   * {@link #uncompress(InputStream, OutputStream)}.
   */
  private int inputBufferSize = 32768;

  /**
   * The size of the buffer used for writing data out during
   * {@link #uncompress(InputStream, OutputStream)}.
   */
  private int outputBufferSize = 32768;

  /**
   * Cached {@link Inflater} to be used.
   */
  private Inflater inflater = null;

  /**
   * Whether or not to cache {@link Inflater} instances, which is a major performance tradeoff.
   */
  private boolean caching = false;

  /**
   * Returns whether to skip the standard zlib header and checksum fields when reading.
   * @return the value
   * @see Inflater#Inflater(boolean)
   */
  public boolean isNowrap() {
    return nowrap;
  }

  /**
   * Returns the size of the buffer used for reading from the input stream in
   * {@link #uncompress(InputStream, OutputStream)}.
   * @return the size (default is 32768)
   */
  public int getInputBufferSize() {
    return inputBufferSize;
  }

  /**
   * Sets the size of the buffer used for reading from the input stream in
   * {@link #uncompress(InputStream, OutputStream)}.
   * NB: {@link Inflater} uses an <em>internal</em> buffer and this method adjusts the size of that
   * buffer. This buffer is important for performance, <em>even if the {@link InputStream} is
   * is already buffered</em>.
   * @param inputBufferSize the size to set (default is 32768)
   */
  public void setInputBufferSize(int inputBufferSize) {
    this.inputBufferSize = inputBufferSize;
  }

  /**
   * Returns the size of the buffer used for writing to the output stream in
   * {@link #uncompress(InputStream, OutputStream)}.
   * @return the size (default is 32768)
   */
  public int getOutputBufferSize() {
    return outputBufferSize;
  }

  /**
   * Sets the size of the buffer used for writing to the output stream in
   * {@link #uncompress(InputStream, OutputStream)}.
   * @param outputBufferSize the size to set (default is 32768)
   */
  public void setOutputBufferSize(int outputBufferSize) {
    this.outputBufferSize = outputBufferSize;
  }

  /**
   * Sets whether or not to suppress wrapping the deflate output with the standard zlib header and
   * checksum fields. Defaults to false.
   * @param nowrap see {@link Inflater#Inflater(boolean)}
   */
  public void setNowrap(boolean nowrap) {
    if (nowrap != this.nowrap) {
      release(); // Cannot re-use the inflater any more.
      this.nowrap = nowrap;
    }
  }

  /**
   * Returns if caching is enabled.
   * @return true if enabled, otherwise false
   * @see #setCaching(boolean)
   */
  public boolean isCaching() {
    return caching;
  }

  /**
   * Sets whether or not to cache the {@link Inflater} instance. Defaults to false. If set to true,
   * the {@link Inflater} is kept until this object is finalized or until {@link #release()} is
   * called. Instances of {@link Inflater} can be surprisingly expensive, so caching is advised in
   * situations where many resources need to be inflated.
   * @param caching whether to enable caching
   */
  public void setCaching(boolean caching) {
    this.caching = caching;
  }

  /**
   * Returns the {@link Inflater} to be used, creating a new one if necessary and caching it for
   * future use.
   * @return the inflater
   */
  protected Inflater createOrResetInflater() {
    Inflater result = inflater;
    if (result == null) {
      result = new Inflater(nowrap);
      if (caching) {
        inflater = result;
      }
    } else {
      result.reset();
    }
    return result;
  }

  /**
   * Immediately releases any cached {@link Inflater} instance.
   */
  public void release() {
    if (inflater != null) {
      inflater.end();
      inflater = null;
    }
  }

  @Override
  public void uncompress(InputStream compressedIn, OutputStream uncompressedOut)
      throws IOException {
    InflaterInputStream inflaterIn =
        new InflaterInputStream(compressedIn, createOrResetInflater(), inputBufferSize);
    byte[] buffer = new byte[outputBufferSize];
    int numRead = 0;
    while ((numRead = inflaterIn.read(buffer)) >= 0) {
      uncompressedOut.write(buffer, 0, numRead);
    }
    if (!isCaching()) {
      release();
    }
  }
}
