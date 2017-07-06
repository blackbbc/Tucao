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

import java.util.List;

/**
 * A plan for transforming the old file prior to applying the delta and for recompressing the
 * delta-friendly new file afterwards, along with information on the deltas to be applied.
 * <p>
 * The plan for uncompressing the old file is a {@link List} of {@link TypedRange} entries with void
 * metadata. This describes the chunks of the old file that need to be uncompressed prior to
 * applying the delta, in file order. The file produced by executing this plan is the
 * "delta-friendly" old file.
 * <p>
 * The plan for recompressing the delta-friendly new file is a {@link List} of {@link TypedRange}
 * entries with {@link JreDeflateParameters} metadata. This describes the chunks of the
 * delta-friendly new file that need to be recompressed after diffing, again in file order.
 * The {@link JreDeflateParameters} metadata indicate the settings to use during recompression. The
 * file produced by executing this plan is the new file.
 * <p>
 * The "plan" for the deltas themselves is a {@link List} of {@link DeltaDescriptor} entries that
 * describe the deltas present in the patch stream. Nominally, a {@link PatchReader} is used to
 * read the stream up to the first byte of the deltas; the plan for the deltas is ordered in the
 * same order as the patch stream and contains the byte length of each delta, so it is then trivial
 * to read each delta in order and apply it.
 */
public class PatchApplyPlan {
  /**
   * The plan for uncompressing the old file, in file order.
   */
  private final List<TypedRange<Void>> oldFileUncompressionPlan;

  /**
   * The plan for recompressing the delta-friendly new file, in file order.
   */
  private final List<TypedRange<JreDeflateParameters>> deltaFriendlyNewFileRecompressionPlan;

  /**
   * The expected size of the delta-friendly old file after executing the
   * {@link #oldFileUncompressionPlan}.
   */
  private final long deltaFriendlyOldFileSize;

  /**
   * The delta descriptors that describe how and what to do to the delta-friendly old file.
   */
  private final List<DeltaDescriptor> deltaDescriptors;

  /**
   * Constructs a new plan.
   * @param oldFileUncompressionPlan the plan for uncompressing the old file, in file order
   * @param deltaFriendlyOldFileSize the expected size of the delta-friendly old file, after
   * executing the plan in oldFileUncompressionPlan; this can be used to pre-allocate the necessary
   * space to hold the delta-friendly old file
   * @param deltaFriendlyNewFileRecompressionPlan the plan for recompressing the delta-friendly new
   * file, in file order
   * @param deltaDescriptors the descriptors for the deltas in the patch stream
   */
  public PatchApplyPlan(
      List<TypedRange<Void>> oldFileUncompressionPlan,
      long deltaFriendlyOldFileSize,
      List<TypedRange<JreDeflateParameters>> deltaFriendlyNewFileRecompressionPlan,
      List<DeltaDescriptor> deltaDescriptors) {
    this.oldFileUncompressionPlan = oldFileUncompressionPlan;
    this.deltaFriendlyOldFileSize = deltaFriendlyOldFileSize;
    this.deltaFriendlyNewFileRecompressionPlan = deltaFriendlyNewFileRecompressionPlan;
    this.deltaDescriptors = deltaDescriptors;
  }

  /**
   * Returns the old file uncompression plan.
   * @return as described
   */
  public List<TypedRange<Void>> getOldFileUncompressionPlan() {
    return oldFileUncompressionPlan;
  }

  /**
   * Returns the delta-friendly new file recompression plan.
   * @return as described
   */
  public List<TypedRange<JreDeflateParameters>> getDeltaFriendlyNewFileRecompressionPlan() {
    return deltaFriendlyNewFileRecompressionPlan;
  }

  /**
   * Returns the expected size of the delta-friendly old file after executing the plan returned by
   * {@link #getOldFileUncompressionPlan()}. This can be used to pre-allocate the necessary space to
   * hold the delta-friendly old file.
   * @return as described
   */
  public long getDeltaFriendlyOldFileSize() {
    return deltaFriendlyOldFileSize;
  }

  /**
   * Returns the delta descriptors that describe how and what to do to the delta-friendly old file.
   * @return as described
   */
  public List<DeltaDescriptor> getDeltaDescriptors() {
    return deltaDescriptors;
  }
}
