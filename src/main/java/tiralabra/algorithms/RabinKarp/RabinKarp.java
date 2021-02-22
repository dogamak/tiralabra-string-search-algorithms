/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import java.nio.charset.StandardCharsets;

import tiralabra.utils.HashMap;
import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.utils.RingBuffer;

public class RabinKarp extends StringMatcher {
  private class SuspectedMatchState {
    int offset = 0;
    byte[] substring;

    SuspectedMatchState(byte[] substring) {
      this.substring = substring;
    }
  }

  private HashMap<Object, ArrayList<byte[]>> substringHashes = new HashMap<>();
  private RollingHashFunctionFactory hashFactory;
  private RollingHashFunction hash;
  private int inputOffset = 0;
  private int windowSize;
  private ArrayList<SuspectedMatchState> suspectedMatches;

  RabinKarp(byte[][] substrings, RollingHashFunctionFactory hashFactory) {
    this.hashFactory = hashFactory;

    windowSize = substrings[0].length;
    int bufferCapacity = substrings[0].length;

    for (int i = 1; i < substrings.length; i++) {
      if (windowSize > substrings[i].length) {
        windowSize = substrings[i].length;
      }

      if (bufferCapacity < substrings[i].length) {
        bufferCapacity = substrings[i].length;
      }
    }

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

  public static RabinKarpBuilder getBuilder() {
    return new RabinKarpBuilder();
  }

  public void process() {
    RingBuffer buffer = getBuffer();

    int cursor = 0;

    while (buffer.size() > 0) {
      inputOffset += 1;

      hash.pushByte(buffer.get(cursor));

      if (cursor >= windowSize) {
        buffer.advance(1);
      } else {
        cursor++;
      }

      for (int i = 0; i < suspectedMatches.size(); i++) {
        suspectedMatches.get(i).offset += 1;
      }

      ArrayList<byte[]> matches = substringHashes.get(hash.getHash());

      if (matches != null && cursor == windowSize) {
        outer:
        for (int i = 0; i < matches.size(); i++) {
          byte[] pattern = matches.get(i);

          for (int j = 0; j < windowSize; j++) {
            if (buffer.get(j) != pattern[j])
              continue outer;
          }

          suspectedMatches.add(new SuspectedMatchState(pattern));
        }
      }

      outer:
      for (int i = 0; i < suspectedMatches.size(); i++) {
        SuspectedMatchState state = suspectedMatches.get(i);

        if (state.offset == state.substring.length - windowSize) {
          suspectedMatches.remove(i);
          i--;

          for (int j = 0; j < state.offset; j++) {
            byte bufferByte = buffer.get(state.substring.length - windowSize + j + 1);

            if (bufferByte != state.substring[windowSize + j]) {
              continue outer;
            }
          }

          addMatch(inputOffset - state.substring.length, state.substring);
        }
      }
    }
  }
}
