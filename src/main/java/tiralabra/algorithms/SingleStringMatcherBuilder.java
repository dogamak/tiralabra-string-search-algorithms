/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms;

/**
 * Builder for creating a new {@link StringMatcher} capable of matching a single byte pattern. 
 */
public interface SingleStringMatcherBuilder {
  /**
   * Create a new {@link StringMatcher} instance matching the given byte pattern.
   *
   * @param pattern - A byte pattern matched by the created matcher. 
   *
   * @return A new {@link StringMatcher} instance.
   */
  public StringMatcher buildMatcher(byte[] pattern);

  /**
   * Wraps this builder in {@link SingleStringMatcherAdapter} so that algorithms
   * with no special support for multi-pattern matching can be used to match multiple
   * patterns by using multiple separate instances of the same algorithm.
   *
   * @return A {@link StringMatcherBuilder} which is capable of creating multi-pattern
   * matching string matchers using the same algorithm than this builder.
   */
  default StringMatcherBuilder adapt() {
    return new SingleStringMatcherAdapter(this);
  }
}
