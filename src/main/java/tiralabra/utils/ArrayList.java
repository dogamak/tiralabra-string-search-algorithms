/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.utils;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.Arrays;

/**
 * Simple resizeable array implementation.
 */
public class ArrayList<T> implements Iterable<T> {
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

  public Stream<T> getStream() {
    return Arrays.stream(array).map(entry -> (T) entry).limit(size);
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator() {
      private int cursor = 0;

      @Override
      public boolean hasNext() {
        return cursor < size;
      }

      @Override
      public Object next() {
        return array[cursor++];
      }
    };
  }

  /**
   * Removes element from the specified index in O(1) time by swapping it with the last item
   * in the array and truncating the array.
   *
   * @param index - Index of the item to remove.
   * @return The removed item.
   */
  public T swapRemove(int index) {
    T value = (T) array[index];

    array[index] = array[size - 1];
    array[size - 1] = null;
    size--;

    return value;
  }

  /**
   * Filters the contents of the array using a predicate.
   * NOTE: Does not maintain the order of the array.
   *
   * @param predicate - Predicate to decide which elements are kept.
   */
  public void filter(Function<T, Boolean> predicate) {
    int i = 0;

    while (i < size) {
      if (!predicate.apply(get(0))) {
        swapRemove(i);
      } else {
        i++;
      }
    }
  }
}
