/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.algorithms.StringMatcher;

public class RabinKarpBuilder implements StringMatcherBuilder {
  private RollingHashFunctionFactory hashFactory;
  private ArrayList<byte[]> patterns = new ArrayList<>();

  public RabinKarpBuilder() {
    this((n) -> new SimpleModuloHash(n));
  }

  public RabinKarpBuilder(RollingHashFunctionFactory hashFactory) {
    this.hashFactory = hashFactory;
  }

  public StringMatcherBuilder addPattern(byte[] bytes) {
    patterns.add(bytes);

    return this;
  }

  public StringMatcher build() {
    byte[][] array = new byte[patterns.size()][];

    for (int i = 0; i < patterns.size(); i++) {
      array[i] = (byte[]) patterns.get(i);
    }

    return new RabinKarp(array, hashFactory);
  }
}
