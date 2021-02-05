/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.algorithms.RabinKarp;

import java.nio.charset.StandardCharsets;

import tiralabra.utils.HashMap;
import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcher;

public class RabinKarp implements StringMatcher {
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
  private byte[] buffer;
  private int bufferHead = 0;
  private int bufferSize = 0;
  private ArrayList<SuspectedMatchState> suspectedMatches;
  private ArrayList<Match> pendingMatches = new ArrayList<>();

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

    buffer = new byte[bufferCapacity];
    suspectedMatches = new ArrayList<>(substrings.length);
  }

  private void pushBuffer(byte b) {
    bufferHead = (bufferHead + 1) % buffer.length;
    buffer[bufferHead] = b;

    if (bufferSize < buffer.length) {
      bufferSize += 1;
    }
  }

  public void pushByte(byte b) {
    // System.out.println("---");

    hash.pushByte(b);
    pushBuffer(b);
    inputOffset += 1;

    for (int i = 0; i < suspectedMatches.size(); i++) {
      suspectedMatches.get(i).offset += 1;
    }

    ArrayList<byte[]> matches = substringHashes.get(hash.getHash());


    if (matches != null && bufferSize >= windowSize) {
outer:
      for (int i = 0; i < matches.size(); i++) {
        byte[] pattern = matches.get(i);

        for (int j = 0; j < windowSize; j++) {
          byte bufferByte = buffer[(bufferHead + buffer.length - windowSize + j + 1) % buffer.length];

          // System.out.format("%s == %s\n", (char) bufferByte, (char) pattern[j]);

          if (bufferByte != pattern[j])
            continue outer;
        }

        suspectedMatches.add(new SuspectedMatchState(pattern));
      }
    }

outer:
    for (int i = 0; i < suspectedMatches.size(); i++) {
      SuspectedMatchState state = suspectedMatches.get(i);

      // System.out.format("%d %d %d\n", state.offset, state.substring.length, windowSize);

      if (state.offset == state.substring.length - windowSize) {
        suspectedMatches.remove(i);
        i--;

        for (int j = 0; j < state.offset; j++) {
          byte bufferByte = buffer[(bufferHead + 1 + buffer.length - (state.substring.length - windowSize) + j) % buffer.length];

          // System.out.format("%s == %s\n", (char) bufferByte, (char) state.substring[windowSize + j]);

          if (bufferByte != state.substring[windowSize + j]) {
            continue outer;
          }
        }

        pendingMatches.add(new Match(inputOffset - state.substring.length, state.substring));
        // System.out.println("Match: " + new String(state.substring, StandardCharsets.UTF_8));
      }
    }
  }

  public Match pollMatch() {
    if (pendingMatches.size() != 0) {
      return pendingMatches.remove(0);
    }
    
    return null;
  }
}
