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
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Implementation of {@link Compressor} based on Java's built-in {@link Deflater}. Uses default
 * compression, the default strategy, and no-wrap by default along with a 32k read buffer and a 32k
 * write buffer. Buffers are allocated on-demand and discarded after use.
 */
public class DeflateCompressor implements Compressor {

  /**
   * The compression level to use. Defaults to {@link Deflater#DEFAULT_COMPRESSION}.
   */
  private int compressionLevel = Deflater.DEFAULT_COMPRESSION;

  /**
   * The compression strategy to use. Defaults to {@link Deflater#DEFAULT_STRATEGY}.
   */
  private int strategy = Deflater.DEFAULT_STRATEGY;

  /**
   * Whether or not to suppress wrapping the deflate output with the
   * standard zlib header and checksum fields. Defaults to true.
   */
  private boolean nowrap = true;

  /**
   * The size of the buffer used for reading data in during
   * {@link #compress(InputStream, OutputStream)}.
   */
  private int inputBufferSize = 32768;

  /**
   * The size of the buffer used for writing data out during
   * {@link #compress(InputStream, OutputStream)}.
   */
  private int outputBufferSize = 32768;

  /**
   * Cached {@link Deflater} to be used.
   */
  private Deflater deflater = null;

  /**
   * Whether or not to cache {@link Deflater} instances, which is a major performance tradeoff.
   */
  private boolean caching = false;

  /**
   * Returns whether or not to suppress wrapping the deflate output with the standard zlib header
   * and checksum fields.
   * @return the value
   * @see Deflater#Deflater(int, boolean)
   */
  public boolean isNowrap() {
    return nowrap;
  }

  /**
   * Sets whether or not to suppress wrapping the deflate output with the standard zlib header and
   * checksum fields. Defaults to false.
   * @param nowrap see {@link Deflater#Deflater(int, boolean)}
   */
  public void setNowrap(boolean nowrap) {
    if (nowrap != this.nowrap) {
      release(); // Cannot re-use the deflater any more.
      this.nowrap = nowrap;
    }
  }

  /**
   * Returns the compression level that will be used, in the range 0-9.
   * @return the level
   */
  public int getCompressionLevel() {
    return compressionLevel;
  }

  /**
   * Sets the compression level to be used. Defaults to {@link Deflater#BEST_COMPRESSION}.
   * @param compressionLevel the level, in the range 0-9
   */
  public void setCompressionLevel(int compressionLevel) {
    if (compressionLevel < 0 || compressionLevel > 9) {
      throw new IllegalArgumentException(
          "compressionLevel must be in the range [0,9]: " + compressionLevel);
    }
    if (deflater != null && compressionLevel != this.compressionLevel) {
      deflater.reset();
      deflater.setLevel(compressionLevel);
    }
    this.compressionLevel = compressionLevel;
  }

  /**
   * Returns the strategy that will be used, from {@link Deflater}.
   * @return the strategy
   */
  public int getStrategy() {
    return strategy;
  }

  /**
   * Sets the strategy that will be used. Valid values can be found in {@link Deflater}. Defaults to
   * {@link Deflater#DEFAULT_STRATEGY}
   * @param strategy the strategy to be used
   */
  public void setStrategy(int strategy) {
    if (deflater != null && strategy != this.strategy) {
      deflater.reset();
      deflater.setStrategy(strategy);
    }
    this.strategy = strategy;
  }

  /**
   * Returns the size of the buffer used for reading from the input stream in
   * {@link #compress(InputStream, OutputStream)}.
   * @return the size (default is 32768)
   */
  public int getInputBufferSize() {
    return inputBufferSize;
  }

  /**
   * Sets the size of the buffer used for reading from the input stream in
   * {@link #compress(InputStream, OutputStream)}.
   * @param inputBufferSize the size to set (default is 32768)
   */
  public void setInputBufferSize(int inputBufferSize) {
    this.inputBufferSize = inputBufferSize;
  }

  /**
   * Returns the size of the buffer used for writing to the output stream in
   * {@link #compress(InputStream, OutputStream)}.
   * @return the size (default is 32768)
   */
  public int getOutputBufferSize() {
    return outputBufferSize;
  }

  /**
   * Sets the size of the buffer used for writing to the output stream in
   * {@link #compress(InputStream, OutputStream)}.
   * NB: {@link Deflater} uses an <em>internal</em> buffer and this method adjusts the size of that
   * buffer. This buffer is important for performance, <em>even if the {@link OutputStream} is
   * is already buffered</em>.
   * @param outputBufferSize the size to set (default is 32768)
   */
  public void setOutputBufferSize(int outputBufferSize) {
    this.outputBufferSize = outputBufferSize;
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
   * Sets whether or not to cache the {@link Deflater} instance. Defaults to false. If set to true,
   * the {@link Deflater} is kept until this object is finalized or until {@link #release()} is
   * called. Instances of {@link Deflater} can be surprisingly expensive, so caching is advised in
   * situations where many resources need to be deflated.
   * @param caching whether to enable caching
   */
  public void setCaching(boolean caching) {
    this.caching = caching;
  }

  /**
   * Returns the {@link Deflater} to be used, creating a new one if necessary and caching it for
   * future use.
   * @return the deflater
   */
  protected Deflater createOrResetDeflater() {
    Deflater result = deflater;
    if (result == null) {
      result = new Deflater(compressionLevel, nowrap);
      result.setStrategy(strategy);
      if (caching) {
        deflater = result;
      }
    } else {
      result.reset();
    }
    return result;
  }

  /**
   * Immediately releases any cached {@link Deflater} instance.
   */
  public void release() {
    if (deflater != null) {
      deflater.end();
      deflater = null;
    }
  }

  @Override
  public void compress(InputStream uncompressedIn, OutputStream compressedOut) throws IOException {
    byte[] buffer = new byte[inputBufferSize];
    DeflaterOutputStream deflaterOut =
        new DeflaterOutputStream(compressedOut, createOrResetDeflater(), outputBufferSize);
    int numRead = 0;
    while ((numRead = uncompressedIn.read(buffer)) >= 0) {
      deflaterOut.write(buffer, 0, numRead);
    }
    deflaterOut.finish();
    deflaterOut.flush();
  }
}
