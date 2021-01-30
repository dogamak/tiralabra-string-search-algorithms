/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HashMapTest {
  @Test
  void simpleTestCase() {
    HashMap<String, String> map = new HashMap<>();

    map.insert("abc", "123");

    assertEquals(map.get("abc"), "123");
    assertEquals(map.get("cba"), null);
  }

  @Test
  void bigHashmap() {
    HashMap<Integer, Integer> map = new HashMap<>();

    for (int i = 0; i < 1024; i++) {
      map.insert(i, i);
    }

    for (int i = 0; i < 1024; i++) {
      assertEquals(map.get(i), i);
    }

    for (int i = 1024; i < 2048; i++) {
      assertEquals(map.get(i), null);
    }
  }
}
