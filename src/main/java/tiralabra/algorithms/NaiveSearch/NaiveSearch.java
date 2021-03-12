package tiralabra.algorithms.NaiveSearch;

import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.utils.ArrayList;
import tiralabra.utils.RingBuffer;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Implementation of a "naive search" which sequentially checks every byte against every searched pattern.
 */
public class NaiveSearch extends StringMatcher {
    /**
     * List of the patterns this instance is searching for.
     */
    byte[][] patterns;

    /**
     * List of pattern-offset pairs against which the next input byte should be checked.
     *
     * The outermost array is indexed by the pattern's index and the innermost array
     * contains up to {@code pattern.length} different offsets. The inntermost arrays
     * have the same length as their respective patterns, and the actual number of offsets
     * is stored in {@link #pattern_offset_counts}.
     *
     * All patterns have at least one offset in their array at all times.
     */
    int[][] pattern_offsets;

    /**
     * Map from pattern indexes to the number of offsets the pattern has in {@link #pattern_offsets}.
     */
    int[] pattern_offset_counts;

    /**
     * Create a new instance which searches for all of the specified byte sequences.
     *
     * @param patterns List of byte sequences to search for.
     */
    NaiveSearch(byte[][] patterns) {
        this.patterns = patterns;
        pattern_offsets = new int[patterns.length][];
        pattern_offset_counts = new int[patterns.length];

        // Initialize all patterns to have one offset (0) in their array offset array.

        for (int i = 0; i < patterns.length; i++) {
            pattern_offsets[i] = new int[patterns[i].length];
            pattern_offset_counts[i] = 1;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<byte[]> getPatterns() {
        return Arrays.stream(patterns).iterator();
    }

    /** {@inheritDoc} */
    public static StringMatcherBuilder getBuilder() {
        return new StringMatcherBuilder() {
            ArrayList<byte[]> patterns = new ArrayList<>();

            @Override
            public StringMatcherBuilder addPattern(byte[] pattern) {
                patterns.add(pattern);
                return this;
            }

            @Override
            public StringMatcher buildMatcher() {
                byte[][] arr = new byte[patterns.size()][];

                for (int i = 0; i < arr.length; i++) {
                    arr[i] = patterns.get(i);
                }

                return new NaiveSearch(arr);
            }
        };
    }

    /**
     * Current location in the input stream.
     */
    private int inputOffset = 0;

    /**
     * Processes a single byte of input.
     *
     * This implementation processes the byte immediately, and any found matches are immediately available.
     *
     * @param b - Next byte in the input stream.
     * @return Always true.
     */
    public boolean pushByte(byte b) {
        for (int i = 0; i < patterns.length; i++) {
            // With each iteration, a new zero offset is added for each pattern, since we need to consider that
            // every input byte can be a start of a pattern.  This variable keeps track whether the new zero offset
            // has already been inserted into the array.
            boolean new_offset_added = false;

            for (int j = 0; j < pattern_offset_counts[i]; j++) {
                int offset = pattern_offsets[i][j];

                boolean remove_offset = false;

                if (patterns[i][offset] == b) {
                    if (offset == patterns[i].length - 1) {
                        remove_offset = true;
                        addMatch(inputOffset + 1 - patterns[i].length, patterns[i]);
                    } else {
                        pattern_offsets[i][j]++;
                    }
                } else {
                    remove_offset = true;
                }

                if (remove_offset) {
                    if (!new_offset_added) {
                        // If we have yet to insert the new zero offset, we can simply replace the offset-to-be-removed
                        // with a zero.

                        pattern_offsets[i][j] = 0;
                        new_offset_added = true;
                    } else {
                        // Remove the offset by swapping it with the last value in the list and truncating the array.
                        pattern_offsets[i][j] = pattern_offsets[i][pattern_offset_counts[i] - 1];
                        pattern_offset_counts[i]--;
                    }
                }
            }

            if (!new_offset_added) {
                // If there was no opportunity to add the new zero offset during the above loop,
                // we append it to the array here.
                pattern_offsets[i][pattern_offset_counts[i]] = 0;
                pattern_offset_counts[i]++;
            }
        }

        inputOffset++;

        return true;
    }

    /**
     * Process a segment of bytes from an input array.
     *
     * This implementation processes the bytes immediately, and any found matches are immediately available.
     *
     * @param arr Array of input bytes.
     * @param offset Start offset of the segment which is pushed to the matcher.
     * @param size Number of bytes pushed to the matcher.
     * @return Since this implementation always processes all bytes immediately, this method always returns an
     *         integer equal to the provided {@code size} argument.
     */
    public int pushBytes(byte[] arr, int offset, int size) {
      for (int i = offset; i < offset + size; i++)
          pushByte(arr[i]);

      return size;
    }
}
