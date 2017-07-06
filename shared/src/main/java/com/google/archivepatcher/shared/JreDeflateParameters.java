// Copyright 2015 Google Inc. All rights reserved.
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
 * Encapsulates JRE-compatible deflate parameters. There are a total of 54 possible values, which
 * correspond to 9 levels, 3 strategies and 2 wrapping choices (i.e., 9 x 3 x 2). In practice only
 * two of these are frequently encountered: (level 6, strategy 0, nowrap) and (level 9, strategy 0,
 * nowrap); these correspond to the default and maximum-compression settings found in almost all zip
 * tools.
 */
public enum JreDeflateParameters {
  LEVEL1_STRATEGY0_NOWRAP(1, 0, true),
  LEVEL2_STRATEGY0_NOWRAP(2, 0, true),
  LEVEL3_STRATEGY0_NOWRAP(3, 0, true),
  LEVEL4_STRATEGY0_NOWRAP(4, 0, true),
  LEVEL5_STRATEGY0_NOWRAP(5, 0, true),
  LEVEL6_STRATEGY0_NOWRAP(6, 0, true), // Default for almost all zip tools.
  LEVEL7_STRATEGY0_NOWRAP(7, 0, true),
  LEVEL8_STRATEGY0_NOWRAP(8, 0, true),
  LEVEL9_STRATEGY0_NOWRAP(9, 0, true), // Also frequently encountered.
  LEVEL1_STRATEGY1_NOWRAP(1, 1, true),
  LEVEL2_STRATEGY1_NOWRAP(2, 1, true),
  LEVEL3_STRATEGY1_NOWRAP(3, 1, true),
  LEVEL4_STRATEGY1_NOWRAP(4, 1, true),
  LEVEL5_STRATEGY1_NOWRAP(5, 1, true),
  LEVEL6_STRATEGY1_NOWRAP(6, 1, true),
  LEVEL7_STRATEGY1_NOWRAP(7, 1, true),
  LEVEL8_STRATEGY1_NOWRAP(8, 1, true),
  LEVEL9_STRATEGY1_NOWRAP(9, 1, true),
  LEVEL1_STRATEGY2_NOWRAP(1, 2, true),
  LEVEL2_STRATEGY2_NOWRAP(2, 2, true),
  LEVEL3_STRATEGY2_NOWRAP(3, 2, true),
  LEVEL4_STRATEGY2_NOWRAP(4, 2, true),
  LEVEL5_STRATEGY2_NOWRAP(5, 2, true),
  LEVEL6_STRATEGY2_NOWRAP(6, 2, true),
  LEVEL7_STRATEGY2_NOWRAP(7, 2, true),
  LEVEL8_STRATEGY2_NOWRAP(8, 2, true),
  LEVEL9_STRATEGY2_NOWRAP(9, 2, true),
  LEVEL1_STRATEGY0_WRAP(1, 0, false),
  LEVEL2_STRATEGY0_WRAP(2, 0, false),
  LEVEL3_STRATEGY0_WRAP(3, 0, false),
  LEVEL4_STRATEGY0_WRAP(4, 0, false),
  LEVEL5_STRATEGY0_WRAP(5, 0, false),
  LEVEL6_STRATEGY0_WRAP(6, 0, false),
  LEVEL7_STRATEGY0_WRAP(7, 0, false),
  LEVEL8_STRATEGY0_WRAP(8, 0, false),
  LEVEL9_STRATEGY0_WRAP(9, 0, false),
  LEVEL1_STRATEGY1_WRAP(1, 1, false),
  LEVEL2_STRATEGY1_WRAP(2, 1, false),
  LEVEL3_STRATEGY1_WRAP(3, 1, false),
  LEVEL4_STRATEGY1_WRAP(4, 1, false),
  LEVEL5_STRATEGY1_WRAP(5, 1, false),
  LEVEL6_STRATEGY1_WRAP(6, 1, false),
  LEVEL7_STRATEGY1_WRAP(7, 1, false),
  LEVEL8_STRATEGY1_WRAP(8, 1, false),
  LEVEL9_STRATEGY1_WRAP(9, 1, false),
  LEVEL1_STRATEGY2_WRAP(1, 2, false),
  LEVEL2_STRATEGY2_WRAP(2, 2, false),
  LEVEL3_STRATEGY2_WRAP(3, 2, false),
  LEVEL4_STRATEGY2_WRAP(4, 2, false),
  LEVEL5_STRATEGY2_WRAP(5, 2, false),
  LEVEL6_STRATEGY2_WRAP(6, 2, false),
  LEVEL7_STRATEGY2_WRAP(7, 2, false),
  LEVEL8_STRATEGY2_WRAP(8, 2, false),
  LEVEL9_STRATEGY2_WRAP(9, 2, false);

  /**
   * The level of the deflate compressor.
   */
  public final int level;

  /**
   * The strategy used by the deflate compressor.
   */
  public final int strategy;

  /**
   * Whether or not nowrap is enabled for the deflate compressor.
   */
  public final boolean nowrap;

  /**
   * Creates a new parameters object having the specified configuration.
   * @param level the level for the deflate compressor
   * @param strategy the strategy for the deflate compressor
   * @param nowrap whether or not nowrap is enabled for the deflate compressor
   */
  private JreDeflateParameters(int level, int strategy, boolean nowrap) {
    if (level < 1 || level > 9 || strategy < 0 || strategy > 2) {
      throw new IllegalArgumentException("Only levels 1-9 and strategies 0-2 are valid.");
    }
    this.level = level;
    this.strategy = strategy;
    this.nowrap = nowrap;
  }

  public static JreDeflateParameters of(int level, int strategy, boolean nowrap) {
    int id = (level * 100) + (strategy * 10) + (nowrap ? 1 : 0);
    switch (id) {
      case 100:
        return LEVEL1_STRATEGY0_WRAP;
      case 200:
        return LEVEL2_STRATEGY0_WRAP;
      case 300:
        return LEVEL3_STRATEGY0_WRAP;
      case 400:
        return LEVEL4_STRATEGY0_WRAP;
      case 500:
        return LEVEL5_STRATEGY0_WRAP;
      case 600:
        return LEVEL6_STRATEGY0_WRAP;
      case 700:
        return LEVEL7_STRATEGY0_WRAP;
      case 800:
        return LEVEL8_STRATEGY0_WRAP;
      case 900:
        return LEVEL9_STRATEGY0_WRAP;
      case 110:
        return LEVEL1_STRATEGY1_WRAP;
      case 210:
        return LEVEL2_STRATEGY1_WRAP;
      case 310:
        return LEVEL3_STRATEGY1_WRAP;
      case 410:
        return LEVEL4_STRATEGY1_WRAP;
      case 510:
        return LEVEL5_STRATEGY1_WRAP;
      case 610:
        return LEVEL6_STRATEGY1_WRAP;
      case 710:
        return LEVEL7_STRATEGY1_WRAP;
      case 810:
        return LEVEL8_STRATEGY1_WRAP;
      case 910:
        return LEVEL9_STRATEGY1_WRAP;
      case 120:
        return LEVEL1_STRATEGY2_WRAP;
      case 220:
        return LEVEL2_STRATEGY2_WRAP;
      case 320:
        return LEVEL3_STRATEGY2_WRAP;
      case 420:
        return LEVEL4_STRATEGY2_WRAP;
      case 520:
        return LEVEL5_STRATEGY2_WRAP;
      case 620:
        return LEVEL6_STRATEGY2_WRAP;
      case 720:
        return LEVEL7_STRATEGY2_WRAP;
      case 820:
        return LEVEL8_STRATEGY2_WRAP;
      case 920:
        return LEVEL9_STRATEGY2_WRAP;
      case 101:
        return LEVEL1_STRATEGY0_NOWRAP;
      case 201:
        return LEVEL2_STRATEGY0_NOWRAP;
      case 301:
        return LEVEL3_STRATEGY0_NOWRAP;
      case 401:
        return LEVEL4_STRATEGY0_NOWRAP;
      case 501:
        return LEVEL5_STRATEGY0_NOWRAP;
      case 601:
        return LEVEL6_STRATEGY0_NOWRAP;
      case 701:
        return LEVEL7_STRATEGY0_NOWRAP;
      case 801:
        return LEVEL8_STRATEGY0_NOWRAP;
      case 901:
        return LEVEL9_STRATEGY0_NOWRAP;
      case 111:
        return LEVEL1_STRATEGY1_NOWRAP;
      case 211:
        return LEVEL2_STRATEGY1_NOWRAP;
      case 311:
        return LEVEL3_STRATEGY1_NOWRAP;
      case 411:
        return LEVEL4_STRATEGY1_NOWRAP;
      case 511:
        return LEVEL5_STRATEGY1_NOWRAP;
      case 611:
        return LEVEL6_STRATEGY1_NOWRAP;
      case 711:
        return LEVEL7_STRATEGY1_NOWRAP;
      case 811:
        return LEVEL8_STRATEGY1_NOWRAP;
      case 911:
        return LEVEL9_STRATEGY1_NOWRAP;
      case 121:
        return LEVEL1_STRATEGY2_NOWRAP;
      case 221:
        return LEVEL2_STRATEGY2_NOWRAP;
      case 321:
        return LEVEL3_STRATEGY2_NOWRAP;
      case 421:
        return LEVEL4_STRATEGY2_NOWRAP;
      case 521:
        return LEVEL5_STRATEGY2_NOWRAP;
      case 621:
        return LEVEL6_STRATEGY2_NOWRAP;
      case 721:
        return LEVEL7_STRATEGY2_NOWRAP;
      case 821:
        return LEVEL8_STRATEGY2_NOWRAP;
      case 921:
        return LEVEL9_STRATEGY2_NOWRAP;
      default:
        throw new IllegalArgumentException("No such parameters");
    }
  }

  @Override
  public String toString() {
    return "level=" + level + ",strategy=" + strategy + ",nowrap=" + nowrap;
  }

  /**
   * Given an input string formatted like the output of {@link #toString()}, parse the string into
   * an instance of this class.
   * @param input the input string to parse
   * @return an equivalent object of this class
   */
  public static JreDeflateParameters parseString(String input) {
    String[] parts = input.split(",");
    return of(
        Integer.parseInt(parts[0].split("=")[1]),
        Integer.parseInt(parts[1].split("=")[1]),
        Boolean.parseBoolean(parts[2].split("=")[1]));
  }
}
