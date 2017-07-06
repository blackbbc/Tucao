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

package com.google.archivepatcher.applier.bsdiff;

import com.google.archivepatcher.applier.PatchFormatException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Tests for {@link BsPatch}.
 */
@RunWith(JUnit4.class)
public class BsPatchTest {

  private static final String SIGNATURE = "ENDSLEY/BSDIFF43";
  private byte[] buffer1;
  private byte[] buffer2;

  /**
   * The tests need access to an actual File object for the "old file", so that it can be used as
   * the argument to a RandomAccessFile constructor... but the old file is a resource loaded at test
   * run-time, potentially from a JAR, and therefore a copy must be made in the filesystem to access
   * via RandomAccessFile. This is not true for the new file or the patch file, both of which are
   * streamable.
   */
  private File oldFile;

  @Before
  public void setUp() throws IOException {
    buffer1 = new byte[6];
    buffer2 = new byte[6];
    try {
      oldFile = File.createTempFile("archive_patcher", "old");
      oldFile.deleteOnExit();
    } catch (IOException e) {
      if (oldFile != null) {
        oldFile.delete();
      }
      throw e;
    }
  }

  @After
  public void tearDown() {
    if (oldFile != null) {
      oldFile.delete();
    }
    oldFile = null;
  }

  @Test
  public void testTransformBytes() throws IOException {
    // In this case the "patch stream" is just a stream of addends that transformBytes(...) will
    // apply to the old data file.
    final byte[] patchInput = "this is a sample string to read".getBytes("US-ASCII");
    final ByteArrayInputStream patchInputStream = new ByteArrayInputStream(patchInput);
    copyToOldFile("bsdifftest_partial_a.txt");
    RandomAccessFile oldData = new RandomAccessFile(oldFile, "r");
    final byte[] expectedNewData = readTestData("bsdifftest_partial_b.bin");
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    BsPatch.transformBytes(patchInput.length, patchInputStream, oldData, newData, buffer1, buffer2);
    byte[] actual = newData.toByteArray();
    Assert.assertArrayEquals(expectedNewData, actual);
  }

  @Test
  public void testTransformBytes_Error_NotEnoughBytes() throws IOException {
    // This test sets up a trivial 1-byte "patch" (addends) stream but then asks
    // transformBytes(...) to apply *2* bytes, which should fail when it hits EOF.
    final InputStream patchIn = new ByteArrayInputStream(new byte[] {(byte) 0x00});
    copyToOldFile("bsdifftest_partial_a.txt"); // Any file would work here
    RandomAccessFile oldData = new RandomAccessFile(oldFile, "r");
    try {
      BsPatch.transformBytes(2, patchIn, oldData, new ByteArrayOutputStream(), buffer1, buffer2);
      Assert.fail("Read past EOF");
    } catch (IOException expected) {
      // Pass
    }
  }

  @Test
  public void testTransformBytes_Error_JunkPatch() throws IOException {
    final byte[] patchInput = "this is a second sample string to read".getBytes("US-ASCII");
    final ByteArrayInputStream patchInputStream = new ByteArrayInputStream(patchInput);
    copyToOldFile("bsdifftest_partial_a.txt"); // Any file would work here
    RandomAccessFile oldData = new RandomAccessFile(oldFile, "r");
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.transformBytes(
          patchInput.length, patchInputStream, oldData, newData, buffer1, buffer2);
      Assert.fail("Should have thrown an IOException");
    } catch (IOException expected) {
      // Pass
    }
  }

  @Test
  public void testTransformBytes_Error_JunkPatch_Underflow() throws IOException {
    final byte[] patchInput = "this is a sample string".getBytes("US-ASCII");
    final ByteArrayInputStream patchInputStream = new ByteArrayInputStream(patchInput);
    copyToOldFile("bsdifftest_partial_a.txt");
    RandomAccessFile oldData = new RandomAccessFile(oldFile, "r");
    final byte[] buffer1 = new byte[6];
    final byte[] buffer2 = new byte[6];

    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.transformBytes(
          patchInput.length + 1, patchInputStream, oldData, newData, buffer1, buffer2);
      Assert.fail("Should have thrown an IOException");
    } catch (IOException expected) {
      // Pass
    }
  }

  @Test
  public void testApplyPatch_ContrivedData() throws Exception {
    invokeApplyPatch(
        "bsdifftest_internal_blob_a.bin",
        "bsdifftest_internal_patch_a_to_b.bin",
        "bsdifftest_internal_blob_b.bin");
  }

  @Test
  public void testApplyPatch_BetterData() throws Exception {
    invokeApplyPatch(
        "bsdifftest_minimal_blob_a.bin",
        "bsdifftest_minimal_patch_a_to_b.bin",
        "bsdifftest_minimal_blob_b.bin");
  }

  @Test
  public void testApplyPatch_BadSignature() throws Exception {
    createEmptyOldFile(10);
    String junkSignature = "WOOOOOO/BSDIFF43"; // Correct length, wrong content
    InputStream patchIn =
        makePatch(
            junkSignature,
            10, // newLength
            10, // diffSegmentLength
            0, // copySegmentLength
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with bad signature");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("bad signature", actual);
    }
  }

  @Test
  public void testApplyPatch_NewLengthNegative() throws Exception {
    createEmptyOldFile(10);
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            -10, // newLength (illegal)
            10, // diffSegmentLength
            0, // copySegmentLength
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with negative newLength");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("bad newSize", actual);
    }
  }

  @Test
  public void testApplyPatch_NewLengthTooLarge() throws Exception {
    createEmptyOldFile(10);
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            Integer.MAX_VALUE + 1, // newLength (max supported is Integer.MAX_VALUE)
            10, // diffSegmentLength
            0, // copySegmentLength
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(
          new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with excessive newLength");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("bad newSize", actual);
    }
  }

  @Test
  public void testApplyPatch_DiffSegmentLengthNegative() throws Exception {
    createEmptyOldFile(10);
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            10, // newLength
            -10, // diffSegmentLength (negative)
            0, // copySegmentLength
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with negative diffSegmentLength");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("bad diffSegmentLength", actual);
    }
  }

  @Test
  public void testApplyPatch_DiffSegmentLengthTooLarge() throws Exception {
    createEmptyOldFile(10);
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            10, // newLength
            Integer.MAX_VALUE + 1, // diffSegmentLength (too big)
            0, // copySegmentLength
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with excessive diffSegmentLength");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("bad diffSegmentLength", actual);
    }
  }

  @Test
  public void testApplyPatch_CopySegmentLengthNegative() throws Exception {
    createEmptyOldFile(10);
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            10, // newLength
            10, // diffSegmentLength
            -10, // copySegmentLength (negative)
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with negative copySegmentLength");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("bad copySegmentLength", actual);
    }
  }

  @Test
  public void testApplyPatch_CopySegmentLengthTooLarge() throws Exception {
    createEmptyOldFile(10);
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            10, // newLength
            0, // diffSegmentLength
            Integer.MAX_VALUE + 1, // copySegmentLength (too big)
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with excessive copySegmentLength");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("bad copySegmentLength", actual);
    }
  }

  // ExpectedFinalNewDataBytesWritten_Negative case is impossible in code, so no need to test
  // that; just the TooLarge condition.
  @Test
  public void testApplyPatch_ExpectedFinalNewDataBytesWritten_PastEOF() throws Exception {
    createEmptyOldFile(10);
    // Make diffSegmentLength + copySegmentLength > newLength
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            10, // newLength
            10, // diffSegmentLength
            1, // copySegmentLength
            0, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch that moves past EOF in new file");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("expectedFinalNewDataBytesWritten too large", actual);
    }
  }

  @Test
  public void testApplyPatch_ExpectedFinalOldDataOffset_Negative() throws Exception {
    createEmptyOldFile(10);
    // Make diffSegmentLength + offsetToNextInput < 0
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            10, // newLength
            10, // diffSegmentLength
            0, // copySegmentLength
            -11, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with that moves to a negative offset in old file");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("expectedFinalOldDataOffset is negative", actual);
    }
  }

  @Test
  public void testApplyPatch_ExpectedFinalOldDataOffset_PastEOF() throws Exception {
    createEmptyOldFile(10);
    // Make diffSegmentLength + offsetToNextInput > oldLength
    InputStream patchIn =
        makePatch(
            SIGNATURE,
            10, // newLength
            10, // diffSegmentLength
            0, // copySegmentLength
            1, // offsetToNextInput
            new byte[10] // addends
            );
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with that moves past EOF in old file");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("expectedFinalOldDataOffset too large", actual);
    }
  }

  @Test
  public void testApplyPatch_TruncatedSignature() throws Exception {
    createEmptyOldFile(10);
    InputStream patchIn = new ByteArrayInputStream("X".getBytes("US-ASCII"));
    ByteArrayOutputStream newData = new ByteArrayOutputStream();
    try {
      BsPatch.applyPatch(new RandomAccessFile(oldFile, "r"), newData, patchIn);
      Assert.fail("Read patch with truncated signature");
    } catch (PatchFormatException expected) {
      // No way to mock the internal logic, so resort to testing exception string for coverage
      String actual = expected.getMessage();
      Assert.assertEquals("truncated signature", actual);
    }
  }

  @Test
  public void testReadBsdiffLong() throws Exception {
    byte[] data = {
      (byte) 0x78,
      (byte) 0x56,
      (byte) 0x34,
      (byte) 0x12,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0xef,
      (byte) 0xbe,
      (byte) 0xad,
      (byte) 0x0e,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0
    };
    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
    long actual = BsPatch.readBsdiffLong(inputStream);
    Assert.assertEquals(0x12345678, actual);
    actual = BsPatch.readBsdiffLong(inputStream);
    Assert.assertEquals(0x0eadbeef, actual);
  }

  @Test
  public void testReadBsdiffLong_Zero() throws Exception {
    long expected = 0x00000000L;
    long actual =
        BsPatch.readBsdiffLong(
            new ByteArrayInputStream(
                new byte[] {
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00
                }));
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testReadBsdiffLong_IntegerMaxValue() throws Exception {
    long expected = 0x7fffffffL;
    long actual =
        BsPatch.readBsdiffLong(
            new ByteArrayInputStream(
                new byte[] {
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0x7f,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00
                }));
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testReadBsdiffLong_IntegerMinValue() throws Exception {
    long expected = -0x80000000L;
    long actual =
        BsPatch.readBsdiffLong(
            new ByteArrayInputStream(
                new byte[] {
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x80,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x00,
                  (byte) 0x80
                }));
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testReadBsdiffLong_LongMaxValue() throws Exception {
    long expected = 0x7fffffffffffffffL;
    long actual =
        BsPatch.readBsdiffLong(
            new ByteArrayInputStream(
                new byte[] {
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0x7f
                }));
    Assert.assertEquals(expected, actual);
  }

  // Can't read Long.MIN_VALUE because the signed-magnitude representation stops at
  // Long.MIN_VALUE+1.
  @Test
  public void testReadBsdiffLong_LongMinValueIsh() throws Exception {
    long expected = -0x7fffffffffffffffL;
    long actual =
        BsPatch.readBsdiffLong(
            new ByteArrayInputStream(
                new byte[] {
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff,
                  (byte) 0xff
                }));
    Assert.assertEquals(expected, actual);
  }

  // This is also Java's Long.MAX_VALUE.
  @Test
  public void testReadBsdiffLong_NegativeZero() throws Exception {
    try {
      BsPatch.readBsdiffLong(
          new ByteArrayInputStream(
              new byte[] {
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x80
              }));
      Assert.fail("Tolerated negative zero");
    } catch (PatchFormatException expected) {
      // Pass
    }
  }

  @Test
  public void testReadFully() throws IOException {
    final byte[] input = "this is a sample string to read".getBytes();
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
    final byte[] dst = new byte[50];

    try {
      BsPatch.readFully(inputStream, dst, 0, 50);
      Assert.fail("Should've thrown an IOException");
    } catch (IOException expected) {
      // Pass
    }

    inputStream.reset();
    BsPatch.readFully(inputStream, dst, 0, input.length);
    Assert.assertTrue(regionEquals(dst, 0, input, 0, input.length));

    inputStream.reset();
    BsPatch.readFully(inputStream, dst, 40, 10);
    Assert.assertTrue(regionEquals(dst, 40, input, 0, 10));

    inputStream.reset();
    try {
      BsPatch.readFully(inputStream, dst, 45, 11);
      Assert.fail("Should've thrown an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException expected) {
      // Pass
    }
  }

  @Test
  public void testPipe() throws IOException {
    final String inputString = "this is a sample string to read";
    final byte[] input = inputString.getBytes("US-ASCII");
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
    final byte[] buffer = new byte[5];
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    BsPatch.pipe(inputStream, outputStream, buffer, 0);
    int actualLength = outputStream.toByteArray().length;
    Assert.assertEquals(0, actualLength);

    inputStream.reset();
    BsPatch.pipe(inputStream, outputStream, buffer, 1);
    actualLength = outputStream.toByteArray().length;
    Assert.assertEquals(1, actualLength);
    byte actualByte = outputStream.toByteArray()[0];
    Assert.assertEquals((byte) 't', actualByte);

    outputStream = new ByteArrayOutputStream();
    inputStream.reset();
    BsPatch.pipe(inputStream, outputStream, buffer, 5);
    actualLength = outputStream.toByteArray().length;
    Assert.assertEquals(5, actualLength);
    String actualOutput = outputStream.toString();
    String expectedOutput = inputString.substring(0, 5);
    Assert.assertEquals(expectedOutput, actualOutput);

    outputStream = new ByteArrayOutputStream();
    inputStream.reset();
    BsPatch.pipe(inputStream, outputStream, buffer, input.length);
    actualLength = outputStream.toByteArray().length;
    Assert.assertEquals(input.length, actualLength);
    expectedOutput = outputStream.toString();
    Assert.assertEquals(inputString, expectedOutput);
  }

  @Test
  public void testPipe_Underrun() {
    int dataLength = 10;
    ByteArrayInputStream in = new ByteArrayInputStream(new byte[dataLength]);
    try {
      // Tell pipe to copy 1 more byte than is actually available
      BsPatch.pipe(in, new ByteArrayOutputStream(), new byte[dataLength], dataLength + 1);
      Assert.fail("Should've thrown an IOException");
    } catch (IOException expected) {
      // Pass
    }
  }

  @Test
  public void testPipe_CopyZeroBytes() throws IOException {
    int dataLength = 0;
    ByteArrayInputStream in = new ByteArrayInputStream(new byte[dataLength]);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BsPatch.pipe(in, out, new byte[100], dataLength);
    int actualLength = out.toByteArray().length;
    Assert.assertEquals(0, actualLength);
  }

  /**
   * Invoke applyPatch(...) and verify that the results are as expected.
   * @param oldPath the path to the old asset in /assets
   * @param patchPatch the path to the patch asset in /assets
   * @param newPath the path to the new asset in /assets
   * @throws IOException if unable to read/write
   * @throws PatchFormatException if the patch is invalid
   */
  private void invokeApplyPatch(String oldPath, String patchPatch, String newPath)
      throws IOException, PatchFormatException {
    copyToOldFile(oldPath);
    RandomAccessFile oldData = new RandomAccessFile(oldFile, "r");
    InputStream patchInputStream = new ByteArrayInputStream(readTestData(patchPatch));
    byte[] expectedNewDataBytes = readTestData(newPath);
    ByteArrayOutputStream actualNewData = new ByteArrayOutputStream();
    BsPatch.applyPatch(oldData, actualNewData, patchInputStream);
    byte[] actualNewDataBytes = actualNewData.toByteArray();
    Assert.assertArrayEquals(expectedNewDataBytes, actualNewDataBytes);
  }

  /**
   * Checks two byte ranges for equivalence.
   *
   * @param data1  first array
   * @param data2  second array
   * @param start1 first byte to compare in |data1|
   * @param start2 first byte to compare in |data2|
   * @param length the number of bytes to compare
   */
  private static boolean regionEquals(
      final byte[] data1,
      final int start1,
      final byte[] data2,
      final int start2,
      final int length) {
    for (int x = 0; x < length; x++) {
      if (data1[x + start1] != data2[x + start2]) {
        return false;
      }
    }
    return true;
  }

  // (Copied from BsDiffTest)
  // Some systems force all text files to end in a newline, which screws up this test.
  private static byte[] stripNewlineIfNecessary(byte[] b) {
    if (b[b.length - 1] != (byte) '\n') {
      return b;
    }

    byte[] ret = new byte[b.length - 1];
    System.arraycopy(b, 0, ret, 0, ret.length);
    return ret;
  }

  // (Copied from BsDiffTest)
  private byte[] readTestData(String testDataFileName) throws IOException {
    InputStream in = getClass().getResourceAsStream("testdata/" + testDataFileName);
    Assert.assertNotNull("test data file doesn't exist: " + testDataFileName, in);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[32768];
    int numRead = 0;
    while ((numRead = in.read(buffer)) >= 0) {
      result.write(buffer, 0, numRead);
    }
    return stripNewlineIfNecessary(result.toByteArray());
  }

  /**
   * Copy the contents of the specified testdata asset into {@link #oldFile}.
   * @param testDataFileName the name of the testdata asset to read
   * @throws IOException if unable to complete the copy
   */
  private void copyToOldFile(String testDataFileName) throws IOException {
    oldFile = File.createTempFile("archive_patcher", "temp");
    Assert.assertNotNull("cant create file!", oldFile);
    byte[] buffer = readTestData(testDataFileName);
    FileOutputStream out = new FileOutputStream(oldFile);
    out.write(buffer);
    out.flush();
    out.close();
  }

  /**
   * Make {@link #oldFile} an empty file (full of binary zeroes) of the specified length.
   * @param desiredLength the desired length in bytes
   * @throws IOException if unable to write the file
   */
  private void createEmptyOldFile(int desiredLength) throws IOException {
    OutputStream out = new FileOutputStream(oldFile);
    for (int x = 0; x < desiredLength; x++) {
      out.write(0);
    }
    out.close();
  }

  /**
   * Create an arbitrary patch that consists of a signature, a length, and a directive sequence.
   * Used to manufacture junk for failure and edge cases.
   * @param signature the signature to use
   * @param newLength the expected length of the "new" file produced by applying the patch
   * @param diffSegmentLength the value to supply as diffSegmentLength
   * @param copySegmentLength the value to supply as copySegmentLength
   * @param offsetToNextInput the value to supply as offsetToNextInput
   * @param addends a byte array of addends; all are written, ignoring |diffSegmentLength|.
   * @return the bytes constituting the patch
   * @throws IOException
   */
  private static InputStream makePatch(
      String signature,
      long newLength,
      long diffSegmentLength,
      long copySegmentLength,
      long offsetToNextInput,
      byte[] addends)
      throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(signature.getBytes("US-ASCII"));
    writeBsdiffLong(newLength, out);
    writeBsdiffLong(diffSegmentLength, out);
    writeBsdiffLong(copySegmentLength, out);
    writeBsdiffLong(offsetToNextInput, out);
    out.write(addends);
    return new ByteArrayInputStream(out.toByteArray());
  }

  // Copied from com.google.archivepatcher.generator.bsdiff.BsUtil for convenience.
  private static void writeBsdiffLong(final long value, OutputStream out) throws IOException {
    long y = value;
    if (y < 0) {
      y = (-y) | (1L << 63);
    }
    for (int i = 0; i < 8; ++i) {
      out.write((byte) (y & 0xff));
      y >>>= 8;
    }
  }
}
