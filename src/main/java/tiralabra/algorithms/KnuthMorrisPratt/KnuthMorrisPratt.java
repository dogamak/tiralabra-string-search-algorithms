/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms.KnuthMorrisPratt;

import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.utils.RingBuffer;

import java.util.Iterator;

/**
 * Implementation of the Knuth-Morris-Pratt string search algorithm.
 */
public class KnuthMorrisPratt extends StringMatcher {
  /**
   * The pattern that is being searched for.
   */
  byte[] pattern;

  /**
   * Look-up-table for the number of bytes we can skip in the pattern when a match fails
   * at a given offset in the pattern.
   */
  int[] skip_table;

  /**
   * Offset of the byte in the pattern against which the next input byte
   * should be compared.
   */
  int pattern_offset = 0;

  /**
   * Bytes processed since the beginning of the input stream.
   */
  int input_offset = 0;

  /**
   * Creates an instance which searches for the given pattern.
   *
   * @param pattern - Byte string to search for.
   */
  KnuthMorrisPratt (byte[] pattern) {
    this.pattern = pattern;

    buildSkipTable();
  }

  /** {@inheritDoc} */
  public static StringMatcherBuilder getBuilder() {
    return new KnuthMorrisPrattBuilder().adapt();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<byte[]> getPatterns() {
    return new Iterator<byte[]>() {
      private boolean consumed = false;

      @Override
      public boolean hasNext() {
        return !consumed;
      }

      @Override
      public byte[] next() {
        return pattern;
      }
    };
  }

  /**
   * Constructs the {@link #skip_table} from the {@link #pattern}.
   */
  void buildSkipTable() {
    skip_table = new int[pattern.length + 1];

    skip_table[0] = -1;

    int next_possible_offset = 0;
    int pattern_index = 1;

    while (pattern_index < pattern.length) {
      skip_table[pattern_index] = next_possible_offset;

      while (next_possible_offset >= 0 && pattern[pattern_index] != pattern[next_possible_offset])
        next_possible_offset = skip_table[next_possible_offset];

      pattern_index++;
      next_possible_offset++;
    }

    skip_table[pattern_index] = next_possible_offset;
  }

  /** {@inheritDoc} */
  @Override
  public boolean pushByte(byte b) {
    processByte(b);

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int pushBytes(byte[] array, int offset, int size) {
    for (int i = offset; i < offset + size; i++)
      processByte(array[i]);

    return size;
  }

  /**
   * Processes a single byte of input.
   *
   * @param b - The next byte in the input stream.
   */
  private void processByte(byte b) {
    // Repeat until we have done everything we can with the current byte.
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
