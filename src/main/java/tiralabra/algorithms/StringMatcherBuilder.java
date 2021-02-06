/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms;

/**
 * Class for building string matchers capable of searching for multiple different
 * patterns simultaneously.
 */
public interface StringMatcherBuilder extends SingleStringMatcherBuilder {
  /**
   * Add a pattern to the set of substrings matched by the created string matcher.
   *
   * @param pattern - Pattern to be matched by the matcher.
   *
   * @return Reference to the builder for method chaining.
   */
  public StringMatcherBuilder addPattern(byte[] pattern);

  /**
   * Constructs a string matcher for matching the patterns
   * defined with {@link #addPattern}.
   *
   * @return A new {@link StringMatcher} instance.
   */
  public StringMatcher buildMatcher();

  /**
   * Constructs a string matcher for matching a single pattern.
   *
   * @param pattern - The pattern the matcher should match.
   *
   * @return A new {@link StringMatcher} instance matching the specified pattern.
   */
  default StringMatcher buildMatcher(byte[] pattern) {
    addPattern(pattern);
    return buildMatcher();
  } 
}
