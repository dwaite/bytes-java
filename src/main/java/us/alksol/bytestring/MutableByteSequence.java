package us.alksol.bytestring;

import java.nio.ByteOrder;

public interface MutableByteSequence extends ByteSequence {
	public MutableByteSequence put(int index,
            byte value);
    default MutableByteSequence putChar(int index,
            char value) {
    	if (index < 0 || length() < index + Character.BYTES) {
    		throw new IndexOutOfBoundsException("index");
    	}
    	
    	put(index, (byte) (value >>> 8));
    	put(index + 1, (byte) value);
    	return this;
    }
	default MutableByteSequence putShort(int index,
            short value) {
    	if (index < 0 || length() < index + Short.BYTES) {
    		throw new IndexOutOfBoundsException("index");
    	}
    	
    	put(index, (byte) (value >>> 8));
    	put(index + 1, (byte) value);
    	return this;
	}
	default MutableByteSequence putInt(int index,
            int value) {
    	if (index < 0 || length() < index + Integer.BYTES) {
    		throw new IndexOutOfBoundsException("index");
    	}
    	
    	put(index, (byte) (value >>> 24));
    	put(index + 1, (byte) (value >>> 16));
    	put(index + 2, (byte) (value >>> 8));
    	put(index + 3, (byte) value);
    	return this;
	}
	default MutableByteSequence putLong(int index,
            long value) {
    	if (index < 0 || length() < index + Integer.BYTES) {
    		throw new IndexOutOfBoundsException("index");
    	}
    	
    	put(index, (byte) (value >>> 56));
    	put(index + 1, (byte) (value >>> 48));
    	put(index + 2, (byte) (value >>> 40));
    	put(index + 3, (byte) (value >>> 32));
    	put(index + 4, (byte) (value >>> 24));
    	put(index + 5, (byte) (value >>> 16));
    	put(index + 6, (byte) (value >>> 8));
    	put(index + 7, (byte) value);
    	return this;
	}
	default MutableByteSequence putFloat(int index,
            float value) {
		return putInt(index, Float.floatToIntBits(value));
	}
	
	default MutableByteSequence putDouble(int index,
            double value) {
		return putLong(index, Double.doubleToLongBits(value));
	}
	
	default MutableByteSequence order(ByteOrder order) {
		if (order != ByteOrder.BIG_ENDIAN) {
			throw new UnsupportedOperationException();
		}
		return this;
	}
}