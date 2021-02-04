/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms;

public interface MultiStringMatcherBuilder extends SingleStringMatcherBuilder {
  public MultiStringMatcherBuilder addPattern(byte[] pattern);
  public StringMatcher build();

  default StringMatcher build(byte[] pattern) {
    addPattern(pattern);
    return build();
  } 
}
