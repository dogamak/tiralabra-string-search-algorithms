/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcher.Match;

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

  @Test
  void builderTest() {
    StringMatcher rk = new RabinKarpBuilder()
      .addPattern("asxa".getBytes())
      .addPattern("sxa".getBytes())
      .buildMatcher();

    rk.pushString("xasxasd");
    rk.finish();

    Match match1 = rk.pollMatch();
    Match match2 = rk.pollMatch();

    assertNotNull(match1);
    assertEquals(1, match1.getOffset());
    assertArrayEquals(match1.getSubstring(), "asxa".getBytes());

    assertNotNull(match2);
    assertEquals(2, match2.getOffset());
    assertArrayEquals(match2.getSubstring(), "sxa".getBytes());

    assertNull(rk.pollMatch());
  }
}
