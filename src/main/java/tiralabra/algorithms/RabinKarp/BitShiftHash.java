package tiralabra.algorithms.RabinKarp;

/**
 * Simple but efficient rolling hash function implemented by using bit shifting.
 */
public class BitShiftHash extends RollingHashFunction {
    private int hash = 0;

    /** {@inheritDoc} */
    public BitShiftHash(int windowSize) {
        super(windowSize);
    }

    @Override
    public void pushByte(byte b) {
        int rotated = Integer.rotateLeft(hash, 1);
        int old_rotated = Integer.rotateLeft(getLastByte(), getWindowSize());
        hash = rotated ^ old_rotated ^ b;
        advanceWindow(b);
    }

    @Override
    public Object getHash() {
        return Integer.toUnsignedLong(hash);
    }
}
