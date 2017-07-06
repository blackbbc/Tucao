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
import com.google.archivepatcher.shared.UnitTestZipEntry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link FileByFileV1DeltaApplier}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class FileByFileV1DeltaApplierTest {

  // These constants are used to construct all the blobs (note the OLD and NEW contents):
  //   old file := UNCOMPRESSED_HEADER + COMPRESSED_OLD_CONTENT + UNCOMPRESSED_TRAILER
  //   delta-friendly old file := UNCOMPRESSED_HEADER + UNCOMPRESSED_OLD_CONTENT +
  //                              UNCOMPRESSED_TRAILER
  //   delta-friendly new file := UNCOMPRESSED_HEADER + UNCOMPRESSED_NEW_CONTENT +
  //                              UNCOMPRESSED_TRAILER
  //   new file := UNCOMPRESSED_HEADER + COMPRESSED_NEW_CONTENT + UNCOMPRESSED_TRAILIER
  // NB: The patch *applier* is agnostic to the format of the file, and so it doesn't have to be a
  //     valid zip or zip-like archive.
  private static final JreDeflateParameters PARAMS1 = JreDeflateParameters.of(6, 0, true);
  private static final String OLD_CONTENT = "This is Content the Old";
  private static final UnitTestZipEntry OLD_ENTRY =
      new UnitTestZipEntry("/foo", PARAMS1.level, PARAMS1.nowrap, OLD_CONTENT, null);
  private static final String NEW_CONTENT = "Rambunctious Absinthe-Loving Stegosaurus";
  private static final UnitTestZipEntry NEW_ENTRY =
      new UnitTestZipEntry("/foo", PARAMS1.level, PARAMS1.nowrap, NEW_CONTENT, null);
  private static final byte[] UNCOMPRESSED_HEADER = new byte[] {0, 1, 2, 3, 4};
  private static final byte[] UNCOMPRESSED_OLD_CONTENT = OLD_ENTRY.getUncompressedBinaryContent();
  private static final byte[] COMPRESSED_OLD_CONTENT = OLD_ENTRY.getCompressedBinaryContent();
  private static final byte[] UNCOMPRESSED_NEW_CONTENT = NEW_ENTRY.getUncompressedBinaryContent();
  private static final byte[] COMPRESSED_NEW_CONTENT = NEW_ENTRY.getCompressedBinaryContent();
  private static final byte[] UNCOMPRESSED_TRAILER = new byte[] {5, 6, 7, 8, 9};
  private static final String BSDIFF_DELTA = "1337 h4x0r";

  /**
   * Where to store temp files.
   */
  private File tempDir;

  /**
   * The old file.
   */
  private File oldFile;

  /**
   * Bytes that describe a patch to convert the old file to the new file.
   */
  private byte[] patchBytes;

  /**
   * Bytes that describe the new file.
   */
  private byte[] expectedNewBytes;

  /**
   * For debugging test issues, it is convenient to be able to see these bytes in the debugger
   * instead of on the filesystem.
   */
  private byte[] oldFileBytes;

  /**
   * Again, for debugging test issues, it is convenient to be able to see these bytes in the
   * debugger instead of on the filesystem.
   */
  private byte[] expectedDeltaFriendlyOldFileBytes;

  /**
   * To mock the dependency on bsdiff, a subclass of FileByFileV1DeltaApplier is made that always
   * returns a testing delta applier. This delta applier asserts that the old content is as
   * expected, and "patches" it by simply writing the expected *new* content to the output stream.
   */
  private FileByFileV1DeltaApplier fakeApplier;
  
  @Before
  public void setUp() throws IOException {
    // Creates the following resources:
    // 1. The old file, on disk (and in-memory, for convenience).
    // 2. The new file, in memory only (for comparing results at the end).
    // 3. The patch, in memory.

    File tempFile = File.createTempFile("foo", "bar");
    tempDir = tempFile.getParentFile();
    tempFile.delete();
    oldFile = File.createTempFile("fbfv1dat", "old");
    oldFile.deleteOnExit();

    // Write the old file to disk:
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    buffer.write(UNCOMPRESSED_HEADER);
    buffer.write(COMPRESSED_OLD_CONTENT);
    buffer.write(UNCOMPRESSED_TRAILER);
    oldFileBytes = buffer.toByteArray();
    FileOutputStream out = new FileOutputStream(oldFile);
    out.write(oldFileBytes);
    out.flush();
    out.close();

    // Write the delta-friendly old file to a byte array
    buffer = new ByteArrayOutputStream();
    buffer.write(UNCOMPRESSED_HEADER);
    buffer.write(UNCOMPRESSED_OLD_CONTENT);
    buffer.write(UNCOMPRESSED_TRAILER);
    expectedDeltaFriendlyOldFileBytes = buffer.toByteArray();

    // Write the new file to a byte array
    buffer = new ByteArrayOutputStream();
    buffer.write(UNCOMPRESSED_HEADER);
    buffer.write(COMPRESSED_NEW_CONTENT);
    buffer.write(UNCOMPRESSED_TRAILER);
    expectedNewBytes = buffer.toByteArray();

    // Finally, write the patch that should transform old to new
    patchBytes = writePatch();

    // Initialize fake delta applier to mock out dependency on bsdiff
    fakeApplier = new FileByFileV1DeltaApplier(tempDir) {
          @Override
          protected DeltaApplier getDeltaApplier() {
            return new FakeDeltaApplier();
          }
        };
  }

  /**
   * Write a patch that will convert the old file to the new file, and return it.
   * @return the patch, as a byte array
   * @throws IOException if anything goes wrong
   */
  private byte[] writePatch() throws IOException {
    long deltaFriendlyOldFileSize =
        UNCOMPRESSED_HEADER.length + UNCOMPRESSED_OLD_CONTENT.length + UNCOMPRESSED_TRAILER.length;
    long deltaFriendlyNewFileSize =
        UNCOMPRESSED_HEADER.length + UNCOMPRESSED_NEW_CONTENT.length + UNCOMPRESSED_TRAILER.length;

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(buffer);
    // Now write a patch, independent of the PatchWrite code.
    dataOut.write(PatchConstants.IDENTIFIER.getBytes("US-ASCII"));
    dataOut.writeInt(0); // Flags (reserved)
    dataOut.writeLong(deltaFriendlyOldFileSize);

    // Write a single uncompress instruction to uncompress the compressed content in oldFile
    dataOut.writeInt(1); // num instructions that follow
    dataOut.writeLong(UNCOMPRESSED_HEADER.length);
    dataOut.writeLong(COMPRESSED_OLD_CONTENT.length);

    // Write a single compress instruction to recompress the uncompressed content in the
    // delta-friendly old file.
    dataOut.writeInt(1); // num instructions that follow
    dataOut.writeLong(UNCOMPRESSED_HEADER.length);
    dataOut.writeLong(UNCOMPRESSED_NEW_CONTENT.length);
    dataOut.write(PatchConstants.CompatibilityWindowId.DEFAULT_DEFLATE.patchValue);
    dataOut.write(PARAMS1.level);
    dataOut.write(PARAMS1.strategy);
    dataOut.write(PARAMS1.nowrap ? 1 : 0);

    // Write a delta. This test class uses its own delta applier to intercept and mangle the data.
    dataOut.writeInt(1);
    dataOut.write(PatchConstants.DeltaFormat.BSDIFF.patchValue);
    dataOut.writeLong(0); // i.e., start of the working range in the delta-friendly old file
    dataOut.writeLong(deltaFriendlyOldFileSize); // i.e., length of the working range in old
    dataOut.writeLong(0); // i.e., start of the working range in the delta-friendly new file
    dataOut.writeLong(deltaFriendlyNewFileSize); // i.e., length of the working range in new

    // Write the length of the delta and the delta itself. Again, this test class uses its own
    // delta applier; so this is irrelevant.
    dataOut.writeLong(BSDIFF_DELTA.length());
    dataOut.write(BSDIFF_DELTA.getBytes("US-ASCII"));
    dataOut.flush();
    return buffer.toByteArray();
  }

  private class FakeDeltaApplier implements DeltaApplier {
  @SuppressWarnings("resource")
  @Override
    public void applyDelta(File oldBlob, InputStream deltaIn, OutputStream newBlobOut)
        throws IOException {
      // Check the patch is as expected
      DataInputStream deltaData = new DataInputStream(deltaIn);
      byte[] actualDeltaDataRead = new byte[BSDIFF_DELTA.length()];
      deltaData.readFully(actualDeltaDataRead);
      Assert.assertArrayEquals(BSDIFF_DELTA.getBytes("US-ASCII"), actualDeltaDataRead);

      // Check that the old data is as expected
      int oldSize = (int) oldBlob.length();
      byte[] oldData = new byte[oldSize];
      FileInputStream oldBlobIn = new FileInputStream(oldBlob);
      DataInputStream oldBlobDataIn = new DataInputStream(oldBlobIn);
      oldBlobDataIn.readFully(oldData);
      Assert.assertArrayEquals(expectedDeltaFriendlyOldFileBytes, oldData);

      // "Convert" the old blob to the new blow as if this were a real patching algorithm.
      newBlobOut.write(UNCOMPRESSED_HEADER);
      newBlobOut.write(NEW_ENTRY.getUncompressedBinaryContent());
      newBlobOut.write(UNCOMPRESSED_TRAILER);
    }
  }

  @After
  public void tearDown() {
    try {
      oldFile.delete();
    } catch (Exception ignored) {
      // Nothing
    }
  }

  @Test
  public void testApplyDelta() throws IOException {
    // Test all aspects of patch apply: copying, uncompressing and recompressing ranges.
    // This test uses the subclasses applier to apply the test patch to the old file, producing the
    // new file. Along the way the entry is uncompressed, altered by the testing delta applier, and
    // recompressed. It's deceptively simple below, but this is a lot of moving parts.
    ByteArrayOutputStream actualNewBlobOut = new ByteArrayOutputStream();
    fakeApplier.applyDelta(oldFile, new ByteArrayInputStream(patchBytes), actualNewBlobOut);
    Assert.assertArrayEquals(expectedNewBytes, actualNewBlobOut.toByteArray());
  }

  @Test
  public void testApplyDelta_DoesntCloseStream() throws IOException {
    // Test for https://github.com/andrewhayden/archive-patcher/issues/6
    final AtomicBoolean closed = new AtomicBoolean(false);
    ByteArrayOutputStream actualNewBlobOut = new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        closed.set(true);
      }
    };
    fakeApplier.applyDelta(oldFile, new ByteArrayInputStream(patchBytes), actualNewBlobOut);
    Assert.assertArrayEquals(expectedNewBytes, actualNewBlobOut.toByteArray());
    Assert.assertFalse(closed.get());
  }

}
