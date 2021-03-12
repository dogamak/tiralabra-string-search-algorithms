/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import java.util.Arrays;
import java.util.Iterator;

import tiralabra.utils.HashMap;
import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcher;
import tiralabra.utils.RingBuffer;

/**
 * Implementation of the Rabin-Karp string search algorithm.
 */
public class RabinKarp extends StringMatcher {
  /**
   * Class for keeping state about matches, that are only verified up to the
   * current cursor position.
   */
  private class SuspectedMatch {
    /**
     * Location of the start of this match, counting from the start of the input stream.
     */
    int offset = 0;

    /**
     * Pattern which is begin confirmed.
     */
    byte[] substring;

    SuspectedMatch(byte[] substring, int offset) {
      this.substring = substring;
      this.offset = offset;
    }
  }

  /**
   * Map from hashes produced by {@link #hash} to a list of patterns that have a prefix
   * with this hash.
   */
  private HashMap<Object, ArrayList<byte[]>> substringHashes = new HashMap<>();

  /**
   * Factory for creating new instances of the rolling hash function.
   */
  private RollingHashFunctionFactory hashFactory;

  /**
   * An instance of a rolling hash function.
   */
  private RollingHashFunction hash;

  /**
   * Current offset in the input stream in bytes.
   */
  private int inputOffset = 0;

  /**
   * Size of the window (up to the current point in the input stream) which is being hashed.
   *
   * The size of this window is the length of the shortest pattern.
   */
  private int windowSize;

  /**
   * List of matches suspected because a part of the input stream has a same hash
   * than a prefix of a pattern.
   */
  private ArrayList<SuspectedMatch> suspectedMatches;

  /**
   * List of the patterns that are being searched for.
   */
  private byte[][] patterns;

  /**
   * Creates a new instance of the Rabin-Karp algorithm using a provided hashing function.
   *
   * @param substrings - List of byte strings to search for.
   * @param hashFactory - Factory for creating a hash function.
   */
  RabinKarp(byte[][] substrings, RollingHashFunctionFactory hashFactory) {
    this.patterns = substrings;
    this.hashFactory = hashFactory;

    windowSize = Integer.MAX_VALUE;

    for (byte[] substring : substrings) {
      windowSize = Math.min(windowSize, substring.length);
    }

    // Calculate a hash for the windowSize-length prefix of each pattern
    // and store them in the `substringHashes` map.

    for (byte[] substring : substrings) {
      RollingHashFunction hash = hashFactory.create(windowSize);

      for (int i = 0; i < windowSize; i++)
        hash.pushByte(substring[i]);

      ArrayList<byte[]> hashSubstrings = substringHashes.get(hash.getHash());

      if (hashSubstrings == null) {
        hashSubstrings = new ArrayList<>();
        substringHashes.insert(hash.getHash(), hashSubstrings);
      }

      hashSubstrings.add(substring);
    }

    hash = hashFactory.create(windowSize);

    suspectedMatches = new ArrayList<>(substrings.length);
  }

  /** {@inheritDoc} */
  public static RabinKarpBuilder getBuilder() {
    return new RabinKarpBuilder();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<byte[]> getPatterns() {
    return Arrays.stream(patterns).iterator();
  }

  /**
   * Cursor position in the internal buffer.
   */
  private int cursor = 0;

  /** {@inheritDoc} */
  public void process() {
    RingBuffer buffer = getBuffer();

    while (buffer.size() > 0) {
      inputOffset += 1;

      hash.pushByte(buffer.get(cursor));

      // Since we need to be able to verify the hash matches we encounter,
      // we need to keep `windowSize` number of bytes in the buffer "behind"
      // the cursor.

      if (cursor >= windowSize) {
        buffer.advance(1);
      } else {
        cursor++;
      }

      checkForPreliminaryMatches();

      suspectedMatches.filter(this::handleSuspectedMatches);
    }
  }

  /**
   * Check if hash of the rolling window matches a known hash
   * and add an entry to {@link #suspectedMatches} for each
   * possible match associated with that hash.
   */
  private void checkForPreliminaryMatches() {
    RingBuffer buffer = getBuffer();

    ArrayList<byte[]> matches = substringHashes.get(hash.getHash());

    // Hash of the rolling window matches a known hash.
    // If we do not have yet `windowSize` bytes in the buffer,
    // we know that this match must be a false positive.

    if (matches != null && cursor == windowSize) {
      for (byte[] pattern : matches) {
        int i = 0;

        while (i < windowSize && buffer.get(i) == pattern[i]) i++;

        if (i == windowSize) {
          if (pattern.length > windowSize) {
            suspectedMatches.add(new SuspectedMatch(pattern, inputOffset - windowSize));
          } else {
            addMatch(inputOffset - windowSize, pattern);
          }
        }
      }
    }
  }

  /**
   * Checks a suspected match against the byte at the cursor.
   *
   * @param match - A suspected match.
   * @return True if the match need to be checked again, False if the match has been confirmed
   *   to be correct or a false positive.
   */
  private boolean handleSuspectedMatches(SuspectedMatch match) {
    RingBuffer buffer = getBuffer();

    byte buffer_byte = buffer.get(cursor);
    byte pattern_byte = match.substring[inputOffset - match.offset];

    boolean byte_matches = buffer_byte == pattern_byte;
    boolean whole_pattern_checked = inputOffset >= match.offset + match.substring.length - 1;

    if (byte_matches && whole_pattern_checked) {
      addMatch(inputOffset - match.substring.length + 1, match.substring);
    }

    return byte_matches && !whole_pattern_checked;
  }
}
