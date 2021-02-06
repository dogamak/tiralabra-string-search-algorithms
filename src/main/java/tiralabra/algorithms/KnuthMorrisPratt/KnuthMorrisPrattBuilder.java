/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms.KnuthMorrisPratt;

import tiralabra.algorithms.SingleStringMatcherBuilder;
import tiralabra.algorithms.StringMatcher;

public class KnuthMorrisPrattBuilder implements SingleStringMatcherBuilder {
  public StringMatcher buildMatcher(byte[] pattern) {
    return new KnuthMorrisPratt(pattern);
  }
}
