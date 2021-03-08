package tiralabra.algorithms.AhoCorasick;

import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.utils.ArrayList;
import tiralabra.utils.HashMap;
import tiralabra.utils.Queue;

import java.util.Arrays;

/**
 * Represents a single node in the trie used by the Aho-Corasick algorithm.
 */
class Node {
    /**
     * Path from the trie root to this node, i.e. the prefix this node represents.
     */
    byte[] path;

    /**
     * Children of this node.
     * Value of the edge leading to the child can be determined by inspecting the child's {@link #path}.
     */
    ArrayList<Node> children = new ArrayList<>();

    /**
     * Link to the node which represents the longest possible suffix of this node's path.
     * If no non-trivial suffixes are contained in the trie, points to the trie root.
     */
    Node suffixLink;

    /**
     * Points to an other accepting node with the longest common prefix.
     * Null if no such accepting node exists.
     */
    Node dictionarySuffixLink;

    /**
     * Whether this node is accepting, i.e. if this node represents a complete search pattern.
     */
    boolean accepting = false;

    /**
     * Construct a new non-accepting trie node.
     *
     * @param path Path to this node from the trie root.
     */
    Node(byte[] path) {
        this.path = path;
    }

    /**
     * Determines the child node, to which the provided input byte would cause a transition to.
     *
     * @param b - Input byte.
     *
     * @return The child node with an edge labeled with the input byte, or null if no such node exists.
     */
    Node getChild(byte b) {
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);

            if (child.getEdgeByte() == b)
                return child;
        }

        return null;
    }

    /**
     * Returns the byte which can cause a transition into this node.
     *
     * @return The input byte which can cause the state machine to transition into the state represented by this node.
     */
    byte getEdgeByte() {
        return path[path.length - 1];
    }
}

/**
 * Implementation of the Aho-Corasick string matching algorithm.
 */
public class AhoCorasick extends StringMatcher {
    /**
     * List of all the patterns matched by this instance.
     */
    ArrayList<byte[]> dictionary;

    /**
     * Root node of the trie used for pattern matching.
     */
    Node trieRoot = new Node(new byte[0]);

    /**
     * Current state of the state machine, represented by a node in the trie.
     */
    Node state = trieRoot;

    /**
     * Constructs an instance which matches all of the strings in the provided dictionary.
     *
     * @param dictionary - List of byte string which this instance will match.
     */
    AhoCorasick(ArrayList<byte[]> dictionary) {
        this.dictionary = dictionary;

        constructTrie();
    }

    /**
     * Returns a {@link StringMatcherBuilder} for constructing Aho-Corasick matchers.
     *
     * @return A {@link StringMatcherBuilder} which constructs Aho-Corasick matchers.
     */
    public static StringMatcherBuilder getBuilder() {
        return new StringMatcherBuilder() {
            ArrayList<byte[]> patterns = new ArrayList<>();

            @Override
            public StringMatcherBuilder addPattern(byte[] pattern) {
                patterns.add(pattern);
                return this;
            }

            @Override
            public StringMatcher buildMatcher() {
                return new AhoCorasick(patterns);
            }
        };
    }

    /**
     * Constructs the state machine trie from the pattern dictionary.
     */
    private void constructTrie() {
        // Keep count of the total number of nodes in the trie.
        int node_count = 0;

        // The root (i.e. empty string) is the suffix of itself.
        // The root node is also used as a sentinel in loops traversing the trie.
        trieRoot.suffixLink = trieRoot;

        for (int i = 0; i < dictionary.size(); i++) {
            byte[] pattern = dictionary.get(i);

            Node node = trieRoot;

            // The following loop starts from the the trie root and traverses the path
            // specified by the pattern, creating new nodes if they don't already exist.

            byteLoop:
            for (int j = 0; j < pattern.length; j++) {
                Node child = node.getChild(pattern[j]);

                if (child != null) {
                    node = child;
                    continue byteLoop;
                }

                // A node at path pattern[0..j+1] does not exist, so let's create it.

                byte[] path = Arrays.copyOf(pattern, j+1);
                Node newChild = new Node(path);

                // If this is the end of the current pattern, mark the node to be an accepting state.

                if (j == pattern.length-1) {
                    newChild.accepting = true;
                }

                // Insert the node into the trie and immediately traverse into it.

                node.children.add(newChild);
                node = newChild;

                node_count++;
            }
        }

        // Create a queue with capacity to hold all of our nodes if necessary.
        Queue<Node> stack = new Queue<>(node_count);

        // Start from the root...
        stack.push(trieRoot);

        // ...and traverse down the trie breadth-first.
        while (!stack.empty()) {
            Node node = stack.remove();

            // We could process `node` here...

            for (int i = 0; i < node.children.size(); i++) {
                Node child = node.children.get(i);
                stack.push(child);

                // ...but instead we process it's children here.

                // This way we have the child's parent in scope without any additional book keeping.
                // We can do this because aren't interested in the root node itself, as it cannot have
                // non-trivial suffixes.

                // NOTE: Loop invariant:
                //   Up to this point, we have processed all nodes strictly above this node in the trie.
                //   This means that all those nodes have their `suffixLink` field populated, including `node`,
                //   `child`'s parent.

                // Start from the `child`'s parent...
                Node suffixNode = node.suffixLink;

                // ... and traverse up the trie, following the `suffixLink`s.
                do {

                    // Find the node with the longest common prefix with `node` (the parent) and
                    // a leaving edge with the same label as between `node` and `child`.
                    // The node into which this edge leads to is the `child`'s `suffixLink`.

                    if (child.suffixLink == null) {
                        Node suffixNodeChild = suffixNode.getChild(child.getEdgeByte());

                        if (suffixNodeChild != null && suffixNodeChild != child) {
                            child.suffixLink = suffixNodeChild;
                        }
                    }

                    // Find the accepting node with longest common suffix.
                    // That node is the `child`'s `dictionarySuffixLink`.

                    if (child.dictionarySuffixLink == null) {
                        if (suffixNode.accepting) {
                            child.dictionarySuffixLink = suffixNode;
                        }
                    }

                    // If both `suffixLink` and `dictionarySuffixLink` fields have been populated,
                    // we can break out of this loop.

                    if (child.suffixLink != null && child.dictionarySuffixLink != null)
                        break;

                    // Traverse to the next node following `suffixLink` and terminating
                    // when the root node is reached.
                } while ((suffixNode = suffixNode.suffixLink) != trieRoot);

                // If no non-trivial suffix is found, use the trie root.

                // The trie root represents an empty string and thus is
                // a trivial suffix of every node.

                if (child.suffixLink == null) {
                    child.suffixLink = trieRoot;
                }
            }
        }
    }

    /**
     * Current offset in the input stream.
     */
    private int inputOffset = 0;

    /** {@inheritDoc} */
    @Override
    public int pushBytes(byte[] bytes, int offset, int size) {
        for (int i = 0; i < size; i++) {
            pushByte(bytes[offset + i]);
        }

        return size;
    }

    /**
     * Transition the state machine into the next state.
     *
     * Checks if that state is an accepting state and handles any possible found matches.
     *
     * @param newState - New state for the state machine.
     */
    private void transition(Node newState) {
        state = newState;

        if (state.accepting) {
            // The found matches can be determined by traversing the trie from the accepting
            // state upwards, following the `dictionarySuffixLink` field. All of the nodes on this
            // path are matches.

            Node dictSuffixNode = state;

            while (dictSuffixNode != null) {
                addMatch(inputOffset - dictSuffixNode.path.length + 1, dictSuffixNode.path);
                dictSuffixNode = dictSuffixNode.dictionarySuffixLink;
            }
        }
    }

    /**
     * Processes a single byte of input.
     *
     * @param b - The next input byte.
     *
     * @return This implementation always consumes the input immediately, and so this method returns always true.
     */
    @Override
    public boolean pushByte(byte b) {
        boolean match = false;

        // Traverse the trie, starting from the node representing the current state.

        Node suffixNode = state;

        do {
            Node suffixNodeChild = suffixNode.getChild(b);

            // If there is a valid transition for the current node with the input byte `b`,
            // make that transition and break out of the loop.
            if (suffixNodeChild != null) {
                transition(suffixNodeChild);
                match = true;
                break;
            }

            // If no such transition exists, traverse to the node pointed
            // by `suffixLink` and look for a similar transition there.

            suffixNode = suffixNode.suffixLink;

            // Terminate if the root node is reached.

        } while (suffixNode != trieRoot);

        // If the root node was reached during the above traversal and
        // no valid transition for the input byte `b` was found,
        // default to a transition to the root node.

        if (!match) {
            transition(trieRoot);
        }

        // NOTE: Make sure that the above code does not do early returns,
        //       or else our input offset book keeping falls out of sync.
        inputOffset++;

        return true;
    }
}
