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

import com.google.archivepatcher.shared.JreDeflateParameters;
import com.google.archivepatcher.shared.TypedRange;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * An {@link OutputStream} that is pre-configured to compress some of the bytes that are written to
 * it according to the specified parameters.
 */
public class PartiallyCompressingOutputStream extends FilterOutputStream {

  /**
   * The underlying stream.
   */
  private final OutputStream normalOut;

  /**
   * The deflater, non-null only during compression.
   */
  private Deflater deflater = null;

  /**
   * The deflater stream, non-null only during compression.
   */
  private DeflaterOutputStream deflaterOut = null;

  /**
   * Used when writing one byte at a time.
   */
  private final byte[] internalCopyBuffer = new byte[1];

  /**
   * The size of the buffer to use when compressing bytes.
   */
  private final int compressionBufferSize;

  /**
   * The number of bytes written so far.
   */
  private long numBytesWritten;

  /**
   * The iterator that is used to iterate over the compression ranges.
   */
  private final Iterator<TypedRange<JreDeflateParameters>> rangeIterator;

  /**
   * The compress range that is either being worked on or that is coming up next.
   */
  private TypedRange<JreDeflateParameters> nextCompressedRange = null;

  /**
   * The last {@link JreDeflateParameters} that were used.
   */
  private JreDeflateParameters lastDeflateParameters = null;

  /**
   * Creates a new stream that wraps the specified other stream, compressing the specified ranges
   * with the specified parameters. All unspecified ranges are implicitly copied without
   * modification.
   * @param compressionRanges ranges to be compressed, with accompanying parameters
   * @param out the stream to write to
   * @param compressionBufferSize the size of the buffer to use when compressing data
   */
  public PartiallyCompressingOutputStream(
      List<TypedRange<JreDeflateParameters>> compressionRanges,
      OutputStream out,
      int compressionBufferSize) {
    super(out);
    this.normalOut = out;
    this.compressionBufferSize = compressionBufferSize;
    rangeIterator = compressionRanges.iterator();
    if (rangeIterator.hasNext()) {
      nextCompressedRange = rangeIterator.next();
    } else {
      // Degenerate case, no compression at all/
      nextCompressedRange = null;
    }
  }

  @Override
  public void write(int b) throws IOException {
    internalCopyBuffer[0] = (byte) b;
    write(internalCopyBuffer, 0, 1);
  }

  @Override
  public void write(byte[] buffer) throws IOException {
    write(buffer, 0, buffer.length);
  }

  @Override
  public void write(byte[] buffer, int offset, int length) throws IOException {
    int writtenSoFar = 0;
    while (writtenSoFar < length) {
      writtenSoFar += writeChunk(buffer, offset + writtenSoFar, length - writtenSoFar);
    }
  }

  /**
   * Write up to <em>length</em> bytes from the specified buffer to the underlying stream. For
   * simplicity, this method stops at the edges of ranges; it is always either copying OR
   * compressing bytes, but never both. All manipulation of the compression state machinery is done
   * within this method. When the end of a compression range is reached it is completely flushed to
   * the output stream, to keep things as straightforward as possible.
   * @param buffer the buffer to copy/compress bytes from
   * @param offset the offset at which to start copying/compressing
   * @param length the maximum number of bytes to copy or compress
   * @return the number of bytes of the buffer that have been consumed
   */
  private int writeChunk(byte[] buffer, int offset, int length) throws IOException {
    if (bytesTillCompressionStarts() == 0 && !currentlyCompressing()) {
      // Compression will begin immediately.
      JreDeflateParameters parameters = nextCompressedRange.getMetadata();
      if (deflater == null) {
        deflater = new Deflater(parameters.level, parameters.nowrap);
      } else if (lastDeflateParameters.nowrap != parameters.nowrap) {
        // Last deflater must be destroyed because nowrap settings do not match.
        deflater.end();
        deflater = new Deflater(parameters.level, parameters.nowrap);
      }
      // Deflater will already have been reset at the end of this method, no need to do it again.
      // Just set up the right parameters.
      deflater.setLevel(parameters.level);
      deflater.setStrategy(parameters.strategy);
      deflaterOut = new DeflaterOutputStream(normalOut, deflater, compressionBufferSize);
    }

    int numBytesToWrite;
    OutputStream writeTarget;
    if (currentlyCompressing()) {
      // Don't write past the end of the compressed range.
      numBytesToWrite = (int) Math.min(length, bytesTillCompressionEnds());
      writeTarget = deflaterOut;
    } else {
      writeTarget = normalOut;
      if (nextCompressedRange == null) {
        // All compression ranges have been consumed.
        numBytesToWrite = length;
      } else {
        // Don't write past the point where the next compressed range begins.
        numBytesToWrite = (int) Math.min(length, bytesTillCompressionStarts());
      }
    }

    writeTarget.write(buffer, offset, numBytesToWrite);
    numBytesWritten += numBytesToWrite;

    if (currentlyCompressing() && bytesTillCompressionEnds() == 0) {
      // Compression range complete. Finish the output and set up for the next run.
      deflaterOut.finish();
      deflaterOut.flush();
      deflaterOut = null;
      deflater.reset();
      lastDeflateParameters = nextCompressedRange.getMetadata();
      if (rangeIterator.hasNext()) {
        // More compression ranges await in the future.
        nextCompressedRange = rangeIterator.next();
      } else {
        // All compression ranges have been consumed.
        nextCompressedRange = null;
        deflater.end();
        deflater = null;
      }
    }

    return numBytesToWrite;
  }

  private boolean currentlyCompressing() {
    return deflaterOut != null;
  }

  private long bytesTillCompressionStarts() {
    if (nextCompressedRange == null) {
      // All compression ranges have been consumed
      return -1L;
    }
    return nextCompressedRange.getOffset() - numBytesWritten;
  }

  private long bytesTillCompressionEnds() {
    if (nextCompressedRange == null) {
      // All compression ranges have been consumed
      return -1L;
    }
    return (nextCompressedRange.getOffset() + nextCompressedRange.getLength()) - numBytesWritten;
  }
}
