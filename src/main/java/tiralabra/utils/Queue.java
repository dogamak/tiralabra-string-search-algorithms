package tiralabra.utils;

/**
 * A simple FIFO queue implemented using a fixed size ring buffer.
 *
 * @param <T> Type of the items in the queue.
 */
public class Queue<T> {
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
     * Create a queue with the specified fixed capacity.
     *
     * @param size Fixed capacity of the created queue.
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
        array[head] = item;
        head = (head + 1) % array.length;
        size++;
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
}
