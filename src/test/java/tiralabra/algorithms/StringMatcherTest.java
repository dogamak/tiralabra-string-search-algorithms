/**
 * @author : dogamak
 * @created : 2021-02-15
**/

package tiralabra.algorithms;

import tiralabra.algorithms.RabinKarp.RabinKarp;
import tiralabra.algorithms.KnuthMorrisPratt.KnuthMorrisPratt;
import tiralabra.algorithms.BoyerMoore.BoyerMoore;

import tiralabra.algorithms.StringMatcher.Match;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

public class StringMatcherTest {
  @ParameterizedTest
  @MethodSource("getBuilders")
  void testSimpleMatch(StringMatcherBuilder builder) {
    StringMatcher matcher = builder.addPattern("pattern").buildMatcher();

    matcher.pushString("there is a pattern in my soup");
    matcher.finish();

    Match match = matcher.pollMatch();

    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern".getBytes());
    assertEquals(match.getOffset(), 11);

    assertNull(matcher.pollMatch());
  }

  @ParameterizedTest
  @MethodSource("getBuilders")
  void testMultipleMatches(StringMatcherBuilder builder) {
    StringMatcher matcher = builder.addPattern("pattern").buildMatcher();

    matcher.pushString("there is multiple patterns in my pattern soup");
    matcher.finish();

    Match match = matcher.pollMatch();
    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern".getBytes());
    assertEquals(match.getOffset(), 18);

    Match match2 = matcher.pollMatch();
    assertNotNull(match2);
    assertArrayEquals(match2.getSubstring(), "pattern".getBytes());
    assertEquals(match2.getOffset(), 33);

    assertNull(matcher.pollMatch());
  }

  @ParameterizedTest
  @MethodSource("getBuilders")
  void testMatchAtOffsetZero(StringMatcherBuilder builder) {
    StringMatcher matcher = builder.addPattern("pattern").buildMatcher();

    matcher.pushString("pattern is number 0");
    matcher.finish();

    Match match = matcher.pollMatch();
    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern".getBytes());
    assertEquals(match.getOffset(), 0);

    assertNull(matcher.pollMatch());
  }

  @ParameterizedTest
  @MethodSource("getBuilders")
  void testTailingMatch(StringMatcherBuilder builder) {
    StringMatcher matcher = builder.addPattern("pattern").buildMatcher();

    matcher.pushString("there is soup in my pattern");
    matcher.finish();

    Match match = matcher.pollMatch();
    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern".getBytes());
    assertEquals(match.getOffset(), 20);

    assertNull(matcher.pollMatch());
  }

  @ParameterizedTest
  @MethodSource("getBuilders")
  void testPatternOnlyText(StringMatcherBuilder builder) {
    StringMatcher matcher = builder.addPattern("pattern").buildMatcher();

    matcher.pushString("pattern");
    matcher.finish();

    Match match = matcher.pollMatch();
    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern".getBytes());
    assertEquals(match.getOffset(), 0);

    assertNull(matcher.pollMatch());
  }

  @ParameterizedTest
  @MethodSource("getBuilders")
  void testOverlappingPatterns(StringMatcherBuilder builder) {
    StringMatcher matcher = builder.addPattern("pattern pattern").buildMatcher();

    matcher.pushString("pattern of pattern patterns is called pattern pattern pattern");
    matcher.finish();

    Match match = matcher.pollMatch();
    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern pattern".getBytes());
    assertEquals(11, match.getOffset());

    match = matcher.pollMatch();
    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern pattern".getBytes());
    assertEquals(38, match.getOffset());

    match = matcher.pollMatch();
    assertNotNull(match);
    assertArrayEquals(match.getSubstring(), "pattern pattern".getBytes());
    assertEquals(46, match.getOffset());

    assertNull(matcher.pollMatch());
  }

  static StringMatcherBuilder[] getBuilders() {
    return new StringMatcherBuilder[] {
      RabinKarp.getBuilder(),
      KnuthMorrisPratt.getBuilder(),
      BoyerMoore.getBuilder()
    };
  }
}
