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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link PatchReader}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class PatchReaderTest {
  // This is Integer.MAX_VALUE + 1.
  private static final long BIG = 2048L * 1024L * 1024L;

  private static final JreDeflateParameters DEFLATE_PARAMS = JreDeflateParameters.of(6, 0, true);

  private static final TypedRange<Void> OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE1 =
      new TypedRange<Void>(BIG, 17L, null);

  private static final TypedRange<Void> OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE2 =
      new TypedRange<Void>(BIG + 25L, 19L, null);

  private static final List<TypedRange<Void>> OLD_DELTA_FRIENDLY_UNCOMPRESS_PLAN =
      Collections.unmodifiableList(
          Arrays.asList(
              OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE1, OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE2));

  private static final TypedRange<JreDeflateParameters> NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE1 =
      new TypedRange<JreDeflateParameters>(BIG, BIG, DEFLATE_PARAMS);

  private static final TypedRange<JreDeflateParameters> NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE2 =
      new TypedRange<JreDeflateParameters>(BIG * 2, BIG, DEFLATE_PARAMS);

  private static final List<TypedRange<JreDeflateParameters>> NEW_DELTA_FRIENDLY_RECOMPRESS_PLAN =
      Collections.unmodifiableList(
          Arrays.asList(
              NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE1, NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE2));

  private static final long DELTA_FRIENDLY_OLD_FILE_SIZE = BIG - 75L;

  private static final long DELTA_FRIENDLY_NEW_FILE_SIZE = BIG + 75L;

  private static final TypedRange<Void> DELTA_FRIENDLY_OLD_FILE_WORK_RANGE =
      new TypedRange<Void>(0, DELTA_FRIENDLY_OLD_FILE_SIZE, null);

  private static final TypedRange<Void> DELTA_FRIENDLY_NEW_FILE_WORK_RANGE =
      new TypedRange<Void>(0, DELTA_FRIENDLY_NEW_FILE_SIZE, null);

  private static final String DELTA_CONTENT = "all your delta are belong to us";

  private static final DeltaDescriptor DELTA_DESCRIPTOR =
      new DeltaDescriptor(
          PatchConstants.DeltaFormat.BSDIFF,
          DELTA_FRIENDLY_OLD_FILE_WORK_RANGE,
          DELTA_FRIENDLY_NEW_FILE_WORK_RANGE,
          DELTA_CONTENT.length());

  private static final List<DeltaDescriptor> DELTA_DESCRIPTORS =
      Collections.singletonList(DELTA_DESCRIPTOR);

  private Corruption corruption = null;

  /**
   * Settings that can be altered to break the code under test in useful ways.
   */
  private static class Corruption {
    boolean corruptIdentifier = false;
    boolean corruptDeltaFriendlyOldFileSize = false;
    boolean corruptOldFileUncompressionInstructionCount = false;
    boolean corruptOldFileUncompressionInstructionOffset = false;
    boolean corruptOldFileUncompressionInstructionLength = false;
    boolean corruptOldFileUncompressionInstructionOrder = false;
    boolean corruptDeltaFriendlyNewFileRecompressionInstructionCount = false;
    boolean corruptDeltaFriendlyNewFileRecompressionInstructionOffset = false;
    boolean corruptDeltaFriendlyNewFileRecompressionInstructionLength = false;
    boolean corruptDeltaFriendlyNewFileRecompressionInstructionOrder = false;
    boolean corruptCompatibilityWindowId = false;
    boolean corruptLevel = false;
    boolean corruptStrategy = false;
    boolean corruptNowrap = false;
    boolean corruptNumDeltaRecords = false;
    boolean corruptDeltaType = false;
    boolean corruptDeltaFriendlyOldFileWorkRangeOffset = false;
    boolean corruptDeltaFriendlyOldFileWorkRangeLength = false;
    boolean corruptDeltaFriendlyNewFileWorkRangeOffset = false;
    boolean corruptDeltaFriendlyNewFileWorkRangeLength = false;
    boolean corruptDeltaLength = false;
  }

  @Before
  public void setup() {
    corruption = new Corruption();
  }

  /**
   * Write a test patch with the constants in this file.
   * @return the patch, as an array of bytes.
   * @throws IOException if something goes wrong
   */
  private byte[] writeTestPatch() throws IOException {
    // ---------------------------------------------------------------------------------------------
    // CAUTION - DO NOT CHANGE THIS FUNCTION WITHOUT DUE CONSIDERATION FOR BREAKING THE PATCH FORMAT
    // ---------------------------------------------------------------------------------------------
    // This test writes a simple patch with all the static data listed above and verifies that it
    // can be read. This code MUST be INDEPENDENT of the patch writer code, even if it is partially
    // redundant; this guards against accidental changes to the patch reader that could alter the
    // expected format and otherwise escape detection.
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    DataOutputStream patchOut = new DataOutputStream(out);
    patchOut.write(
        corruption.corruptIdentifier
            ? new byte[8]
            : PatchConstants.IDENTIFIER.getBytes("US-ASCII")); // header
    patchOut.writeInt(0); // Flags, all reserved in v1
    patchOut.writeLong(
        corruption.corruptDeltaFriendlyOldFileSize ? -1 : DELTA_FRIENDLY_OLD_FILE_SIZE);

    // Write the uncompression instructions
    patchOut.writeInt(
        corruption.corruptOldFileUncompressionInstructionCount
            ? -1
            : OLD_DELTA_FRIENDLY_UNCOMPRESS_PLAN.size());
    List<TypedRange<Void>> oldDeltaFriendlyUncompressPlan =
        new ArrayList<TypedRange<Void>>(OLD_DELTA_FRIENDLY_UNCOMPRESS_PLAN);
    if (corruption.corruptOldFileUncompressionInstructionOrder) {
      Collections.reverse(oldDeltaFriendlyUncompressPlan);
    }
    for (TypedRange<Void> range : oldDeltaFriendlyUncompressPlan) {
      patchOut.writeLong(
          corruption.corruptOldFileUncompressionInstructionOffset ? -1 : range.getOffset());
      patchOut.writeLong(
          corruption.corruptOldFileUncompressionInstructionLength ? -1 : range.getLength());
    }

    // Write the recompression instructions
    patchOut.writeInt(
        corruption.corruptDeltaFriendlyNewFileRecompressionInstructionCount
            ? -1
            : NEW_DELTA_FRIENDLY_RECOMPRESS_PLAN.size());
    List<TypedRange<JreDeflateParameters>> newDeltaFriendlyRecompressPlan =
        new ArrayList<TypedRange<JreDeflateParameters>>(NEW_DELTA_FRIENDLY_RECOMPRESS_PLAN);
    if (corruption.corruptDeltaFriendlyNewFileRecompressionInstructionOrder) {
      Collections.reverse(newDeltaFriendlyRecompressPlan);
    }
    for (TypedRange<JreDeflateParameters> range : newDeltaFriendlyRecompressPlan) {
      patchOut.writeLong(
          corruption.corruptDeltaFriendlyNewFileRecompressionInstructionOffset
              ? -1
              : range.getOffset());
      patchOut.writeLong(
          corruption.corruptDeltaFriendlyNewFileRecompressionInstructionLength
              ? -1
              : range.getLength());
      // Now the JreDeflateParameters for the record
      patchOut.write(
          corruption.corruptCompatibilityWindowId
              ? 31
              : PatchConstants.CompatibilityWindowId.DEFAULT_DEFLATE.patchValue);
      patchOut.write(corruption.corruptLevel ? 11 : range.getMetadata().level);
      patchOut.write(corruption.corruptStrategy ? 11 : range.getMetadata().strategy);
      patchOut.write(corruption.corruptNowrap ? 3 : (range.getMetadata().nowrap ? 1 : 0));
    }

    // Delta section. V1 patches have exactly one delta entry and it is always mapped to the entire
    // file contents of the delta-friendly files.
    patchOut.writeInt(
        corruption.corruptNumDeltaRecords
            ? -1
            : DELTA_DESCRIPTORS.size()); // Number of difference records
    for (DeltaDescriptor descriptor : DELTA_DESCRIPTORS) {
      patchOut.write(corruption.corruptDeltaType ? 73 : descriptor.getFormat().patchValue);
      patchOut.writeLong(
          corruption.corruptDeltaFriendlyOldFileWorkRangeOffset
              ? -1
              : descriptor.getDeltaFriendlyOldFileRange().getOffset());
      patchOut.writeLong(
          corruption.corruptDeltaFriendlyOldFileWorkRangeLength
              ? -1
              : descriptor.getDeltaFriendlyOldFileRange().getLength());
      patchOut.writeLong(
          corruption.corruptDeltaFriendlyNewFileWorkRangeOffset
              ? -1
              : descriptor.getDeltaFriendlyNewFileRange().getOffset());
      patchOut.writeLong(
          corruption.corruptDeltaFriendlyNewFileWorkRangeLength
              ? -1
              : descriptor.getDeltaFriendlyNewFileRange().getLength());
      patchOut.writeLong(corruption.corruptDeltaLength ? -1 : descriptor.getDeltaLength());
    }

    // Finally, the delta bytes
    patchOut.write(DELTA_CONTENT.getBytes("US-ASCII"));
    return out.toByteArray();
  }

  @Test
  public void testReadPatchApplyPlan() throws IOException {
    PatchApplyPlan plan =
        new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
    Assert.assertEquals(DELTA_FRIENDLY_OLD_FILE_SIZE, plan.getDeltaFriendlyOldFileSize());
    Assert.assertEquals(OLD_DELTA_FRIENDLY_UNCOMPRESS_PLAN, plan.getOldFileUncompressionPlan());
    Assert.assertEquals(
        NEW_DELTA_FRIENDLY_RECOMPRESS_PLAN, plan.getDeltaFriendlyNewFileRecompressionPlan());
    Assert.assertEquals(DELTA_DESCRIPTORS, plan.getDeltaDescriptors());
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptIdentifier() throws IOException {
    corruption.corruptIdentifier = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyOldFileSize() throws IOException {
    corruption.corruptDeltaFriendlyOldFileSize = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptOldFileUncompressionInstructionCount()
      throws IOException {
    corruption.corruptOldFileUncompressionInstructionCount = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptOldFileUncompressionInstructionOrder()
      throws IOException {
    corruption.corruptOldFileUncompressionInstructionOrder = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptOldFileUncompressionInstructionOffset()
      throws IOException {
    corruption.corruptOldFileUncompressionInstructionOffset = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptOldFileUncompressionInstructionLength()
      throws IOException {
    corruption.corruptOldFileUncompressionInstructionLength = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyNewFileRecompressionInstructionCount()
      throws IOException {
    corruption.corruptDeltaFriendlyNewFileRecompressionInstructionCount = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyNewFileRecompressionInstructionOrder()
      throws IOException {
    corruption.corruptDeltaFriendlyNewFileRecompressionInstructionOrder = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyNewFileRecompressionInstructionOffset()
      throws IOException {
    corruption.corruptDeltaFriendlyNewFileRecompressionInstructionOffset = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyNewFileRecompressionInstructionLength()
      throws IOException {
    corruption.corruptDeltaFriendlyNewFileRecompressionInstructionLength = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptCompatibilityWindowId() throws IOException {
    corruption.corruptCompatibilityWindowId = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptLevel() throws IOException {
    corruption.corruptLevel = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptStrategy() throws IOException {
    corruption.corruptStrategy = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptNowrap() throws IOException {
    corruption.corruptNowrap = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptNumDeltaRecords() throws IOException {
    corruption.corruptNumDeltaRecords = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaType() throws IOException {
    corruption.corruptDeltaType = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyOldFileWorkRangeOffset()
      throws IOException {
    corruption.corruptDeltaFriendlyOldFileWorkRangeOffset = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyOldFileWorkRangeLength()
      throws IOException {
    corruption.corruptDeltaFriendlyOldFileWorkRangeLength = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyNewFileWorkRangeOffset()
      throws IOException {
    corruption.corruptDeltaFriendlyNewFileWorkRangeOffset = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_CorruptDeltaFriendlyNewFileWorkRangeLength()
      throws IOException {
    corruption.corruptDeltaFriendlyNewFileWorkRangeLength = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }

  @Test(expected = PatchFormatException.class)
  public void testReadPatchApplyPlan_DeltaLength() throws IOException {
    corruption.corruptDeltaLength = true;
    new PatchReader().readPatchApplyPlan(new ByteArrayInputStream(writeTestPatch()));
  }
}
