/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms;

import tiralabra.utils.ArrayList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Adapter which allows multi-pattern matching string matchers to be created
 * for algorithms which only provide a {@link SingleStringMatcherBuilder} implementation.
 *
 * The created {@link StringMatcher} wraps a separate instance of the underlying
 * algorithm for each of the matcher patterns.
 */
public class SingleStringMatcherAdapter implements StringMatcherBuilder {
  /**
   * The adapted builder.
   */
  SingleStringMatcherBuilder builder;

  /**
   * List of the patterns for which a matcher instance is to created.
   */
  ArrayList<byte[]> patterns = new ArrayList<>();

  /**
   * String matcher class whose instances are returned by the adapting builder.  
   */
  public class AdaptedStringMatcher extends StringMatcher {
    /**
     * List of the wrapped single-pattern matchers. One for each of the matched patterns.
     */
    ArrayList<StringMatcher> matchers = new ArrayList<>(patterns.size());


    /**
     * Initialize the matcher by building the wrapped matchers from the list of patterns.
     */
    AdaptedStringMatcher() {
      for (int i = 0; i < patterns.size(); i++) {
        matchers.add(builder.buildMatcher(patterns.get(i)));
      }
    }

    @Override
    public Iterator<byte[]> getPatterns() {
      return new Iterator<byte[]>() {
        private int matcher_index = -1;
        private Iterator<byte[]> matcher_iterator = null;

        @Override
        public boolean hasNext() {
          if (matcher_iterator == null || !matcher_iterator.hasNext()) {
            matcher_index++;

            if (matcher_index > matchers.size()) {
              return false;
            }

            matcher_iterator = matchers.get(matcher_index).getPatterns();
          }

          return matcher_iterator.hasNext();
        }

        @Override
        public byte[] next() {
          if (matcher_iterator == null || !matcher_iterator.hasNext()) {
            matcher_index++;

            if (matcher_index > matchers.size()) {
              throw new NoSuchElementException();
            }

            matcher_iterator = matchers.get(matcher_index).getPatterns();
          }

          return matcher_iterator.next();
        }
      };
    }

    /**
     * {@inheritDoc}
     */
    public Match pollMatch() {
      for (int i = 0; i < matchers.size(); i++) {
        StringMatcher matcher = matchers.get(i);
        Match match = matcher.pollMatch();

        if (match != null) {
          return match;
        }
      }

      return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean pushByte(byte b) {
      for (int i = 0; i < matchers.size(); i++) {
        StringMatcher matcher = matchers.get(i);

        while (!matcher.pushByte(b))
          matcher.process();
      }

      return true;
    }

    /** {@inheritDoc} */
    public int pushBytes(byte[] source, int offset, int size) {
      int[] consumed = new int[matchers.size()];
      boolean[] finished = new boolean[matchers.size()];
      int most_consumed = 0;
      int i = 0;
      int finished_matchers = 0;

      while (finished_matchers < matchers.size()) {
        i = (i + 1) % matchers.size();
        StringMatcher matcher = matchers.get(i);

        int matcher_consumed = consumed[i];
        boolean first_iteration = matcher_consumed == 0;

        if (matcher_consumed >= most_consumed && !first_iteration) {
          if (!finished[i]) {
            finished_matchers++;
            finished[i] = true;
          }

          continue;
        }

        int bytes = matcher.pushBytes(source, offset + matcher_consumed, size - matcher_consumed);
        consumed[i] += bytes;

        if (bytes == 0)
          matcher.process();

        if (first_iteration && bytes > most_consumed)
            most_consumed = bytes;
      }

      return most_consumed;
    }

    /** {@inheritDoc} */
    public int pushBytes(byte[] source) {
      return pushBytes(source, 0, source.length);
    }

    /** {@inheritDoc} */
    public void finish() {
      for (int i = 0; i < matchers.size(); i++) {
        matchers.get(i).finish();
      }
    }

    /** {@inheritDoc} */
    public void process() {
      for (int i = 0; i < matchers.size(); i++) {
        matchers.get(i).process();
      }
    }
  }

  /**
   * Create a new {@link StringMatcherBuilder} from a {@link SingleStringMatcherBuilder}.
   */
  public SingleStringMatcherAdapter(SingleStringMatcherBuilder builder) {
    this.builder = builder;
  }

  /**
   * {@inheritDoc}
   */
  public StringMatcherBuilder addPattern(byte[] pattern) {
    patterns.add(pattern);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public StringMatcher buildMatcher() {
    return new AdaptedStringMatcher();
  }
}
