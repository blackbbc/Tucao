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

/**
 * An interface for implementing a streaming uncompressor. An uncompressor may be used to uncompress
 * data that was previously compressed by the corresponding {@link Compressor} implementation, and
 * always operates in a streaming manner.
 */
public interface Uncompressor {
  /**
   * Uncompresses data that was previously processed by the corresponding {@link Compressor}
   * implementation, writing the uncompressed data into uncompressedOut.
   *
   * @param compressedIn the compressed data
   * @param uncompressedOut the uncompressed data
   * @throws IOException if something goes awry while reading or writing
   */
  public void uncompress(InputStream compressedIn, OutputStream uncompressedOut) throws IOException;
}
