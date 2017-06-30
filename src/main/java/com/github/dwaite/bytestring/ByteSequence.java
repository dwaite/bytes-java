package com.github.dwaite.bytestring;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.github.dwaite.bytestring.impl.ImmutableBytesSpliterator;

/**
 * Common interface for byte sequences. This interface does not support mutation, with mutation operations
 * being present on the child interface {@link MutableByteSequence}.
 */
public interface ByteSequence extends Comparable<ByteSequence> {
	/**
	 * Invalid index reported when a search operation like {@link #indexOf(ByteSequence)} does not find a
	 * match.
	 */
	public static final int NOT_FOUND = -1;

	/**
	 * Helper method to verify that the common array/offset/length parameters are valid.
	 * @param input byte array, required to be non-null
	 * @param offset offset within byte array, must be in the range [0, input.length)
	 * @param length length after offset of bytes, must be in the range [0, input.length-offset]
	 * 
	 * @throws NullPointerException byte array passed `input` is `null`
	 * @throws IndexOutOfBoundsException `offset` or `length` are invalid values
	 */
	public static void assertCorrectByteOffsetLength(byte[] input, int offset, int length) 
			throws NullPointerException, IndexOutOfBoundsException {
		Objects.requireNonNull(input);
		if (offset < 0 || offset >= input.length) {
			throw new IndexOutOfBoundsException("offset");
		}
		if (length < 0 || offset + length > input.length) {
			throw new IndexOutOfBoundsException("length");
		}
	}

	/**
	 * Return the byte at a given index
	 * @param index index value in the range [0, {@link #length()}]
	 * 
	 * @return byte at the given index
	 * @throws IndexOutOfBoundsException index is out of bounds 
	 */
	byte get(int index) throws IndexOutOfBoundsException;

	/**
	 * Return a ByteSequence which is a subsequence of this object.
	 * 
	 * @param start starting offset, in the range [0, {@link #length()}]
	 * @param end ending offset (not `length`), in range [`start`, {@link #length()}]
	 * 
	 * @return new ByteSequence containing the subsequence
	 * @throws IndexOutOfBoundsException if indexes are invalid
	 */
	ByteSequence subSequence(int start, int end) throws IndexOutOfBoundsException;

	/**
	 * Return a bytes instance for this sequence. This is useful to get a guaranteed immutable sequence for
	 * sharing with other code, as an arbitrary {@link ByteSequence} may also implement mutating operations.
	 * 
	 * @return the given sequence as a {@link Bytes} instance. If the object is already a Bytes instance, 
	 * this method does nothing
	 */
	Bytes toBytes();

	/**
	 * Return the length of this byte instance
	 * 
	 * @return a non-negative length of the byte instance
	 */
	int length();

	/**
	 * Search for the first instance of a given byte sequence.
	 * 
	 * @param s sequence to search for within the object
	 * @param fromIndex starting index to search from. Must be in the range [0, {@link #length()}). If the 
	 * value is greater than {@link #length()} - `s.length()`, the search is guaranteed not to find an 
	 * instance.
	 * 
	 * @return index of the first instance of the sequence, or {@value #NOT_FOUND} if the sequence is not
	 * present within the range
	 * @throws IndexOutOfBoundsException `fromIndex` is outside the valid range.
	 */
	default int indexOf(ByteSequence s, int fromIndex) throws IndexOutOfBoundsException {
		Objects.requireNonNull(s);
		if (s.length() == 0) {
			return 0;
		}
		byte initial = s.get(0);
		int end = length() - s.length();
		for (int i = fromIndex; i < end; i++) {
			if (get(i) == initial) {
				boolean found = true;
				for (int j = 1; i < s.length(); j++) {
					if (get(i+j) != s.get(j)) {
						found = false;
						break;
					}
				}
				if (found) {
					return i;
				}
			}
		}
		return NOT_FOUND;
	}

	/**
	 * Return a new String based on the interpretation of this byte sequence within the given character
	 * encoding.
	 * 
	 * @param charset character encoding to interpret the byte sequence with
	 * @return new String instance
	 */
	String asString(Charset charset);

	/**
	 * Search for the first instance of a given byte sequence. Equivalent to {@link #indexOf(ByteSequence, int)}
	 * with a `fromIndex` of zero.
	 * 
	 * @param s sequence to search for within the object
	 * 
	 * @return index of the first instance of the sequence, or {@value #NOT_FOUND} if the sequence is not
	 * present.
	 */
	default int indexOf(ByteSequence s) {
		return indexOf(s, 0);
	}
	
	/**
	 * Return a new String based on the interpretation of this byte sequence as a utf-8 character encoding.
	 * @return String instance
	 */
	default String asUTF8String() {
		return asString(Charset.forName("UTF-8"));
	}

	/**
	 * Search for the given byte sequence, returning true if this sequence contains at once instance of the
	 * given sequence
	 * 
	 * @param s sequence to search for within the object
	 * @param fromIndex starting index to search from. Must be in the range [0, {@link #length()}). If the 
	 * value is greater than {@link #length()} - `s.length()`, the search is guaranteed not to find an 
	 * instance.
	 * 
	 * @return true if at least one instance of the given sequence was found within the range
	 * @throws IndexOutOfBoundsException `fromIndex` is outside the valid range.
	 */
	default boolean contains(ByteSequence s, int fromIndex) throws IndexOutOfBoundsException {
		return indexOf(s, fromIndex) != NOT_FOUND;
	}

	/**
	 * Search for the given byte sequence, returning true if this sequence contains at once instance of the
	 * given sequence. Equivalent to {@link #contains(ByteSequence, int)} with a `fromIndex` of zero.
	 * 
	 * @param s sequence to search for within the object
	 * 
	 * @return true if at least one instance of the given sequence was found within the range
	 */
	default boolean	contains(ByteSequence s) {
		return indexOf(s, 0) != NOT_FOUND;
	}
	
	/**
	 * @return `true` if the sequence is empty / has zero length
	 */
	default boolean isEmpty() {
		return length() == 0;
	}
		
	/**
	 * Retrieve a java `char` primitive as a two byte sequence starting at the given index. The bytes are 
	 * interpreted as being in the order defined by {@link #order()}, by default network/big-endian order
	 * 
	 * @param index of first byte of the char
	 * @return char value
	 * @throws IndexOutOfBoundsException if the sequence does not have enough bytes after the index to
	 * read the given type
	 */
	default char getChar(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index > length() - Character.BYTES) {
			throw new IndexOutOfBoundsException("index");
		}
		return (char) ((get(index) & 0xff) << 8 + (get(index + 1) & 0xff));
	}

	/**
	 * Retrieve a java `short` primitive as a two byte sequence starting at the given index. The bytes are 
	 * interpreted as being in the order defined by {@link #order()}, by default network/big-endian order
	 * 
	 * @param index of first byte of the short
	 * @return short value
	 * @throws IndexOutOfBoundsException if the sequence does not have enough bytes after the index to
	 * read the given type
	 */
	default short getShort(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index > length() - Short.BYTES) {
			throw new IndexOutOfBoundsException("index");
		}
		return (short) ((get(index) & 0xff) << 8 + (get(index + 1) & 0xff));
	}

	/**
	 * Retrieve a java `int` primitive as a two byte sequence starting at the given index. The bytes are 
	 * interpreted as being in the order defined by {@link #order()}, by default network/big-endian order
	 * 
	 * @param index of first byte of the int
	 * @return short value
	 * @throws IndexOutOfBoundsException if the sequence does not have enough bytes after the index to
	 * read the given type
	 */
	default int getInt(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index > length() - Integer.BYTES) {
			throw new IndexOutOfBoundsException("index");
		}
		return (get(index) & 0xff) << 24 + 
				(get(index + 1) & 0xff) << 16 +
				(get(index + 2) & 0xff) << 8 +
				(get(index + 3) & 0xff);
	}

	/**
	 * Retrieve a java `long` primitive as a two byte sequence starting at the given index. The bytes are 
	 * interpreted as being in the order defined by {@link #order()}, by default network/big-endian order
	 * 
	 * @param index of first byte of the long
	 * @return long value
	 * @throws IndexOutOfBoundsException if the sequence does not have enough bytes after the index to
	 * read the given type
	 */
	default long getLong(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index > length() - Long.BYTES) {
			throw new IndexOutOfBoundsException("index");
		}
		return (get(index) & 0xffL) << 56 + 
				(get(index + 1) & 0xffL) << 48 +
				(get(index + 2) & 0xffL) << 40 +
				(get(index + 3) & 0xffL) << 32 +
				(get(index + 4) & 0xffL) << 24 + 
				(get(index + 5) & 0xffL) << 16 +
				(get(index + 6) & 0xffL) << 8 +
				(get(index + 7) & 0xffL);
	}
	
	/**
	 * Retrieve a java `float` primitive as a two byte sequence starting at the given index. The bytes are 
	 * interpreted as being in the order defined by {@link #order()}, by default network/big-endian order
	 * 
	 * @param index of first byte of the float
	 * @return float value
	 * @throws IndexOutOfBoundsException if the sequence does not have enough bytes after the index to
	 * read the given type
	 */
	default float getFloat(int index) throws IndexOutOfBoundsException {
		return Float.intBitsToFloat(getInt(index));
	}
	
	/**
	 * Retrieve a java `double` primitive as a two byte sequence starting at the given index. The bytes are 
	 * interpreted as being in the order defined by {@link #order()}, by default network/big-endian order
	 * 
	 * @param index of first byte of the double
	 * @return double value
	 * @throws IndexOutOfBoundsException if the sequence does not have enough bytes after the index to
	 * read the given type
	 */
	default double getDouble(int index) throws IndexOutOfBoundsException {
		return Double.longBitsToDouble(getLong(index));
	}
	
	/**
	 * Retrieve a 16-bit value as a two byte sequence starting at the given index. The value is interpreted
	 * as a unsigned value, and is returned as a java integer. The bytes are
	 * interpreted as being in the order defined by {@link #order()}, by default network/big-endian order 
	 * 
	 * @param index of first byte of the short
	 * @return unsigned short value within a java int
	 * @throws IndexOutOfBoundsException if the sequence does not have enough bytes after the index to
	 * read the given type
	 */
	default int getUnsignedShort(int index) throws IndexOutOfBoundsException {
		return getShort(index) & 0x0000ffff;
	}

	/**
	 * Retrieve a 8-bit value at the given index. The value is interpreted
	 * as a unsigned value, and is returned as a java integer.
	 * 
	 * @param index of the byte
	 * @return unsigned byte value within a java int
	 * @throws IndexOutOfBoundsException if the index is outside the allowable range
	 */
	default int getUnsignedByte(int index) throws IndexOutOfBoundsException {
		return get(index) & 0x000000ff;
	}

	/**
	 * Convert the byte sequence to a hexadecimal string.
	 * 
	 * @param uppercase `true` uses uppercase letters A-F for representing the hexadecimal values 
	 * corresponding to 10-15. `false` uses lowercase letters a-f
	 * 
	 * @return hexadecimal string without whitespace of length {@link #length()} * 2 characters
	 */
	default String toHexString(boolean uppercase) {
		int length = length();
		StringBuilder builder = new StringBuilder(length*2);
		for (int i = 0; i < length; i++) {
			if (uppercase) {
				builder.append(String.format("%2X", get(i)));
			}
			else {
				builder.append(String.format("%2x", get(i)));
			}
		}
		return builder.toString();
	}
	

	/**
	 * Compare this sequence with another sequence.
	 * 
	 * Sequences are compared by each byte in the sequence as an unsigned value. This means, for example,
	 * that a 4-byte sequence of {@link Integer#MAX_VALUE} will sort before a sequence of {@link Integer#MIN_VALUE}.
	 * 
	 * If the available bytes are the same, the shorter length sorts first.
	 */
	default int compareTo(ByteSequence o) {
		int shortestLength = Math.min(length(), o.length());
		for (int i = 0; i<shortestLength; i++) {
			int comparison = getUnsignedByte(i) - o.getUnsignedByte(i);
			if (comparison != 0) {
				return comparison;
			}
		}
		return length() - o.length();
	}

	/** Equals implementation. Implementations of ByteSequence must implement equality across all implementations
	 * of {@link ByteSequence}.
	 */
	@Override
	public boolean equals(Object other);

	/** Hash code of value. The has value is required to be computed given the same algorithm so that 
	 * identical sequences of bytes hash to the same value. 
	 * 
	 * Must be computed as:
	 * 
	 * ```
	 * sum(i = 0; i &lt; length) (31^i * byteAt(i)) + 1
	 * ```
	 * 
	 * or `0` if `length == 0`
	 */
	@Override
	public int hashCode();

	/**
	 * @return byte order for interpreting values in getXXX(), by default {@link ByteOrder#BIG_ENDIAN} and
	 * not modifiable
	 */
	default ByteOrder order() {
		return ByteOrder.BIG_ENDIAN;
	}
	
	default public IntStream stream() {
		return toBytes().stream();		
	}

}