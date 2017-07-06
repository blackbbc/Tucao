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

/**
 * A range, annotated with metadata, that is represented as an offset and a length. Comparison is
 * performed based on the natural ordering of the offset field.
 * @param <T> the type of the metadata
 */
public class TypedRange<T> implements Comparable<TypedRange<T>> {
  /**
   * The offset at which the range starts.
   */
  private final long offset;

  /**
   * The length of the range.
   */
  private final long length;

  /**
   * Optional metadata associated with this range.
   */
  private final T metadata;

  /**
   * Constructs a new range with the specified parameters.
   * @param offset the offset at which the range starts
   * @param length the length of the range
   * @param metadata optional metadata associated with this range
   */
  public TypedRange(long offset, long length, T metadata) {
    this.offset = offset;
    this.length = length;
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    return "offset " + offset + ", length " + length + ", metadata " + metadata;
  }

  /**
   * Returns the offset at which the range starts.
   * @return as described
   */
  public long getOffset() {
    return offset;
  }

  /**
   * Returns the length of the range.
   * @return as described
   */
  public long getLength() {
    return length;
  }

  /**
   * Returns the metadata associated with the range, or null if no metadata has been set.
   * @return as described
   */
  public T getMetadata() {
    return metadata;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (length ^ (length >>> 32));
    result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
    result = prime * result + (int) (offset ^ (offset >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TypedRange<?> other = (TypedRange<?>) obj;
    if (length != other.length) return false;
    if (metadata == null) {
      if (other.metadata != null) return false;
    } else if (!metadata.equals(other.metadata)) return false;
    if (offset != other.offset) return false;
    return true;
  }

  @Override
  public int compareTo(TypedRange<T> other) {
    if (getOffset() < other.getOffset()) {
      return -1;
    } else if (getOffset() > other.getOffset()) {
      return 1;
    }
    return 0;
  }
}
