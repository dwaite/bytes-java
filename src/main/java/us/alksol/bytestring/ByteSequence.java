package us.alksol.bytestring;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Objects;

public interface ByteSequence extends Comparable<ByteSequence> {
	public static final int NOT_FOUND = -1;

	public static void assertCorrectByteOffsetLength(byte[] input, int offset, int length) {
		Objects.requireNonNull(input);
		if (offset < 0 || offset >= input.length) {
			throw new IndexOutOfBoundsException("offset");
		}
		if (length < 0 || offset + length > input.length) {
			throw new IndexOutOfBoundsException("length");
		}
	}

	byte get(int index);

	ByteSequence subSequence(int start, int end);

	Bytes toByteString();

	int length();

	default int indexOf(ByteSequence s, int fromIndex) {
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

	String asString(Charset charset);

	default int indexOf(ByteSequence s) {
		return indexOf(s, 0);
	}
	
	default String asUTF8String() {
		return asString(Charset.forName("UTF-8"));
	}

	default boolean contains(ByteSequence s, int fromIndex) {
		return indexOf(s, fromIndex) != NOT_FOUND;
	}

	default boolean	contains(ByteSequence s) {
		return indexOf(s, 0) != NOT_FOUND;
	}
	
	default boolean isEmpty() {
		return length() == 0;
	}
	
	default char getChar(int index) {
		if (index < 0 || index > length() - Character.BYTES) {
			throw new IndexOutOfBoundsException("index");
		}
		return (char) ((get(index) & 0xff) << 8 + (get(index + 1) & 0xff));
	}

	default short getShort(int index) {
		if (index < 0 || index > length() - Short.BYTES) {
			throw new IndexOutOfBoundsException("index");
		}
		return (short) ((get(index) & 0xff) << 8 + (get(index + 1) & 0xff));
	}

	default int getInt(int index) {
		if (index < 0 || index > length() - Integer.BYTES) {
			throw new IndexOutOfBoundsException("index");
		}
		return (get(index) & 0xff) << 24 + 
				(get(index + 1) & 0xff) << 16 +
				(get(index + 2) & 0xff) << 8 +
				(get(index + 3) & 0xff);
	}

	default long getLong(int index) {
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
	
	default float getFloat(int index) {
		return Float.intBitsToFloat(getInt(index));
	}
	
	default double getDouble(int index) {
		return Double.longBitsToDouble(getLong(index));
	}
	default int getUnsignedShort(int index) {
		return getShort(index) & 0x0000ffff;
	}
	default int getUnsignedByte(int index) {
		return get(index) & 0x000000ff;
	}
	
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
	

	default int compareTo(ByteSequence o) {
		int shortestLength = Math.min(length(), o.length());
		for (int i = 0; i<shortestLength; i++) {
			int comparison = get(i) - o.get(i);
			if (comparison != 0) {
				return comparison;
			}
		}
		return length() - o.length();
	}

	/** Equals implementation should compare against all ByteSequence, not just your specific subtype */
	@Override
	public boolean equals(Object other);

	/** Hash code of value
	 * 
	 * Must be computed as:
	 * 
	 * sum(i = 0; i < length) (31^i * byteAt(i)) + 1
	 * 
	 * or zero if length == 0
	 */
	@Override
	public int hashCode();

	default ByteOrder order() {
		return ByteOrder.BIG_ENDIAN;
	}
}