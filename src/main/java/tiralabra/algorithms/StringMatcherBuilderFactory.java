/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms;

import tiralabra.algorithms.RabinKarp.RabinKarp;
import tiralabra.algorithms.KnuthMorrisPratt.KnuthMorrisPratt;

/**
 * Factory for constructing a {@link StringMatcherBuilder}.
 */
public interface StringMatcherBuilderFactory {
  /**
   * Create a new {@link StringMatcherBuilder} instance.
   */
  public StringMatcherBuilder createBuilder(); 

  /**
   * Returns a list of factories for creating a builder for each
   * of the string searching algorithms currently implemented.
   */
  public static StringMatcherBuilderFactory[] getFactories() {
    return new StringMatcherBuilderFactory[] {
      RabinKarp::getBuilder,
      KnuthMorrisPratt::getBuilder,
    };
  }
}
