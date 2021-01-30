/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleModuloHashTest {
  @Test
  void testPositiveMatch() {
    SimpleModuloHash h1 = new SimpleModuloHash(4);
    SimpleModuloHash h2 = new SimpleModuloHash(4);

    String input1 = "asdasdhlailmao";
    String input2 = "kljiopuvoplmao";

    for (byte b : input1.getBytes())
      h1.pushByte(b);

    for (byte b : input2.getBytes())
      h2.pushByte(b);

    assertEquals(h1.getHash(), h2.getHash());
  }

  @Test
  void testNegativeMatch() {
    SimpleModuloHash h1 = new SimpleModuloHash(4);
    SimpleModuloHash h2 = new SimpleModuloHash(4);

    String input1 = "asdasdhlailmao";
    String input2 = "kljiopuvoplamo";

    for (byte b : input1.getBytes())
      h1.pushByte(b);

    for (byte b : input2.getBytes())
      h2.pushByte(b);

    assertNotEquals(h1.getHash(), h2.getHash());
  }
}
