package tiralabra.utils;

public class RingBuffer {
    /**
     * Maximum capacity of the buffer.
     */
    private int capacity;

    /**
     * Offset of the
     */
    private int head = 0;
    private int size = 0;
    private byte[] buffer;

    public RingBuffer() {
        this(1024);
    }

    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new byte[capacity];
    }

    public int size() {
        return this.size;
    }

    public int capacity() {
        return this.capacity;
    }

    /**
     * Copy buffer's contents sequentially into an array.
     *
     * @param target - Array into which the contents are copied.
     * @return The number of bytes copied.
     */
    private int copySequential(byte[] target) {
        return copySequential(target, target.length);
    }

    /**
     * Copy buffer's contents sequentially into an array.
     *
     * @param target - Array into which the contents are copied.
     * @param bytes - Maximum number of bytes copied.
     * @return The number of bytes copied.
     */
    private int copySequential(byte[] target, int bytes) {
        if (bytes > size)
            bytes = size;

        int seg1_length = head + size;

        if (seg1_length > bytes)
            seg1_length = bytes - head;

        System.arraycopy(buffer, head, target, 0, seg1_length);

        if (head + size > bytes) {
            int seg2_length = head + size - bytes;
            System.arraycopy(buffer, 0, target, seg1_length, seg2_length);
        }

        return bytes;
    }

    /**
     * Allocate a new internal buffer with the specified capacity and copy content's to the new buffer.
     *
     * @param new_capacity - Size of the new internal buffer.
     */
    public void setCapacity(int new_capacity) {
        byte[] new_buffer = new byte[new_capacity];
        copySequential(new_buffer);
        buffer = new_buffer;
        head = 0;
        capacity = new_capacity;
    }

    /**
     * Allocates an array and copies the buffer's contents into it.
     *
     * @return A new array containing the buffer's contents.
     */
    public byte[] toArray() {
        byte[] new_array = new byte[capacity];
        copySequential(new_array);
        return new_array;
    }

    /**
     * Append bytes from an array to the queue.
     *
     * @param source - Source array which is appended to the queue.
     * @return Number of appended bytes.
     */
    public int pushArray(byte[] source) {
        return pushArray(source, 0, source.length);
    }

    /**
     * Append bytes from an array segment to the queue.
     *
     * @param source - Source array from which bytes are copied.
     * @param source_offset - Start offset of the copied segment in {@code source}.
     * @param source_size - Length of the copied segment.
     * @return Number of appended bytes.
     */
    public int pushArray(byte[] source, int source_offset, int source_size) {
        int copy_length = Math.min(capacity - size, source_size);
        int seg1_length = Math.min(buffer.length - head, copy_length);

        System.arraycopy(source, source_offset, buffer, head, seg1_length);
        head = (head + seg1_length) % buffer.length;

        if (seg1_length < source_size) {
            int seg2_length = copy_length - seg1_length;
            System.arraycopy(source, source_offset + seg1_length, buffer, head, seg2_length);
            head = (head + seg1_length) % buffer.length;
        }

        head %= buffer.length;
        size += copy_length;

        return copy_length;
    }

    /**
     * Append a single byte to the queue if the queue is not full.
     *
     * @param b - Byte to append.
     * @return Whether or not the byte was appended.
     */
    public boolean pushByte(byte b) {
        if (size >= capacity)
            return false;

        buffer[head] = b;
        head = (head + 1) % buffer.length;
        size += 1;

        return true;
    }

    /**
     * Returns the byte at the tail of the buffer (index 0, the next byte).
     *
     * @return Tailing byte of the buffer.
     * @throws ArrayIndexOutOfBoundsException if the buffer is empty.
     */
    public byte get() {
        return get(0);
    }

    /**
     * Returns the byte at the specified offset.
     *
     * @param offset - Offset from the tail of the buffer.
     * @return Byte from the specified offset.
     * @throws ArrayIndexOutOfBoundsException if the buffer is smaller than the specified offset.
     */
    public byte get(int offset) {
        if (offset >= capacity)
            throw new ArrayIndexOutOfBoundsException(offset);

        return buffer[(buffer.length + head + offset - size) % buffer.length];
    }

    /**
     * Advances the tail of the buffer by the specified amount, discarding the same amount of bytes and freeing them for use.
     * @param bytes - The number of bytes to free.
     * @throws ArrayIndexOutOfBoundsException if the buffer is smaller than the specified number of bytes.
     */
    public void advance(int bytes) {
        if (size < bytes)
            throw new ArrayIndexOutOfBoundsException(bytes);

        size -= bytes;
    }

    /**
     * Advances the buffer's tail by one byte and returns the freed byte.
     *
     * @return Tailing byte of the buffer.
     */
    public byte pop() {
        byte b = get();
        advance(1);
        return b;
    }
}
