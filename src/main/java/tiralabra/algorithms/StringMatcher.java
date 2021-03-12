/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms;

import tiralabra.utils.ArrayList;
import tiralabra.utils.RingBuffer;

import java.util.Iterator;

/**
 * Common abstract base class for all search algorithm implementations.
 */
public abstract class StringMatcher {

  /**
   * Object containing basic information about a found match.
   */
  public class Match {
    /**
     * Location of the beginning of the match specified in bytes from the start of the input stream.
     */
    private int offset;

    /**
     * The pattern which was found in the location specified by {@link #offset}.
     */
    private byte[] substring;

    /**
     * Create an instance.
     *
     * @param offset - Start of the matching substring.
     * @param substring - The pattern which was found.
     */
    public Match(int offset, byte[] substring) {
      this.offset = offset;
      this.substring = substring;
    }

    /**
     * Get offset of the beginning of the found match.
     *
     * @return Offset from the start of the input stream.
     */
    public int getOffset() {
      return offset;
    }

    /**
     * Get the pattern which was found.
     *
     * @return One of the patterns which the string searcher was configured to look for.
     */
    public byte[] getSubstring() {
      return substring;
    }
  }

  /**
   * Array for storing found matches until the user polls for them by using {@link #pollMatch()}.
   */
  private ArrayList<Match> matches = new ArrayList<>();

  /**
   * Whether end of the input stream has been reached.
   */
  private boolean hasFinished = false;

  /**
   * Get an iterator over the patterns this matcher searches for.
   *
   * @return An iterator over the patterns.
   */
  public abstract Iterator<byte[]> getPatterns();

  /**
   * Internal input buffer.
   *
   * TODO: Investigate the need for this, and if needed how it should be used.
   */
  private RingBuffer buffer = new RingBuffer();

  /**
   * Add a new match to the internal match queue, from which the user can poll matches.
   *
   * Used internally by the search algorithm implementations.
   *
   * @param offset - Offset to the beginning of the found match in the input stream.
   * @param substring - The pattern which was found.
   */
  protected void addMatch(int offset, byte[] substring) {
    matches.add(new Match(offset, substring));
  }

  /**
   * Get a new match from the internal match queue and remove it from the queue.
   *
   * @return A {@link Match} object or {@code null} if no new matches have been found.
   */
  public Match pollMatch() {
    if (matches.size() == 0)
      return null;

    return matches.remove(0);
  }

  /**
   * Set the minumum size for the internal input buffer.
   *
   * @param size - Buffer size in bytes.
   */
  protected void setMinimumBufferSize(int size) {
      if (buffer.capacity() < size) {
        buffer.setCapacity(size);
      }
  }

  /**
   * Push a single byte of input to the matcher.
   *
   * The implementation may process the byte immediately, delay it's processing or reject it completely.
   *
   * @param b - Next byte in the input stream.
   * @return {@code true} if the matcher accepts the byte and {@code false} if it cannot accept more input at the moment.
   */
  public boolean pushByte(byte b) {
    return buffer.pushByte(b);
  }

  /**
   * Push an array of bytes to the matcher.
   *
   * The implementation may process the bytes immediately, delay their processing, and/or accept only a part of the array.
   *
   * @param bytes Array of input bytes.
   * @return Number of bytes accepted.
   */
  public int pushBytes(byte[] bytes) {
      return pushBytes(bytes, 0, bytes.length);
  }

  /**
   * Push a segment of bytes from an array to the matcher.
   *
   * The implementation may process the bytes immediately, delay their processing, and/or accept only a part of the array.
   *
   * @param bytes Array containing the input bytes.
   * @param offset Start offset of the segment which is pushed to the matcher.
   * @param size Number of bytes pushed to the matcher.
   * @return Number of bytes accepted by the matcher.
   */
  public int pushBytes(byte[] bytes, int offset, int size) {
      return buffer.pushArray(bytes, offset, size);
  }

  /**
   * Convert a string to bytes using the default string encoding of the local system and push the bytes to the matcher.
   *
   * @param str String to push to the matcher.
   */
  public void pushString(String str) {
    pushBytes(str.getBytes());
  }

  /**
   * Get a reference to the internal input buffer.
   *
   * @return The internal input buffer.
   */
  protected RingBuffer getBuffer() {
    return buffer;
  }

  /**
   * Force the matcher to process whatever input it may hold in it's internal buffers.
   */
  public void process() {}

  /**
   * Signal the matcher that the input stream has reached it's end and it should not expect any more input.
   */
  public void finish() {
    hasFinished = true;
    process();
  }

  /**
   * Check whether this matcher has reached the end of it's input stream.
   *
   * @return {@code true} it the matcher does not expect any more input
   */
  public boolean hasFinished() {
    return hasFinished;
  }
}
