/**
 * @author : dogamak
 * @created : 2021-02-15
**/

package tiralabra.algorithms.BoyerMoore;

import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.algorithms.SingleStringMatcherBuilder;
import tiralabra.utils.ArrayList;
import tiralabra.utils.RingBuffer;

import java.util.Iterator;

public class BoyerMoore extends StringMatcher {
  private ArrayList[] bad_character_table = new ArrayList[256];
  private int[] good_suffix_table;
  private int[] full_shift_table;
  private byte[] pattern;

  public BoyerMoore(byte[] pattern) {
    this.pattern = pattern;
    this.backbuffer_size = pattern.length - 1;
    // this.cursor = pattern.length - 1;

    preprocess_bad_character_table();
    preprocess_good_suffix_table();
    preprocess_full_shift_table();
  }

  public static StringMatcherBuilder getBuilder() {
    return new SingleStringMatcherBuilder() {
      public StringMatcher buildMatcher(byte[] pattern) {
        return new BoyerMoore(pattern);
      }
    }.adapt();
  }

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
        consumed = true;
        return pattern;
      }
    };
  }

  /**
   * Calculates length of the longest common prefix of substrings at offsets
   * {@code offset1} and {@code offset1} from offset {@code start} in
   * {@code arr} in direction {@code direction}.
   *
   * @param arr - Array from which to search for the common substrings. 
   * @param offset1 - Offset in relation to {@code start} in direction {@code direction}.
   *   Specifies the starting position of first of the considered substrings. 
   * @param offset2 - Offset in relation to {@code start} in direction {@code direction}.
   *   Specifies the starting position of latter of the considered substrings.
   * @param direction - Specifies the direction of the search in {@code arr}.
   *   Value {@code true} means searching staring from low indexes and advancing towards high indexes.
   *   Value {@code false} means the opposite.
   *
   * @return Length of the longest common prefix.
   */
  private int substring_common_prefix_length(
    byte[] arr,
    int offset1,
    int offset2,
    boolean direction
  ) {
    if (offset1 == offset2) {
      if (direction) {
        return arr.length - offset1;
      } else {
        return offset1;
      }
    }

    int max_length = direction
      ? offset1 < offset2
        ? arr.length - offset2
        : arr.length - offset1
      : offset1 > offset2
        ? offset2
        : offset1;

    for (int i = 0; i < max_length; i += 1) {
      int d = direction ? i : -i;

      if (arr[offset1 + d] != arr[offset2 + d]) {
        return i;
      }
    }

    return max_length;
  }

  private int[] preprocess_prefix_lut(boolean direction) {
    int step = direction ? 1 : -1;
    int start = direction ? 0 : pattern.length - 1;

    int[] prefix_lut;

    if (pattern.length == 0) {
      prefix_lut = new int[] {};
      return prefix_lut;
    }
  
    if (pattern.length == 1) {
      prefix_lut = new int[] { pattern[0] };
      return prefix_lut;
    }

    prefix_lut = new int[pattern.length];

    for (int i = 0; i < prefix_lut.length; i++) {
      prefix_lut[i] = 0;
    }

    prefix_lut[0] = pattern.length;
    prefix_lut[1] = substring_common_prefix_length(pattern, start, start + step, direction);

    // Start of a substring which contains `pattern[i]` and is a prefix of `pattern`. 
    int z_box_start = 0;

    // End of the substring starting from `l` and containing `pattern[i]` which
    // is also a prefix of `pattern`.
    int z_box_end = 0;

    for (int i = 2; i < pattern.length; i++) {
      if (i > z_box_end) {
        prefix_lut[i] = substring_common_prefix_length(pattern, start, start + i * step, direction);

        if (prefix_lut[i] > 0) {
          z_box_start = i;
          z_box_end = i + prefix_lut[i] - 1;
        }
      } else {
        int z_box_offset = i - z_box_start;
        int prefix_length = prefix_lut[z_box_offset];
        int z_box_remaining = z_box_end - i + 1;

        if (prefix_length < z_box_remaining) {
          prefix_lut[i] = prefix_length;
        } else {
          prefix_lut[i] = z_box_remaining +
            substring_common_prefix_length(pattern, start + prefix_length * step, start + (z_box_end + 1) * step, direction);
          z_box_start = i;
          z_box_end = i + prefix_lut[i] - 1;
        }
      }
    }

    return prefix_lut;
  }

  private void preprocess_bad_character_table() {
    if (pattern.length == 0) {
      return;
    }

    int[] alpha = new int[256];

    for (int i = 0; i < 256; i++) {
      ArrayList<Integer> arr = new ArrayList<>(1);
      arr.add(-1);
      bad_character_table[i] = arr;

      alpha[i] = -1;
    }

    for (int i = 0; i < pattern.length; i++) {
      byte b = pattern[i];

      alpha[b] = i;

      for (int j = 0; j < 256; j++) {
        bad_character_table[j].add(alpha[j]);
      }
    }
  }

  private void preprocess_good_suffix_table() {
    good_suffix_table = new int[pattern.length];
    int[] suffix_lut = preprocess_prefix_lut(false);

    for (int i = 0; i < good_suffix_table.length; i++) {
      good_suffix_table[i] = -1;
    }

    for (int i = 0; i < pattern.length - 1; i++) {
      int j = pattern.length - suffix_lut[suffix_lut.length - i - 1];

      if (j != pattern.length) {
        good_suffix_table[j] = i;
      }
    }
  }

  private void preprocess_full_shift_table() {
    full_shift_table = new int[pattern.length];

    for (int i = 0; i < full_shift_table.length; i++) {
      full_shift_table[i] = 0;
    }

    int[] prefix_lut = preprocess_prefix_lut(true);

    int longest = 0;

    for (int i = 0; i < prefix_lut.length; i++) {
      int zv = prefix_lut[prefix_lut.length - i - 1];

      if (zv == i + 1 && longest < zv) {
        longest = zv;
      }

      full_shift_table[full_shift_table.length - i - 1] = longest;
    }
  }

  /**
   * The number of bytes we keep around in the buffer after processing them.
   */
  private int backbuffer_size;

  /**
   * Cursor in the input stream, indexed from the start of input and not from the start of the buffer.
   */
  private int cursor = 0;

  /**
   * Start of the buffer expressed in bytes since the start of input.
   */
  private int buffer_start = 0;

  public void process() {
    RingBuffer buffer = getBuffer();

    while (buffer.size() > backbuffer_size + cursor - buffer_start) {
      int drop_bytes = Math.max(0, cursor - buffer_start - backbuffer_size);
      buffer.advance(drop_bytes);
      buffer_start += drop_bytes;

      int pattern_offset = pattern.length - 1;
      int buffer_offset = backbuffer_size + cursor - buffer_start;

      while (pattern_offset >= 0 && pattern[pattern_offset] == (buffer.get(buffer_offset) & 0xFF)) {
        pattern_offset -= 1;
        buffer_offset -= 1;
      }

      if (pattern_offset == -1) {
        addMatch(buffer_start + buffer_offset + 1, pattern);
        cursor += pattern.length == 0 ? 1 : pattern.length - full_shift_table[1];
      } else {
        int char_shift = pattern_offset - (int) bad_character_table[buffer.get(buffer_offset) & 0xFF].get(pattern_offset);
        int suffix_shift = 0;

        if (pattern_offset + 1 == pattern.length) {
          suffix_shift = 1;
        } else if (good_suffix_table[pattern_offset + 1] == -1) {
          suffix_shift = pattern.length - full_shift_table[pattern_offset + 1];
        } else {
          suffix_shift = pattern.length - 1 - good_suffix_table[pattern_offset + 1];
        }

        cursor += Math.max(char_shift, suffix_shift);
      }
    }
  }
}
