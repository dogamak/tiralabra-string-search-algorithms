/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RabinKarpTest {
  @Test
  void basicTest() {
    byte[][] patterns = new byte[][] {
      "asxa".getBytes(),
      "sxa".getBytes(),
    };

    RabinKarp rk = new RabinKarp(patterns, (n) -> new SimpleModuloHash(n));

    for (byte b : "xasxasd".getBytes())
      rk.pushByte(b);
  }
}
