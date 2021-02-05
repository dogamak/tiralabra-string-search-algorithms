/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.algorithms;

import tiralabra.utils.ArrayList;

public class SingleStringMatcherAdapter implements MultiStringMatcherBuilder {
  SingleStringMatcherBuilder builder;
  ArrayList<byte[]> patterns = new ArrayList<>();

  public class AdaptedStringMatcher implements StringMatcher {
    ArrayList<StringMatcher> matchers = new ArrayList<>(patterns.size());

    AdaptedStringMatcher() {
      for (int i = 0; i < patterns.size(); i++) {
        matchers.add(builder.build(patterns.get(i)));
      }
    }

    public Match pollMatch() {
      for (int i = 0; i < matchers.size(); i++) {
        StringMatcher matcher = matchers.get(i);
        Match match = matcher.pollMatch();

        if (match != null) {
          return match;
        }
      }

      return null;
    }

    public void pushByte(byte b) {
      for (int i = 0; i < matchers.size(); i++) {
        StringMatcher matcher = matchers.get(i);
        matcher.pushByte(b);
      }
    }
  }

  public SingleStringMatcherAdapter(SingleStringMatcherBuilder builder) {
    this.builder = builder;
  }

  public MultiStringMatcherBuilder addPattern(byte[] pattern) {
    patterns.add(pattern);
    return this;
  }

  public StringMatcher build() {
    return new AdaptedStringMatcher();
  }
}
