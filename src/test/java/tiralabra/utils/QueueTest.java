package tiralabra.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class QueueTest {
  static int QUEUE_ELEMENT_COUNT = 16;

  @ParameterizedTest
  @ValueSource(ints = {4, 8, 16})
  void testPushAndRemove(int startSize) {
    Queue<Integer> q = new Queue<>(startSize);

    for (int i = 0; i < startSize / 2; i++) {
      q.push(Integer.MAX_VALUE);
      q.remove();
    }

    for (int i = 0; i < QUEUE_ELEMENT_COUNT; i++)
      q.push(i);

    assertEquals(QUEUE_ELEMENT_COUNT, q.size());

    for (int i = 0; i < QUEUE_ELEMENT_COUNT; i++)
      assertEquals(i, q.get(i));

    Iterator<Integer> it = q.iterator();

    for (int i = 0; it.hasNext(); i++) {
      assertTrue(i < QUEUE_ELEMENT_COUNT);
      assertEquals(i, it.next());
    }

    for (int i = 0; i < QUEUE_ELEMENT_COUNT; i++) {
      assertFalse(q.empty());
      assertEquals(i, q.peek());
      assertEquals(i, q.remove());
      assertEquals(QUEUE_ELEMENT_COUNT - 1 - i, q.size());
    }

    assertTrue(q.empty());
  }

  @Test
  void testRemoveOnEmptyQueue() {
    Queue<Integer> q = new Queue<>(10);
    assertNull(q.remove());
  }

  @Test
  void testPeekOnEmptyQueue() {
    Queue<Integer> q = new Queue<>(10);
    assertNull(q.peek());
  }

  @Test
  void testIndexOutOfBoundsGet() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
      Queue<Integer> q = new Queue<>(10);
      q.get(0);
    });
  }
}
