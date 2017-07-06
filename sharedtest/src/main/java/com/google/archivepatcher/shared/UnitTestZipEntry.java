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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Data for one entry in the zip returned by {@link UnitTestZipArchive#makeTestZip()}.
 */
public class UnitTestZipEntry {
  /**
   * The path under which the data is located in the archive.
   */
  public final String path;

  /**
   * The compression level of the entry.
   */
  public final int level;

  /**
   * The binary content of the entry.
   */
  public final String content;

  /**
   * Optional comment, as an ASCII string.
   */
  public final String comment;

  /**
   * Whether or not to use nowrap.
   */
  public final boolean nowrap;

  /**
   * Creates a new entry with nowrap=true.
   * @param path the path under which the data is located in the archive
   * @param level the compression level of the entry
   * @param content the binary content of the entry, as an ASCII string
   * @param comment optional comment, as an ASCII string
   */
  public UnitTestZipEntry(String path, int level, String content, String comment) {
    this(path, level, true, content, comment);
  }

  /**
   * Creates a new entry.
   *
   * @param path the path under which the data is located in the archive
   * @param level the compression level of the entry
   * @param nowrap the wrapping mode (false to wrap the entry like gzip, true otherwise)
   * @param content the binary content of the entry, as an ASCII string
   * @param comment optional comment, as an ASCII string
   */
  public UnitTestZipEntry(String path, int level, boolean nowrap, String content, String comment) {
    this.path = path;
    this.level = level;
    this.nowrap = nowrap;
    this.content = content;
    this.comment = comment;
  }

  /**
   * Returns the uncompressed content of the entry as a byte array for unit test simplicity. If the
   * level is 0, this is the same as the actual array of bytes that will be present in the zip
   * archive. If the level is not 0, this is the result of uncompressed the bytes that are present
   * in the zip archive for this entry.
   * @return as described
   */
  public byte[] getUncompressedBinaryContent() {
    try {
      return content.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("System doesn't support US-ASCII"); // Not likely
    }
  }

  /**
   * Returns the compressed form of the content, according to the level, that should be found in the
   * zip archive. If the level is 0 (store, i.e. not compressed) this is the same as calling
   * {@link #getUncompressedBinaryContent()}.
   * @return the content, as a byte array
   */
  public byte[] getCompressedBinaryContent() {
    if (level == 0) {
      return getUncompressedBinaryContent();
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DeflateCompressor compressor = new DeflateCompressor();
    compressor.setCompressionLevel(level);
    compressor.setNowrap(nowrap);
    try {
      compressor.compress(new ByteArrayInputStream(getUncompressedBinaryContent()), buffer);
    } catch (IOException e) {
      throw new RuntimeException(e); // Shouldn't happen as this is all in-memory
    }
    return buffer.toByteArray();
  }
}
