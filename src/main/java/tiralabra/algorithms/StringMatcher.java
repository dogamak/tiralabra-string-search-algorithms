/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms;

import tiralabra.utils.ArrayList;
import tiralabra.utils.RingBuffer;

public abstract class StringMatcher {
  public class Match {
    private int offset;
    private byte[] substring;

    public Match(int offset, byte[] substring) {
      this.offset = offset;
      this.substring = substring;
    }

    public int getOffset() {
      return offset;
    }

    public byte[] getSubstring() {
      return substring;
    }
  }

  private ArrayList<Match> matches = new ArrayList<>();
  private boolean hasFinished = false;

  protected void addMatch(int offset, byte[] substring) {
    matches.add(new Match(offset, substring));
  }

  public Match pollMatch() {
    if (matches.size() == 0)
      return null;

    return matches.remove(0);
  }

  private RingBuffer buffer = new RingBuffer();

  protected void setMinimumBufferSize(int size) {
      if (buffer.capacity() < size) {
        buffer.setCapacity(size);
      }
  }

  public boolean pushByte(byte b) {
    return buffer.pushByte(b);
  }

  public int pushBytes(byte[] bytes) {
      return pushBytes(bytes, 0, bytes.length);
  }

  public int pushBytes(byte[] bytes, int offset, int size) {
      return buffer.pushArray(bytes, offset, size);
  }

  public void pushString(String str) {
    pushBytes(str.getBytes());
  }

  protected RingBuffer getBuffer() {
    return buffer;
  }

  public void process() {}

  public void finish() {
    hasFinished = true;
    process();
  }

  public boolean hasFinished() {
    return hasFinished;
  }
}
