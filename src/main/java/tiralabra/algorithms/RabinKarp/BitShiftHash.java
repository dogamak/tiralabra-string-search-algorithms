package tiralabra.algorithms.RabinKarp;

public class BitShiftHash extends RollingHashFunction {
    private int hash = 0;

    /** {@inheritDoc} */
    public BitShiftHash(int windowSize) {
        super(windowSize);
    }

    @Override
    public void pushByte(byte b) {
        hash = (hash << 1) - (getLastByte() << getWindowSize()) + b;
    }

    @Override
    public Object getHash() {
        return Integer.toUnsignedLong(hash);
    }
}
