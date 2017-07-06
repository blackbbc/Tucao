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

import com.google.archivepatcher.shared.PatchConstants;
import com.google.archivepatcher.shared.PatchConstants.DeltaFormat;

import com.google.archivepatcher.shared.TypedRange;

/**
 * Describes all of the information needed to apply a single delta operation - the format of the
 * delta, the ranges in the delta-friendly old and new files that serve as inputs and outputs, and
 * the number of bytes that the delta comprises in the patch stream.
 */
public class DeltaDescriptor {
  /**
   * The format of the delta.
   */
  private final PatchConstants.DeltaFormat format;

  /**
   * The work range for the delta-friendly old file.
   */
  private final TypedRange<Void> deltaFriendlyOldFileRange;

  /**
   * The work range for the delta-friendly new file.
   */
  private final TypedRange<Void> deltaFriendlyNewFileRange;

  /**
   * The number of bytes of delta data in the patch stream.
   */
  private final long deltaLength;

  /**
   * Constructs a new descriptor having the specified data.
   * @param format the format of the delta
   * @param deltaFriendlyOldFileRange the work range for the delta-friendly old file
   * @param deltaFriendlyNewFileRange the work range for the delta-friendly new file
   * @param deltaLength the number of bytes of delta data in the patch stream
   */
  public DeltaDescriptor(
      DeltaFormat format,
      TypedRange<Void> deltaFriendlyOldFileRange,
      TypedRange<Void> deltaFriendlyNewFileRange,
      long deltaLength) {
    this.format = format;
    this.deltaFriendlyOldFileRange = deltaFriendlyOldFileRange;
    this.deltaFriendlyNewFileRange = deltaFriendlyNewFileRange;
    this.deltaLength = deltaLength;
  }

  /**
   * Returns the format of the delta.
   * @return as described
   */
  public PatchConstants.DeltaFormat getFormat() {
    return format;
  }

  /**
   * Returns the work range for the delta-friendly old file.
   * @return as described
   */
  public TypedRange<Void> getDeltaFriendlyOldFileRange() {
    return deltaFriendlyOldFileRange;
  }

  /**
   * Returns the work range for the delta-friendly new file.
   * @return as described
   */
  public TypedRange<Void> getDeltaFriendlyNewFileRange() {
    return deltaFriendlyNewFileRange;
  }

  /**
   * Returns the number of bytes of delta data in the patch stream.
   * @return as described
   */
  public long getDeltaLength() {
    return deltaLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((deltaFriendlyNewFileRange == null) ? 0 : deltaFriendlyNewFileRange.hashCode());
    result =
        prime * result
            + ((deltaFriendlyOldFileRange == null) ? 0 : deltaFriendlyOldFileRange.hashCode());
    result = prime * result + (int) (deltaLength ^ (deltaLength >>> 32));
    result = prime * result + ((format == null) ? 0 : format.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DeltaDescriptor other = (DeltaDescriptor) obj;
    if (deltaFriendlyNewFileRange == null) {
      if (other.deltaFriendlyNewFileRange != null) return false;
    } else if (!deltaFriendlyNewFileRange.equals(other.deltaFriendlyNewFileRange)) return false;
    if (deltaFriendlyOldFileRange == null) {
      if (other.deltaFriendlyOldFileRange != null) return false;
    } else if (!deltaFriendlyOldFileRange.equals(other.deltaFriendlyOldFileRange)) return false;
    if (deltaLength != other.deltaLength) return false;
    if (format != other.format) return false;
    return true;
  }
}
