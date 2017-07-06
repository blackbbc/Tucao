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
import com.google.archivepatcher.shared.PatchConstants;
import com.google.archivepatcher.shared.TypedRange;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Reads patches.
 */
public class PatchReader {

  /**
   * Reads patch data from the specified {@link InputStream} up to but not including the first byte
   * of delta bytes, and returns a {@link PatchApplyPlan} that describes all the operations that
   * need to be performed in order to apply the patch. When this method returns, the stream is
   * positioned so that the next read will be the first byte of delta bytes corresponding to the
   * first {@link DeltaDescriptor} in the returned plan.
   * @param in the stream to read from
   * @return the plan for applying the patch
   * @throws IOException if anything goes wrong
   */
  public PatchApplyPlan readPatchApplyPlan(InputStream in) throws IOException {
    // Use DataOutputStream for ease of writing. This is deliberately left open, as closing it would
    // close the output stream that was passed in and that is not part of the method's documented
    // behavior.
    @SuppressWarnings("resource")
    DataInputStream dataIn = new DataInputStream(in);

    // Read header and flags.
    byte[] expectedIdentifier = PatchConstants.IDENTIFIER.getBytes("US-ASCII");
    byte[] actualIdentifier = new byte[expectedIdentifier.length];
    dataIn.readFully(actualIdentifier);
    if (!Arrays.equals(expectedIdentifier, actualIdentifier)) {
      throw new PatchFormatException("Bad identifier");
    }
    dataIn.skip(4); // Flags (ignored in v1)
    long deltaFriendlyOldFileSize = checkNonNegative(
        dataIn.readLong(), "delta-friendly old file size");

    // Read old file uncompression instructions.
    int numOldFileUncompressionInstructions = (int) checkNonNegative(
        dataIn.readInt(), "old file uncompression instruction count");
    List<TypedRange<Void>> oldFileUncompressionPlan =
        new ArrayList<TypedRange<Void>>(numOldFileUncompressionInstructions);
    long lastReadOffset = -1;
    for (int x = 0; x < numOldFileUncompressionInstructions; x++) {
      long offset = checkNonNegative(dataIn.readLong(), "old file uncompression range offset");
      long length = checkNonNegative(dataIn.readLong(), "old file uncompression range length");
      if (offset < lastReadOffset) {
        throw new PatchFormatException("old file uncompression ranges out of order or overlapping");
      }
      TypedRange<Void> range = new TypedRange<Void>(offset, length, null);
      oldFileUncompressionPlan.add(range);
      lastReadOffset = offset + length; // To check that the next range starts after the current one
    }

    // Read new file recompression instructions
    int numDeltaFriendlyNewFileRecompressionInstructions = dataIn.readInt();
    checkNonNegative(
        numDeltaFriendlyNewFileRecompressionInstructions,
        "delta-friendly new file recompression instruction count");
    List<TypedRange<JreDeflateParameters>> deltaFriendlyNewFileRecompressionPlan =
        new ArrayList<TypedRange<JreDeflateParameters>>(
            numDeltaFriendlyNewFileRecompressionInstructions);
    lastReadOffset = -1;
    for (int x = 0; x < numDeltaFriendlyNewFileRecompressionInstructions; x++) {
      long offset = checkNonNegative(
          dataIn.readLong(), "delta-friendly new file recompression range offset");
      long length = checkNonNegative(
          dataIn.readLong(), "delta-friendly new file recompression range length");
      if (offset < lastReadOffset) {
        throw new PatchFormatException(
            "delta-friendly new file recompression ranges out of order or overlapping");
      }
      lastReadOffset = offset + length; // To check that the next range starts after the current one

      // Read the JreDeflateParameters
      // Note that v1 only supports the default deflate compatibility window.
      checkRange(
          dataIn.readByte(),
          PatchConstants.CompatibilityWindowId.DEFAULT_DEFLATE.patchValue,
          PatchConstants.CompatibilityWindowId.DEFAULT_DEFLATE.patchValue,
          "compatibility window id");
      int level = (int) checkRange(dataIn.readUnsignedByte(), 1, 9, "recompression level");
      int strategy = (int) checkRange(dataIn.readUnsignedByte(), 0, 2, "recompression strategy");
      int nowrapInt = (int) checkRange(dataIn.readUnsignedByte(), 0, 1, "recompression nowrap");
      TypedRange<JreDeflateParameters> range =
          new TypedRange<JreDeflateParameters>(
              offset,
              length,
              JreDeflateParameters.of(level, strategy, nowrapInt == 0 ? false : true));
      deltaFriendlyNewFileRecompressionPlan.add(range);
    }

    // Read the delta metadata, but stop before the first byte of the actual delta.
    // V1 has exactly one delta and it must be bsdiff.
    int numDeltaRecords = (int) checkRange(dataIn.readInt(), 1, 1, "num delta records");

    List<DeltaDescriptor> deltaDescriptors = new ArrayList<DeltaDescriptor>(numDeltaRecords);
    for (int x = 0; x < numDeltaRecords; x++) {
      byte deltaFormatByte = (byte)
      checkRange(
          dataIn.readByte(),
          PatchConstants.DeltaFormat.BSDIFF.patchValue,
          PatchConstants.DeltaFormat.BSDIFF.patchValue,
          "delta format");
      long deltaFriendlyOldFileWorkRangeOffset = checkNonNegative(
          dataIn.readLong(), "delta-friendly old file work range offset");
      long deltaFriendlyOldFileWorkRangeLength = checkNonNegative(
          dataIn.readLong(), "delta-friendly old file work range length");
      long deltaFriendlyNewFileWorkRangeOffset = checkNonNegative(
          dataIn.readLong(), "delta-friendly new file work range offset");
      long deltaFriendlyNewFileWorkRangeLength = checkNonNegative(
          dataIn.readLong(), "delta-friendly new file work range length");
      long deltaLength = checkNonNegative(dataIn.readLong(), "delta length");
      DeltaDescriptor descriptor =
          new DeltaDescriptor(
              PatchConstants.DeltaFormat.fromPatchValue(deltaFormatByte),
              new TypedRange<Void>(
                  deltaFriendlyOldFileWorkRangeOffset, deltaFriendlyOldFileWorkRangeLength, null),
              new TypedRange<Void>(
                  deltaFriendlyNewFileWorkRangeOffset, deltaFriendlyNewFileWorkRangeLength, null),
              deltaLength);
      deltaDescriptors.add(descriptor);
    }

    return new PatchApplyPlan(
        Collections.unmodifiableList(oldFileUncompressionPlan),
        deltaFriendlyOldFileSize,
        Collections.unmodifiableList(deltaFriendlyNewFileRecompressionPlan),
        Collections.unmodifiableList(deltaDescriptors));
  }

  /**
   * Assert that the value isn't negative.
   * @param value the value to check
   * @param description the description to use in error messages if the value is not ok
   * @return the value
   * @throws PatchFormatException if the value is not ok
   */
  private static final long checkNonNegative(long value, String description)
      throws PatchFormatException {
    if (value < 0) {
      throw new PatchFormatException("Bad value for " + description + ": " + value);
    }
    return value;
  }

  /**
   * Assert that the value is in the specified range.
   * @param value the value to check
   * @param min the minimum (inclusive) value to allow
   * @param max the maximum (inclusive) value to allow
   * @param description the description to use in error messages if the value is not ok
   * @return the value
   * @throws PatchFormatException if the value is not ok
   */
  private static final long checkRange(long value, long min, long max, String description)
      throws PatchFormatException {
    if (value < min || value > max) {
      throw new PatchFormatException(
          "Bad value for "
              + description
              + ": "
              + value
              + " (valid range: ["
              + min
              + ","
              + max
              + "]");
    }
    return value;
  }
}
