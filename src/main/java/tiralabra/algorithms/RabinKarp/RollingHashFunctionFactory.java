/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

public interface RollingHashFunctionFactory {
  public RollingHashFunction create(int windowSize);
}
