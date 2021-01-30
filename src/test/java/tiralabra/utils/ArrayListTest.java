/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.utils;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayListTest {
  @Test
  void testAdd() {
    ArrayList<String> array = new ArrayList<>(2);

    array.add("1");
    array.add("2");
    array.add("3");

    assertEquals(array.size(), 3);
    assertEquals(array.capacity(), 4);

    assertEquals(array.get(0), "1");
    assertEquals(array.get(1), "2");
    assertEquals(array.get(2), "3");
  }

  @Test
  void testRemove() {
    ArrayList<String> array = new ArrayList<>(2);

    array.add("1");
    array.add("2");
    array.add("3");
    array.add("4");

    array.remove(1);

    assertEquals(array.size(), 3);
    assertEquals(array.capacity(), 4);

    assertEquals(array.get(0), "1");
    assertEquals(array.get(1), "3");
    assertEquals(array.get(2), "4");
  }
}
