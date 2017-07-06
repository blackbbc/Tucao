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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface to be implemented by delta appliers.
 */
public interface DeltaApplier {
  /**
   * Applies a delta from deltaIn to oldBlob and writes the result to newBlobOut.
   *
   * @param oldBlob the old blob
   * @param deltaIn the delta to apply to the oldBlob
   * @param newBlobOut the stream to write the result to
   * @throws IOException in the event of an I/O error reading the input or writing the output
   */
  public void applyDelta(File oldBlob, InputStream deltaIn, OutputStream newBlobOut)
      throws IOException;
}
