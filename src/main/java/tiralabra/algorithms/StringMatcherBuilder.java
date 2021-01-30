/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms;

public interface StringMatcherBuilder {
  public StringMatcherBuilder addPattern(byte[] bytes);

  default public StringMatcherBuilder addPattern(String str) {
    return this.addPattern(str.getBytes());
  }

 public StringMatcher build();
}
