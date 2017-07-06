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

import com.google.archivepatcher.applier.DeltaApplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * An implementation of {@link DeltaApplier} that uses {@link BsPatch} to apply a bsdiff patch.
 */
public class BsDiffDeltaApplier implements DeltaApplier {

  @Override
  public void applyDelta(File oldBlob, InputStream deltaIn, OutputStream newBlobOut)
      throws IOException {
    RandomAccessFile oldBlobRaf = null;
    try {
      oldBlobRaf = new RandomAccessFile(oldBlob, "r");
      BsPatch.applyPatch(oldBlobRaf, newBlobOut, deltaIn);
    } finally {
      try {
        oldBlobRaf.close();
      } catch (Exception ignored) {
        // Nothing
      }
    }
  }
}
