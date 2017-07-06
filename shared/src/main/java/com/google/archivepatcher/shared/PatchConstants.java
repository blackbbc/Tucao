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
 * Constants used in reading and writing patches.
 */
public class PatchConstants {
  /**
   * The identifier that begins all patches of this type.
   */
  public static final String IDENTIFIER = "GFbFv1_0"; // Google File-by-File v1.0

  /**
   * All available compatibility windows. The {@link #patchValue} field specifies the value for
   * each constant as represented in a patch file.
   */
  public static enum CompatibilityWindowId {
    /**
     * The {@link com.google.archivepatcher.shared.DefaultDeflateCompatibilityWindow}.
     */
    DEFAULT_DEFLATE((byte) 0);

    /**
     * The representation of this enumerated constant in patch files.
     */
    public final byte patchValue;

    /**
     * Construct a new enumerated constant with the specified value in patch files.
     */
    private CompatibilityWindowId(byte patchValue) {
      this.patchValue = patchValue;
    }

    /**
     * Parse a patch value and return the corresponding enumerated constant.
     * @param patchValue the patch value to parse
     * @return the corresponding enumerated constant, null if unmatched
     */
    public static CompatibilityWindowId fromPatchValue(byte patchValue) {
      switch (patchValue) {
        case 0:
          return DEFAULT_DEFLATE;
        default:
          return null;
      }
    }
  }

  /**
   * All available delta formats. The {@link #patchValue} field specifies the value for each
   * constant as represented in a patch file.
   */
  public static enum DeltaFormat {
    /**
     * The bsdiff delta format.
     */
    BSDIFF((byte) 0);

    /**
     * The representation of this enumerated constant in patch files.
     */
    public final byte patchValue;

    /**
     * Construct a new enumerated constant with the specified value in patch files.
     */
    private DeltaFormat(byte patchValue) {
      this.patchValue = patchValue;
    }

    /**
     * Parse a patch value and return the corresponding enumerated constant.
     * @param patchValue the patch value to parse
     * @return the corresponding enumerated constant, null if unmatched
     */
    public static DeltaFormat fromPatchValue(byte patchValue) {
      switch (patchValue) {
        case 0:
          return BSDIFF;
        default:
          return null;
      }
    }
  }
}
