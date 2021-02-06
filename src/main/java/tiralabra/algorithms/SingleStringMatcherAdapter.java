/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms;

import tiralabra.utils.ArrayList;

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
  public class AdaptedStringMatcher implements StringMatcher {
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
    public void pushByte(byte b) {
      for (int i = 0; i < matchers.size(); i++) {
        StringMatcher matcher = matchers.get(i);
        matcher.pushByte(b);
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
