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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link TypedRange}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("javadoc")
public class TypedRangeTest {

  @Test
  public void testGetters() {
    String text = "hello";
    TypedRange<String> range = new TypedRange<String>(555, 777, text);
    Assert.assertEquals(555, range.getOffset());
    Assert.assertEquals(777, range.getLength());
    Assert.assertSame(text, range.getMetadata());
  }

  @Test
  public void testToString() {
    // Just make sure this doesn't crash.
    TypedRange<String> range = new TypedRange<String>(555, 777, "woohoo");
    Assert.assertNotNull(range.toString());
    Assert.assertFalse(range.toString().length() == 0);
  }

  @Test
  public void testCompare() {
    TypedRange<String> range1 = new TypedRange<String>(1, 777, null);
    TypedRange<String> range2 = new TypedRange<String>(2, 777, null);
    Assert.assertTrue(range1.compareTo(range2) < 0);
    Assert.assertTrue(range2.compareTo(range1) > 0);
    Assert.assertTrue(range1.compareTo(range1) == 0);
  }

  @Test
  public void testHashCode() {
    TypedRange<String> range1a = new TypedRange<String>(123, 456, "hi mom");
    TypedRange<String> range1b = new TypedRange<String>(123, 456, "hi mom");
    Assert.assertEquals(range1a.hashCode(), range1b.hashCode());
    Set<Integer> hashCodes = new HashSet<Integer>();
    hashCodes.add(range1a.hashCode());
    hashCodes.add(new TypedRange<String>(123 + 1, 456, "hi mom").hashCode()); // offset changed
    hashCodes.add(new TypedRange<String>(123, 456 + 1, "hi mom").hashCode()); // length changed
    hashCodes.add(new TypedRange<String>(123 + 1, 456, "x").hashCode()); // metadata changed
    hashCodes.add(new TypedRange<String>(123 + 1, 456, null).hashCode()); // no metadata at all
    // Assert that all 4 hash codes are unique
    Assert.assertEquals(5, hashCodes.size());
  }

  @Test
  public void testEquals() {
    TypedRange<String> range1a = new TypedRange<String>(123, 456, "hi mom");
    Assert.assertEquals(range1a, range1a); // identity case
    TypedRange<String> range1b = new TypedRange<String>(123, 456, "hi mom");
    Assert.assertEquals(range1a, range1b); // equality case
    Assert.assertNotEquals(range1a, new TypedRange<String>(123 + 1, 456, "hi mom")); // offset
    Assert.assertNotEquals(range1a, new TypedRange<String>(123, 456 + 1, "hi mom")); // length
    Assert.assertNotEquals(range1a, new TypedRange<String>(123, 456, "foo")); // metadata
    Assert.assertNotEquals(range1a, new TypedRange<String>(123, 456, null)); // no metadata
    Assert.assertNotEquals(new TypedRange<String>(123, 456, null), range1a); // other code branch
    Assert.assertEquals(
        new TypedRange<String>(123, 456, null),
        new TypedRange<String>(123, 456, null)); // both with null metadata
    Assert.assertNotEquals(range1a, null); // versus null
    Assert.assertNotEquals(range1a, "space channel 5"); // versus object of different class
  }
}
