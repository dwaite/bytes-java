package com.github.dwaite.bytestring;

import java.nio.ByteOrder;

/**
 * Subclass of {@link ByteSequence} to represent additional mutable operations. Note that this interface
 * still acts as a fixed-length sequence - there are no insert or append operations present.
 */
public interface MutableByteSequence extends ByteSequence {
	
	/**
	 * Overwrite any existing byte in the sequence at the given index with the new value
	 * 
	 * @param index index to overwrite, in the range [0, {@link #length()})
	 * @param value byte value to write
	 * @return this instance
	 * @throws IndexOutOfBoundsException if the index is not in the allowed range.
	 */
	public MutableByteSequence put(int index,
            byte value);
	
	/**
	 * Overwrite two bytes with the given character value.
	 * The multi-byte value is interpreted in {@link #order()}, which by default is network byte order/
	 * {link ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param index index to overwrite, in the range [0, {@link #length()} - {@link Character#BYTES})
	 * @param value value to write
	 * @return this instance
	 * @throws IndexOutOfBoundsException if the index is not in the allowed range.
	 */
    default MutableByteSequence putChar(int index,
            char value) {
	    	if (index < 0 || length() < index + Character.BYTES) {
	    		throw new IndexOutOfBoundsException("index");
	    	}
	    	
	    	put(index, (byte) (value >>> 8));
	    	put(index + 1, (byte) value);
	    	return this;
    }
    
	/**
	 * Overwrite two bytes with the given short value.
	 * The multi-byte value is interpreted in {@link #order()}, which by default is network byte order/
	 * {@link ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param index index to overwrite, in the range [0, {@link #length()} - {@link Short#BYTES})
	 * @param value value to write
	 * @return this instance
	 * @throws IndexOutOfBoundsException if the index is not in the allowed range.
	 */
	default MutableByteSequence putShort(int index,
            short value) {
	    	if (index < 0 || length() < index + Short.BYTES) {
	    		throw new IndexOutOfBoundsException("index");
	    	}
	    	
	    	put(index, (byte) (value >>> 8));
	    	put(index + 1, (byte) value);
	    	return this;
	}
	
	/**
	 * Overwrite four bytes with the given int value.
	 * The multi-byte value is interpreted in {@link #order()}, which by default is network byte order/
	 * {@link ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param index index to overwrite, in the range [0, {@link #length()} - {@link Integer#BYTES})
	 * @param value value to write
	 * @return this instance
	 * @throws IndexOutOfBoundsException if the index is not in the allowed range.
	 */
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
	
	/**
	 * Overwrite eight bytes with the given long value.
	 * The multi-byte value is interpreted in {@link #order()}, which by default is network byte order/
	 * {@link ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param index index to overwrite, in the range [0, {@link #length()} - {@link Long#BYTES})
	 * @param value value to write
	 * @return this instance
	 * @throws IndexOutOfBoundsException if the index is not in the allowed range.
	 */
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
	
	/**
	 * Overwrite four bytes with the given float value.
	 * The multi-byte value is interpreted in {@link #order()}, which by default is network byte order/
	 * {@link ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param index index to overwrite, in the range [0, {@link #length()} - {@link Integer#BYTES})
	 * @param value value to write
	 * @return this instance
	 * @throws IndexOutOfBoundsException if the index is not in the allowed range.
	 */
	default MutableByteSequence putFloat(int index,
            float value) {
		return putInt(index, Float.floatToRawIntBits(value));
	}
	
	/**
	 * Overwrite eight bytes with the given double value.
	 * The multi-byte value is interpreted in {@link #order()}, which by default is network byte order/
	 * {@link ByteOrder#BIG_ENDIAN}.
	 * 
	 * @param index index to overwrite, in the range [0, {@link #length()} - {@link Long#BYTES})
	 * @param value value to write
	 * @return this instance
	 * @throws IndexOutOfBoundsException if the index is not in the allowed range.
	 */
	default MutableByteSequence putDouble(int index,
            double value) {
		return putLong(index, Double.doubleToRawLongBits(value));
	}
	
	/**
	 * Modify the byte order used for get and put operations on this sequence.
	 * @param order ByteOrder, either {@link ByteOrder#BIG_ENDIAN} (the default) or {@link ByteOrder#LITTLE_ENDIAN}
	 * @return this sequence
	 * @throws UnsupportedOperationException if the sequence does not support modifying the byte order
	 */
	default MutableByteSequence order(ByteOrder order) {
		if (order != ByteOrder.BIG_ENDIAN) {
			throw new UnsupportedOperationException();
		}
		return this;
	}
}