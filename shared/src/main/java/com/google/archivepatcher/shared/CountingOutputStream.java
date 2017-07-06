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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Trivial output stream that counts the bytes written to it.
 */
public class CountingOutputStream extends FilterOutputStream {
  /**
   * Number of bytes written so far.
   */
  private long bytesWritten = 0;

  /**
   * Create a new counting output stream.
   * @param out the output stream to wrap
   */
  public CountingOutputStream(OutputStream out) {
    super(out);
  }

  /**
   * Returns the number of bytes written to this stream so far.
   * @return as described
   */
  public long getNumBytesWritten() {
    return bytesWritten;
  }

  @Override
  public void write(int b) throws IOException {
    bytesWritten++;
    out.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    bytesWritten += b.length;
    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    bytesWritten += len;
    out.write(b, off, len);
  }
}
