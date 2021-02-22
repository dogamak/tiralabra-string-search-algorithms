package tiralabra.algorithms.NaiveSearch;

import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.utils.ArrayList;
import tiralabra.utils.RingBuffer;

public class NaiveSearch extends StringMatcher {
    byte[][] patterns;
    int[][] pattern_offsets;
    int[] pattern_offset_counts;

    NaiveSearch(byte[][] patterns) {
        this.patterns = patterns;
        pattern_offsets = new int[patterns.length][];
        pattern_offset_counts = new int[patterns.length];

        for (int i = 0; i < patterns.length; i++) {
            pattern_offsets[i] = new int[patterns[i].length];
            pattern_offset_counts[i] = 1;
        }
    }

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

    private int inputOffset = 0;

    public void process() {
        RingBuffer buffer = getBuffer();

        while (buffer.size() > 0)
            processByte(buffer.pop());
    }

    public void processByte(byte b) {
        for (int i = 0; i < patterns.length; i++) {
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
                        pattern_offsets[i][j] = 0;
                        new_offset_added = true;
                    } else {
                        pattern_offsets[i][j] = pattern_offsets[i][pattern_offset_counts[i] - 1];
                        pattern_offset_counts[i]--;
                    }
                }
            }

            if (!new_offset_added) {
                pattern_offsets[i][pattern_offset_counts[i]] = 0;
                pattern_offset_counts[i]++;
            }
        }

        inputOffset++;
    }
}
