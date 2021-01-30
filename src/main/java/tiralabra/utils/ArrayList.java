/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.utils;

/**
 * Simple resizeable array implementation.
 */
public class ArrayList<T> {
  /**
   * Internal array allocated for the entries.
   *
   * Probably contains unused entries at the end.
   */
  private Object[] array;

  /**
   * Number of entries used in {@link #array}.
   *
   * All of the used entries are quaranteed to be at the start of {@link #array}.
   */
  private int size = 0;

  /**
   * Create an ArrayList instance with the specified initial capacity.
   *
   * @param capacity - Initial size of the internal array.
   */
  public ArrayList(int capacity) {
    array = new Object[capacity];

    for (int i = 0; i < capacity; i++)
      array[i] = null;
  }

  /**
   * Create a default-sized ArrayList instance.
   */
  public ArrayList() {
    this(128);
  }

  /**
   * Resizes the internal array to the specified size.
   */
  public void resize(int capacity) {
    Object[] newArray = new Object[capacity];

    System.arraycopy(array, 0, newArray, 0, this.size);

    this.array = newArray;
  }

  /**
   * Returns true if the internal array is fully occupied.
   */
  public boolean isFull() {
    return size == array.length;
  }

  /**
   * Add a value to the array.
   *
   * Allocates more space if neccessary.
   */
  public void add(T value) {
    if (isFull())
      resize(array.length * 2);

    array[size] = value;
    size += 1;
  }

  /**
   * Returns the number of values in this array.
   */
  public int size() {
    return size;
  }

  /**
   * Removes and returns a value from the specified index.
   *
   * Shifts the values after the specified index towards the start
   * of the array by one index.
   */
  public T remove(int index) {
    T removed = (T) array[index];

    System.arraycopy(array, index+1, array, index, size - index - 1);
    size -= 1;

    return removed;
  }

  /**
   * Clears the array.
   *
   * Does not affect the array's capacity.
   */
  public void clear() {
    for (int i = 0; i < size; i++)
      array[i] = null;

    size = 0;
  }

  /**
   * Returns the value in the specified index.
   */
  public T get(int index) {
    return (T) array[index];
  }

  /**
   * Returns the space allocated in the internal array.
   */
  public int capacity() {
    return array.length;
  }

  public T[] toArray() {
    Object[] newArray = new Object[size];

    System.arraycopy(array, 0, newArray, 0, size);

    return (T[]) newArray;
  }
}
