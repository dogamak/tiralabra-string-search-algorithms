package tiralabra.utils;

import java.util.Iterator;

/**
 * A simple FIFO queue implemented using a growable ring buffer.
 *
 * @param <T> Type of the items in the queue.
 */
public class Queue<T> implements Iterable<T> {
    /**
     * The internal ring buffer in which the items are stored.
     */
    T[] array;

    /**
     * Offset in {@link #array} to which the next item is placed.
     * Points to one cell past the last inserted item, wrapping at the end of the buffer.
     */
    int head = 0;

    /**
     * Number of items contained in the queue. Can be used in combination
     * with {@link #head} to determine the index of the oldest item in the
     * queue.
     */
    int size = 0;

    /**
     * Create a queue with specified initial capacity.
     *
     * @param size Initial capacity of the created queue.
     */
    public Queue(int size) {
        array = (T[]) new Object[size];
    }

    /**
     * Add an item to the queue.
     *
     * @param item Item to add
     */
    public void push(T item) {
        if (size == array.length) {
            resize(array.length * 2);
        }

        array[head] = item;
        head = (head + 1) % array.length;
        size++;
    }

    private void copySequential(T[] target) {
        int seg1_length = Math.min(array.length - head, size);

        System.arraycopy(array, head, target, 0, seg1_length);

        if (seg1_length < size) {
          int seg2_length = size - (array.length - head);
          System.arraycopy(array, 0, target, seg1_length, seg2_length);
        }
    }

    private void resize(int new_size) {
      if (new_size <= size) {
        return;
      }

      T[] new_array = (T[]) new Object[new_size];

      copySequential(new_array);

      array = new_array;
      head = size;
    }

    /**
     * Get the number of elements in the queue.
     *
     * @return Number of elements in the queue.
     */
    public int size() {
        return size;
    }

    /**
     * Returns whether the queue is empty.
     *
     * @return True if the queue contains no items and false otherwise.
     */
    public boolean empty() {
        return size == 0;
    }

    /**
     * Removes and returns the oldest item in the queue.
     *
     * @return The removed element.
     */
    public T remove() {
        if (size == 0)
            return null;

        T removed = array[(head + array.length - size) % array.length];

        // Replace the removed element with null to make debugging easier.
        // This also helps the garbage collector, but in our case this has
        // no effect, as our only Queues are short lived.
        array[(head + array.length - size) % array.length] = null;

        size--;

        return removed;
    }

    /**
     * Returns the next item in the queue, without removing it.
     *
     * @return The next item in the queue, or null if the queue is empty.
     */
    public T peek() {
        if (size == 0)
            return null;

        return array[(head + array.length - size) % array.length];
    }

    public T get(int index) {
      if (index >= size || index < 0) {
          throw new ArrayIndexOutOfBoundsException(
            String.format("Index %d is out of bounds (Queue size %d)", index, size)
          );
      }

      return array[(head + array.length + index - size) % array.length];
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator() {
            private int offset = 0;

            @Override
            public boolean hasNext() {
                return size > offset;
            }

            @Override
            public Object next() {
                return get(offset++);
            }
        };
    }
}
