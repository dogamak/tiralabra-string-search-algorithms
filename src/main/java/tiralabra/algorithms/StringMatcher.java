/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms;

public interface StringMatcher {
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

  public Match pollMatch();
  public void pushByte(byte b);

  default public void finish() {}

  default public void pushBytes(byte[] bytes) {
    for (byte b : bytes)
      pushByte(b);
  }

  default public void pushString(String str) {
    pushBytes(str.getBytes());
  }
}
