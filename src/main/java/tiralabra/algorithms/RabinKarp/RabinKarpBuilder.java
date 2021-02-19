/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.algorithms.StringMatcher;

public class RabinKarpBuilder implements StringMatcherBuilder {
  private RollingHashFunctionFactory hashFactory = SimpleModuloHash::new;
  private ArrayList<byte[]> patterns = new ArrayList<>();

  public RabinKarpBuilder setHashFunction(RollingHashFunctionFactory factory) {
    this.hashFactory = factory;
    return this;
  }

  public StringMatcherBuilder addPattern(byte[] bytes) {
    patterns.add(bytes);

    return this;
  }

  public StringMatcher buildMatcher() {
    byte[][] array = new byte[patterns.size()][];

    for (int i = 0; i < patterns.size(); i++) {
      array[i] = patterns.get(i);
    }

    return new RabinKarp(array, hashFactory);
  }
}
