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
      .build();

    rk.pushString("xasxasd");

    Match match1 = rk.pollMatch();
    Match match2 = rk.pollMatch();

    assertNotNull(match1);
    assertEquals(match1.getOffset(), 1);
    assertArrayEquals(match1.getSubstring(), "asxa".getBytes());

    assertNotNull(match2);
    assertEquals(match2.getOffset(), 2);
    assertArrayEquals(match2.getSubstring(), "sxa".getBytes());

    assertNull(rk.pollMatch());
  }
}
