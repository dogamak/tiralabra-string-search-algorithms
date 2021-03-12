/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BitShiftHashTest {
  @Test
  void testPositiveMatch() {
    BitShiftHash h1 = new BitShiftHash(4);
    BitShiftHash h2 = new BitShiftHash(4);

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
    BitShiftHash h1 = new BitShiftHash(4);
    BitShiftHash h2 = new BitShiftHash(4);

    String input1 = "asdasdhlailmao";
    String input2 = "kljiopuvoplamo";

    for (byte b : input1.getBytes())
      h1.pushByte(b);

    for (byte b : input2.getBytes())
      h2.pushByte(b);

    assertNotEquals(h1.getHash(), h2.getHash());
  }
}
