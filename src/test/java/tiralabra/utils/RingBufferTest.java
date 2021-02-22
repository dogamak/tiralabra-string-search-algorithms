package tiralabra.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RingBufferTest {
    @Test
    void canContainTheSpecifiedCapacityWithoutWrapping() {
        RingBuffer buffer = new RingBuffer(8);

        assertEquals(8, buffer.capacity());
        assertEquals(0, buffer.size());

        buffer.pushArray(new byte[] { 1,2,3,4,5,6,7,8 });

        assertEquals(8, buffer.size());

        for (int i = 0; i < 8; i++)
            assertEquals(i + 1, buffer.get(i));
    }

    @Test
    void canContainTheSpecifiedCapacityWithWrapping() {
        RingBuffer buffer = new RingBuffer(8);

        assertEquals(8, buffer.capacity());
        assertEquals(0, buffer.size());

        for (byte i = 10; i < 14; i++) {
            buffer.pushByte(i);
            assertEquals(i, buffer.pop());
        }

        assertEquals(0, buffer.size());

        buffer.pushArray(new byte[] { 1,2,3,4,5,6,7,8 });

        assertEquals(8, buffer.size());

        for (int i = 0; i < 8; i++)
            assertEquals(i + 1, buffer.get(i));
    }

    @Test
    void convertingToArray() {
        RingBuffer buffer = new RingBuffer(8);

        assertEquals(8, buffer.capacity());
        assertEquals(0, buffer.size());

        for (byte i = 0; i < 4; i++) {
            buffer.pushByte(i);
            buffer.pop();
        }

        assertEquals(0, buffer.size());

        buffer.pushArray(new byte[] { 1,2,3,4,5,6,7,8 });

        assertEquals(8, buffer.size());

        for (int i = 0; i < 8; i++)
            assertEquals(i + 1, buffer.get(i));

        assertArrayEquals(buffer.toArray(), new byte[] { 1,2,3,4,5,6,7,8 });
    }
}