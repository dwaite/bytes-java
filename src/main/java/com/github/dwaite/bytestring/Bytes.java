package com.github.dwaite.bytestring;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.github.dwaite.bytestring.impl.BytesDataInput;
import com.github.dwaite.bytestring.impl.ImmutableBytesSpliterator;

/**
 * Immutable, save byte sequence type. 
 * 
 * This type is meant to be analagous to {@link String}, in that
 * the underlying character array in String is not safe to pass around normally, but by owning and treating
 * that array as private data you can have a safe, interoperable type. This hopefully reduces the amount of
 * defensive copying you need to make to be sure other objects aren't mutating your object's internal state.
 */
public class Bytes implements ByteSequence, Serializable, Comparable<ByteSequence> {
	private static final long serialVersionUID = 1L;

	private final byte[] bytes;
	
	private static byte[] EMPTY_BYTES = new byte[0];
	private static Bytes EMPTY = new Bytes(EMPTY_BYTES, true);

	/**
	 * Create a new Byte instance by copying the input string
	 * 
	 * @param input byte array to base this object on
	 */
	public Bytes(byte[] input) {
		this(input, 0, input.length);
	}

	// differentiated package-private constructor that doesn't copy, for use with known safe data
	Bytes(byte[] input, boolean alwaysImportWithoutCopying) {
		bytes = input;
	}
	
	/**
	 * Create a new Bytes instance by copying a subsequence of the input string
	 * 
	 * @param input byte array to base this object on
	 * @param offset offset into the byte array to start this Byte instance, in the range [0, input.length)
	 * @param length length of the byte array to create, in the range [0, input.length - offset]
	 */
	public Bytes(byte[] input, int offset, int length) {
		ByteSequence.assertCorrectByteOffsetLength(input, offset, length);

		if (length == 0) {
			bytes = EMPTY_BYTES;
		} else {
			this.bytes = new byte[length];
			System.arraycopy(input, offset, bytes, 0, length);
		}
	}

	/**
	 * Create a new Bytes instance by copying the data out of a ByteBuffer
	 * 
	 * @param buffer byte buffer
	 */
	public Bytes(ByteBuffer buffer) {
		int length = buffer.remaining();
		if (length == 0) {
			bytes = EMPTY_BYTES;
		} 
		else {
			bytes = new byte[length];
			buffer.get(bytes);				
		}
	}

	/**
	 * Return a Bytes object with no content
	 * @return empty bytes object
	 */
	public static Bytes empty() {
		return EMPTY;
	}
	
	public static Bytes ofUTF8(String input) {
		return ofString(input, Charset.forName("UTF-8"));
	}

	public static Bytes ofString(String input, Charset charset) {
		byte[] bytes = input.getBytes(charset);
		if (bytes.length == 0) {
			bytes = EMPTY_BYTES;
		}
		return new Bytes(bytes, true);
	}

	public static Bytes ofHexString(String input) {
	    int len = input.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        int b = ((Character.digit(input.charAt(i), 16) << 4)
	                        + Character.digit(input.charAt(i+1), 16));
	        if (b < 0) {
	        	throw new NumberFormatException();
	        }
	        data[i/2] = (byte) b;
	    }
	    return new Bytes(data, 0, len/2);
	}
	
	public static Bytes join(Bytes... elements) {
		int length = 0;
		for (Bytes element : elements) {
			Objects.requireNonNull(element);
			length += element.length();
		}
		
		if (length == 0) {
			return EMPTY;
		}
		byte[] bytes = new byte[length];
		int offset = 0;
		for (Bytes element : elements) {
			System.arraycopy(element.bytes, 0, bytes, offset, element.length());
			offset += element.length();
		}
		
		return new Bytes(bytes, true);
	}

	@Override
	public String asString(Charset charset) {
		return new String(bytes, charset);
	}

	@Override
	public byte get(int index) {
		if (index < 0 || index > length()) {
			throw new IndexOutOfBoundsException("index");
		}
		return bytes[index];
	}
	
	public IntStream stream() {
		return StreamSupport.intStream(new ImmutableBytesSpliterator(bytes, 0, length()), false);		
	}
	
	public int indexOf(byte[] possibleSubsequence, int fromIndex) {
		Objects.requireNonNull(possibleSubsequence);
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (possibleSubsequence.length > length() - fromIndex) {
			return -1;
		}
		for (int i = fromIndex ;i < length() - bytes.length;i++) {
			boolean found = true;
			for (int j = 0; j < possibleSubsequence.length; j++) {
				if (bytes[i + j] != possibleSubsequence[j]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public int indexOf(ByteSequence possibleSubsequence, int fromIndex) {
		Objects.requireNonNull(possibleSubsequence);
		int subsequenceLength = possibleSubsequence.length();
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (length() - fromIndex < subsequenceLength) {
			return NOT_FOUND;
		}
		
		for (int i = fromIndex; i < length() - subsequenceLength; i++) {
			for (int j = 0; i < subsequenceLength; i++) {
				boolean found = true; 
				if (possibleSubsequence.get(j) != bytes[i + j]) {
					found = false;
					break;
				}
				if (found) {
					return i;
				}
			}
		}
		return NOT_FOUND;
	}

	public int indexOf(byte[] possibleSubsequence) {
		return indexOf(possibleSubsequence, 0);
	}
	
	public int indexOf(Bytes possibleSubsequence, int fromIndex) {
		Objects.requireNonNull(possibleSubsequence);
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (possibleSubsequence.length() > length() - fromIndex) {
			return -1;
		}
		for (int i = fromIndex ;i < length() - bytes.length;i++) {
			boolean found = true;
			for (int j = 0; j < possibleSubsequence.length(); j++) {
				if (bytes[i + j] != possibleSubsequence.bytes[j]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i;
			}
		}
		return -1;
	}
	public int indexOf(Bytes possibleSubsequence) {
		return indexOf(possibleSubsequence, 0);
	}
	public boolean contains(byte[] possibleSubsequence) {
		return indexOf(possibleSubsequence) != -1;
	}
	
	public boolean contains(Bytes possibleSubsequence) {
		return indexOf(possibleSubsequence) != -1;
	}

	public int indexOf(byte b, int fromIndex) {
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (length() - fromIndex < 1) {
			return -1;
		}

		for (int i = fromIndex; i < length(); i++) {
			if (bytes[i] == b) {
				return i;
			}
		}
		return -1;
	}
	
	public int indexOf(byte b) {
		return indexOf(b, 0);
	}
	
	public boolean contains(byte b) {
		return indexOf(b) != -1;
	}
	
	public byte[] toByteArray() {
		if (length() == 0) {
			return EMPTY_BYTES;
		}
		return Arrays.copyOf(bytes, bytes.length);
	}

	@Override
	public int compareTo(ByteSequence o) {
		int shortestLength = Math.min(length(), o.length());
		for (int i = 0; i<shortestLength; i++) {
			int comparison = bytes[i] - o.get(i);
			if (comparison != 0) {
				return comparison;
			}
		}
		return length() - o.length();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || !(other instanceof ByteSequence)) {
			return false;
		}
		ByteSequence o =  (ByteSequence) other;
		if (o.length() != length()) {
			return false;
		}
		int length = length();
		for (int i = 0; i < length; i++) {
			if (get(i) != o.get(i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (length() == 0) {
			return 0;
		}
		int hash = 1;
		for (int i = 0; i < length(); i++) {
			hash = 31 * hash + bytes[i];
		}
		return hash;
	}
	
	@Override
	public BytesSubsequence subSequence(int start, int end) {
		if (start < 0 || start >= length()) {
			throw new IndexOutOfBoundsException("start");
		}
		if (end < 0 || start + end >= length()) {
			throw new IndexOutOfBoundsException("end");
		}
		if (start == end) {
			return BytesSubsequence.empty();
		}
		return new BytesSubsequence(bytes, start, end - start, true);
	}

	@Override
	public Bytes toBytes() {
		return this;
	}
	
	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(bytes, 0, length()).asReadOnlyBuffer();
	}
	
	@Override
	public int length() {
		return bytes.length;
	}


	public Bytes concat(Bytes suffix) {
		Objects.requireNonNull(suffix);
		return concat(suffix.bytes, 0, suffix.length());
	}

	public Bytes concat(byte[] suffix, int offset, int length) {
		Objects.requireNonNull(suffix);
		ByteSequence.assertCorrectByteOffsetLength(suffix, offset, length);

		if (suffix.length == 0) {
			return this;
		}
		int concattedLength = length + this.length();
		byte[] concattedBytes = new byte[concattedLength];
		System.arraycopy(bytes, 0, concattedBytes, 0, this.length());
		System.arraycopy(suffix, 0, concattedBytes, this.length(), length);
		return new Bytes(concattedBytes, true);
	}

	public Bytes concat(byte[] suffix) {
		return concat(suffix, 0, suffix.length);
	}
	
	public boolean endsWith(Bytes suffix) {
		Objects.requireNonNull(suffix);
		if (suffix.length() > length()) {
			return false;
		}
		
		for (int i = 1; i <= suffix.length(); i++) {
			if (suffix.bytes[suffix.length() - i] != bytes[length() - i]) {
				return false;
			}
		}
		return true;
	}
	
	
	public String toString() {
		return toHexString(false);
	}

	
	public DataInput dataInput() {
		return new BytesDataInput(this);
	}
	
	
	public InputStream inputStream() {
		return new ByteArrayInputStream(bytes, 0, length());
	}

	public void intoByteArray(byte[] b, int off, int len) {
		ByteSequence.assertCorrectByteOffsetLength(b, off, len);
		
		if (len > length()) {
			throw new IndexOutOfBoundsException();
		}
		System.arraycopy(bytes, 0, b, off, len);
	}

	public void intoOutputStream(OutputStream os) throws IOException {
		os.write(bytes, 0, length());
	}

	public void intoDataOutput(DataOutput output) throws IOException {
		output.write(bytes, 0, length());
	}

}