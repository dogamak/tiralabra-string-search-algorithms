/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

/**
 * Implements a simple modulo-based rolling hash function.
 *
 * This is the hash function described by the Wikipedia article
 * on the Rabin-Karp algorithm.
 *
 * The function can be described as {@code h(x) = (x₀kⁿ⁻¹ + x₁kⁿ⁻² + ⋯ + xₙ₋₁) modulo p},
 * where {@code k} is the size of the alphabet, {@code n} the size of the
 * window and {@code p} a constant prime. Thus we can update the hash by substracting
 * {@code xkⁿ⁻¹} (where {@code x} is the character leaving the window), adding the value
 * of the character entering the window and taking modulo {@code p}.
 *
 */
public class SimpleModuloHash extends RollingHashFunction {
  /**
   * The hash of the current window. 
   */
  private int hash = 0;

  /**
   * Constant prime used as the modulo.
   */
  private static int modulo = 101;

  SimpleModuloHash (int windowSize) {
    super(windowSize);
  }

  /**
   * Calculates the power {@code power} of {@code base} while taking modulo on every iteration
   * in order to avoid overflowing the integer value.
   */
  private int powerModulo(int base, int power) {
    int p = 1;

    for (int i = 0; i < power; i++)
      p = (p * base) % modulo;

    return p;
  }

  /**
   * {@inheritDoc}
   */
  public void pushByte(byte b) {
    int prevHash = hash;
    int lastByte = getLastByte();

    int p = powerModulo(256, getWindowSize() - 1);

    hash = ((hash + modulo - (getLastByte() * p) % modulo) * 256 + b) % modulo;

    advanceWindow(b);
  }

  /**
   * {@inheritDoc}
   */
  public Object getHash() {
    return hash;
  }
}
