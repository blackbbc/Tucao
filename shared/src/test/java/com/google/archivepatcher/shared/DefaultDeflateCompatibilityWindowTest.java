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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link DefaultDeflateCompatibilityWindow}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class DefaultDeflateCompatibilityWindowTest {

  private DefaultDeflateCompatibilityWindow window = null;

  private final JreDeflateParameters brokenParameters = JreDeflateParameters.of(1, 0, true);

  /**
   * Trivial subclass for testing, always fails compatibility checks.
   */
  private class BrokenCompatibilityWindow extends DefaultDeflateCompatibilityWindow {
    @Override
    public Map<JreDeflateParameters, String> getBaselineValues() {
      // Superclass's version is immutable, so make a copy.
      Map<JreDeflateParameters, String> result =
          new HashMap<JreDeflateParameters, String>(super.getBaselineValues());
      result.put(brokenParameters, "foo");
      return result;
    }
  }

  private class InfallibleCompatibilityWindow extends DefaultDeflateCompatibilityWindow {
    @Override
    public Map<JreDeflateParameters, String> getBaselineValues() {
      // Using the system values for the baseline means the baseline will always match :)
      return getSystemValues();
    }
  }

  @Before
  public void setUp() {
    window = new DefaultDeflateCompatibilityWindow();
  }

  @Test
  public void testGetBaselineValues() {
    // Basic sanity test: ensure it doesn't crash and isn't null, and contains all the values that
    // we care about.
    ensureHasAllKeys(window.getBaselineValues());
  }

  @Test
  public void testGetSystemValues() {
    // Basic sanity test: ensure it doesn't crash and isn't null, and contains all the values that
    // we care about.
    ensureHasAllKeys(window.getSystemValues());
  }

  private void ensureHasAllKeys(Map<JreDeflateParameters, String> mappings) {
    for (int level = 1; level <= 9; level++) {
      for (int strategy = 0; strategy <= 1; strategy++) {
        Assert.assertTrue(mappings.containsKey(JreDeflateParameters.of(level, strategy, true)));
        Assert.assertTrue(mappings.containsKey(JreDeflateParameters.of(level, strategy, false)));
      }
    }
    // Manually scan for presence of the strategy-2 values, only set for compression level 1....
    Assert.assertTrue(mappings.containsKey(JreDeflateParameters.of(1, 2, true)));
    Assert.assertTrue(mappings.containsKey(JreDeflateParameters.of(1, 2, false)));
    Assert.assertEquals(mappings.size(), 38);
  }

  @Test
  public void testGetCorpus() {
    // Basic sanity test: ensure it's a non-null, non-empty return.
    byte[] corpus1 = window.getCorpus();
    Assert.assertNotNull(corpus1);
    Assert.assertTrue(corpus1.length > 0);
    // Basic sanity test: ensure the corpus is distinct each time the method is called (i.e., the
    // mutable object returned is independent of the actual corpus).
    byte[] corpus2 = window.getCorpus();
    Assert.assertArrayEquals(corpus1, corpus2);
    Assert.assertNotSame(corpus1, corpus2);
  }

  @Test
  public void testIsCompatible() {
    // First do a coverage-only call, as it's not safe to assume compatibility in the unit test.
    window.isCompatible();
    // Now do a call that is guaranteed to fail.
    BrokenCompatibilityWindow broken = new BrokenCompatibilityWindow();
    Assert.assertFalse(broken.isCompatible());
    // Now do a call that is guaranteed to succeed.
    InfallibleCompatibilityWindow infallible = new InfallibleCompatibilityWindow();
    Assert.assertTrue(infallible.isCompatible());
  }

  @Test
  public void testGetIncompatibleValues() {
    // First do a coverage-only call, as it's not safe to assume compatibility in the unit test.
    Assert.assertNotNull(window.getIncompatibleValues());
    // Now do a call that is guaranteed to produce failure data.
    BrokenCompatibilityWindow brokenWindow = new BrokenCompatibilityWindow();
    Map<JreDeflateParameters, String> incompatible = brokenWindow.getIncompatibleValues();
    Assert.assertTrue(incompatible.containsKey(brokenParameters));
    // Now do a call that is guaranteed to produce no failure data.
    InfallibleCompatibilityWindow infallible = new InfallibleCompatibilityWindow();
    Assert.assertTrue(infallible.getIncompatibleValues().isEmpty());
  }
}
