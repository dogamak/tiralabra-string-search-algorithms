/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms.KnuthMorrisPratt;

import tiralabra.algorithms.StringMatcher.Match;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KnuthMorrisPrattTest {
  @Test
  void simple() {
    KnuthMorrisPratt kmp = new KnuthMorrisPratt("ABCAB".getBytes());

    for (byte ch : "ABABCABCABCX".getBytes()) {
      kmp.pushByte(ch);
    }
    
    Match m1 = kmp.pollMatch();
    Match m2 = kmp.pollMatch();
    Match m3 = kmp.pollMatch();

    assertNotNull(m1);
    assertEquals(m1.getOffset(), 2);
    assertArrayEquals(m1.getSubstring(), "ABCAB".getBytes());

    assertNotNull(m2);
    assertEquals(m2.getOffset(), 5);
    assertArrayEquals(m2.getSubstring(), "ABCAB".getBytes());

    assertNull(m3);
  }
}
