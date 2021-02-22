/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms.KnuthMorrisPratt;

import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcher.Match;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.utils.RingBuffer;

public class KnuthMorrisPratt extends StringMatcher {
  byte[] pattern;
  int[] skip_table;

  int largest_skip;

  /**
   * Offset of the byte in the pattern against which the next input byte
   * should be compared.
   */
  int pattern_offset = 0;

  /**
   * Bytes processed since the beginnig of the input stream.
   */
  int input_offset = 0;
  ArrayList<Match> matches = new ArrayList<>();

  KnuthMorrisPratt (byte[] pattern) {
    this.pattern = pattern;

    buildSkipTable();
  }

  public static StringMatcherBuilder getBuilder() {
    return new KnuthMorrisPrattBuilder().adapt();
  }

  void buildSkipTable() {
    skip_table = new int[pattern.length + 1];

    skip_table[0] = -1;
    largest_skip = 0;

    int next_possible_offset = 0;
    int pattern_index = 1;

    while (pattern_index < pattern.length) {
      skip_table[pattern_index] = next_possible_offset;

      if (pattern[next_possible_offset] != pattern[pattern_index]) {
        while (next_possible_offset >= 0 && pattern[pattern_index] != pattern[next_possible_offset])
          next_possible_offset = skip_table[next_possible_offset];
      }

      if (skip_table[pattern_index] > largest_skip)
        largest_skip = skip_table[pattern_index];

      pattern_index++;
      next_possible_offset++;
    }

    skip_table[pattern_index] = next_possible_offset;
  }

  public void process() {
    RingBuffer buffer = getBuffer();

    while (buffer.size() > 0)
        processByte(buffer.pop());
  }

  public void processByte(byte b) {
    // Repeat until we need the next byte of input
    while (true) {
      if (pattern[pattern_offset] == b) {
        pattern_offset++;
        input_offset++;

        if (pattern_offset == pattern.length) {
          pattern_offset = skip_table[pattern_offset];
          addMatch(input_offset - pattern.length, pattern);
        }

        break;
      } else {
        pattern_offset = skip_table[pattern_offset];

        if (pattern_offset < 0) {
          pattern_offset++;
          input_offset++;
          break;
        }
      }
    }
  }
}
