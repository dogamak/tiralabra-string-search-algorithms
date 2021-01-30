/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

/**
 * Abstraction over different implementations of rolling hashing functions.
 *
 * A rolling hash function is a hash function which allows to calculate
 * the hash of a shifting window in a string efficiently, by basing each
 * new hash of a shifted window on the hash of the previous window.
 *
 * For example, consider the hash function {@code h(x) = Σx modulo 256}.
 * When using ASCII encoding, we get {@code h("tiralab") = 223}.
 * We can derive the hash {@code h("ralabra")} from this by simply adding
 * the ASCII values for the characters {@code r} and {@code a}, substracting
 * the values for characters {@code t} and {@code i} and taking the result's
 * remainder after dividing by 256 (this size of the alphabet).
 *
 * The hash function described above, while extremely efficient to calculate,
 * is not very useful as collisions are extremely easy to manufacture. For
 * example, the strings {@code transmedia} and {@code damnsatire} have the same
 * hash (as do any other anagrams).
 */
public abstract class RollingHashFunction {
  /**
   * Circular buffer containing the characters in the hash's current window.
   */
  private byte[] window;

  /**
   * Offset to the head of the buffer in {@link #window}.
   */
  private int cursor = 0;

  /**
   * Creates a new instance of the hash function with the specified window size.
   *
   * @param windowSize - Size of the rolling window over which the hash is calculated.
   */
  RollingHashFunction (int windowSize) {
    window = new byte[windowSize];

    for (int i = 0; i < windowSize; i++) {
      window[i] = 0;
    }
  }

  /**
   * Returns the size of the internal window. 
   */
  protected int getWindowSize() {
    return window.length;
  }

  /**
   * Returns the last byte of the internal window.
   *
   * The window is initialized to all zeroes, so this method always
   * returns a zero until the window is fully populated. 
   */
  protected byte getLastByte() {
    return window[(window.length + cursor - 1) % window.length];
  }

  /**
   * Advances the window by pushing a byte to the head of the buffer.
   *
   * Because the buffer is circular and exactly sized to fit the window,
   * this also causes the last byte of the buffer to be discarded if the
   * buffer is full.
   */
  protected void advanceWindow(byte b) {
    window[(window.length + cursor - 1) % window.length] = b;
    cursor += 1;
  }

  /**
   * Advances the window by one byte and updates the rolling hash.
   *
   * @param b - The next byte in the input string.
   */
  public abstract void pushByte(byte b);

  /**
   * Returns the hash value.
   *
   * This method is usually very cheap to call and the heavy lifting is
   * done in the {@link #pushByte}-method.
   */
  public abstract Object getHash();
}
