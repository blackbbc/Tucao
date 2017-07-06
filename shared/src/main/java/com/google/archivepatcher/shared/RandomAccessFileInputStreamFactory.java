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

/**
 * An implementation of {@link MultiViewInputStreamFactory} that produces instances of
 * {@link RandomAccessFileInputStream}.
 */
public class RandomAccessFileInputStreamFactory
    implements MultiViewInputStreamFactory<RandomAccessFileInputStream> {

  /**
   * Argument for {@link RandomAccessFileInputStream#RandomAccessFileInputStream(File, long, long)}.
   */
  private final File file;

  /**
   * Argument for {@link RandomAccessFileInputStream#RandomAccessFileInputStream(File, long, long)}.
   */
  private final long rangeOffset;

  /**
   * Argument for {@link RandomAccessFileInputStream#RandomAccessFileInputStream(File, long, long)}.
   */
  private final long rangeLength;

  /**
   * Constructs a new factory that will create instances of {@link RandomAccessFileInputStream} with
   * the specified parameters.
   * @param file the file to use in {@link #newStream()}
   * @param rangeOffset the range offset to use in {@link #newStream()}
   * @param rangeLength the range length to use in {@link #newStream()}
   */
  public RandomAccessFileInputStreamFactory(File file, long rangeOffset, long rangeLength) {
    this.file = file;
    this.rangeOffset = rangeOffset;
    this.rangeLength = rangeLength;
  }

  @Override
  public RandomAccessFileInputStream newStream() throws IOException {
    return new RandomAccessFileInputStream(file, rangeOffset, rangeLength);
  }
}
