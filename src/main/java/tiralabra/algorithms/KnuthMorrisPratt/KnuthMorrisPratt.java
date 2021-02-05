/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms.KnuthMorrisPratt;

import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcher.Match;

public class KnuthMorrisPratt implements StringMatcher {
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

  byte[] buffer = new byte[1024];
  int buffer_head = 0;
  int buffer_size = 0;

  ArrayList<Match> matches = new ArrayList<>();

  KnuthMorrisPratt (byte[] pattern) {
    this.pattern = pattern;

    buildSkipTable();
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

  int pushBytes(byte[] bytes, int offset, int count) {
    int buffer_available = buffer.length - buffer_size;
    int bytes_to_buffer = buffer_available < count ? buffer_available : count;

    int continuous_bytes = buffer.length - buffer_head;
    int continuous_bytes_to_copy = continuous_bytes < bytes_to_buffer ? continuous_bytes : bytes_to_buffer;

    System.arraycopy(bytes, offset, buffer, buffer_head, continuous_bytes_to_copy);

    if (continuous_bytes < bytes_to_buffer) {
      System.arraycopy(bytes, offset + continuous_bytes_to_copy, buffer, 0, count - continuous_bytes_to_copy);
    }

    buffer_size += bytes_to_buffer;
    buffer_head = (buffer_head + bytes_to_buffer) % buffer.length;

    if (buffer_size > largest_skip) {
      // process();
    }

    return bytes_to_buffer;
  }

  void finish() {
    // process();
  }

  void advance() {
    pattern_offset += 1;
    input_offset += 1;
    buffer_head = (buffer_head + 1) % buffer.length;
    buffer_size -= 1;
  }

  public void pushByte(byte b) {
    int start_input_offset = input_offset;

    // Repeat until we need the next byte of input
    while (start_input_offset == input_offset) {
      if (pattern[pattern_offset] == b) {
        advance();

        if (pattern_offset == pattern.length) {
          pattern_offset = skip_table[pattern_offset];
          matches.add(new Match(input_offset - pattern.length, pattern));
        }
      } else {
        pattern_offset = skip_table[pattern_offset];

        if (pattern_offset < 0) {
          advance();
        }
      }
    }
  }

  public Match pollMatch() {
    if (matches.size() > 0) {
      return matches.remove(0);
    } else {
      return null;
    }
  }
}
