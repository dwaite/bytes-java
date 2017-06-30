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
public class BytesSubsequence implements ByteSequence, Serializable, Comparable<ByteSequence> {
	private static final long serialVersionUID = 1L;

	private final byte[] bytes;
	private final int offset;
	private final int length;
	
	private static byte[] EMPTY_BYTES = new byte[0];
	private static BytesSubsequence EMPTY = new BytesSubsequence(EMPTY_BYTES, 0, 0, true);

	/**
	 * Create a new Byte instance by copying a subsequence of the input string
	 * 
	 * @param input byte array to base this object on
	 * @param offset offset into the byte array to start this Byte instance, in the range [0, input.length)
	 * @param length length of the byte array to create, in the range [0, input.length - offset]
	 */
	public BytesSubsequence(byte[] input, int offset, int length) {
		ByteSequence.assertCorrectByteOffsetLength(input, offset, length);

		this.offset = 0;
		this.length = length;
		if (length == 0) {
			bytes = EMPTY_BYTES;
		} else {
			this.bytes = new byte[length];
			System.arraycopy(input, offset, bytes, 0, length);
		}
	}

	BytesSubsequence(byte[] owned, int offset, int length, boolean distinguisher) {
		bytes = owned;
		this.offset = offset;
		this.length = length;
	} 

	@Override
	public String asString(Charset charset) {
		return new String(bytes, offset, length, charset);
	}

	@Override
	public byte get(int index) {
		if (index < 0 || index > length) {
			throw new IndexOutOfBoundsException("index");
		}
		return bytes[offset + index];
	}
	
	public IntStream bytes() {
		return StreamSupport.intStream(new ImmutableBytesSpliterator(bytes, offset, length + offset), false);		
	}
	
	public int indexOf(byte[] possibleSubsequence, int fromIndex) {
		Objects.requireNonNull(possibleSubsequence);
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (possibleSubsequence.length > length - fromIndex) {
			return -1;
		}
		for (int i = offset + fromIndex ;i < offset + length - bytes.length;i++) {
			boolean found = true;
			for (int j = 0; j < possibleSubsequence.length; j++) {
				if (bytes[i + j] != possibleSubsequence[j]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i - offset;
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
		if (length - fromIndex < subsequenceLength) {
			return NOT_FOUND;
		}
		
		for (int i = offset + fromIndex; i < offset + length - subsequenceLength; i++) {
			for (int j = 0; i < subsequenceLength; i++) {
				boolean found = true; 
				if (possibleSubsequence.get(j) != bytes[i + j]) {
					found = false;
					break;
				}
				if (found) {
					return i - offset;
				}
			}
		}
		return NOT_FOUND;
	}

	public int indexOf(byte[] possibleSubsequence) {
		return indexOf(possibleSubsequence, 0);
	}
	
	public int indexOf(BytesSubsequence possibleSubsequence, int fromIndex) {
		Objects.requireNonNull(possibleSubsequence);
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (possibleSubsequence.length > length - fromIndex) {
			return -1;
		}
		for (int i = offset + fromIndex ;i < offset + length - bytes.length;i++) {
			boolean found = true;
			for (int j = 0; j < possibleSubsequence.length; j++) {
				if (bytes[i + j] != possibleSubsequence.bytes[j + possibleSubsequence.offset]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i - offset;
			}
		}
		return -1;
	}
	public int indexOf(BytesSubsequence possibleSubsequence) {
		return indexOf(possibleSubsequence, 0);
	}
	public boolean contains(byte[] possibleSubsequence) {
		return indexOf(possibleSubsequence) != -1;
	}
	
	public boolean contains(BytesSubsequence possibleSubsequence) {
		return indexOf(possibleSubsequence) != -1;
	}

	public int indexOf(byte b, int fromIndex) {
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (length - fromIndex < 1) {
			return -1;
		}

		for (int i = offset + fromIndex; i < offset + length; i++) {
			if (bytes[i] == b) {
				return i - offset;
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
		if (length == 0) {
			return EMPTY_BYTES;
		}
		return Arrays.copyOfRange(bytes, offset, offset + length);
	}

	@Override
	public int compareTo(ByteSequence o) {
		int shortestLength = Math.min(length, o.length());
		for (int i = 0; i<shortestLength; i++) {
			int comparison = bytes[offset + i] - o.get(i);
			if (comparison != 0) {
				return comparison;
			}
		}
		return length - o.length();
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
		if (length == 0) {
			return 0;
		}
		int hash = 1;
		for (int i = offset; i < offset + length; i++) {
			hash = 31 * hash + bytes[i];
		}
		return hash;
	}
	
	@Override
	public BytesSubsequence subSequence(int start, int end) {
		if (start < 0 || start >= length) {
			throw new IndexOutOfBoundsException("start");
		}
		if (end < 0 || start + end >= length) {
			throw new IndexOutOfBoundsException("end");
		}
		if (start == end) {
			return BytesSubsequence.EMPTY;
		}
		return new BytesSubsequence(bytes, offset + start, end - start, true);
	}

	@Override
	public Bytes toBytes() {
		if (length == 0) {
			return Bytes.empty();
		}
		if (offset == 0 && length == this.bytes.length) {
			return new Bytes(this.bytes, true);
		}
		return new Bytes(this.bytes, offset, length);
	}
	
	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(bytes, offset, length).asReadOnlyBuffer();
	}
	
	@Override
	public int length() {
		return length;
	}


	public BytesSubsequence concat(BytesSubsequence suffix) {
		Objects.requireNonNull(suffix);
		return concat(suffix.bytes, suffix.offset, suffix.length);
	}

	public BytesSubsequence concat(byte[] suffix, int offset, int length) {
		Objects.requireNonNull(suffix);
		ByteSequence.assertCorrectByteOffsetLength(suffix, offset, length);

		if (suffix.length == 0) {
			return this;
		}
		int concattedLength = length + this.length;
		byte[] concattedBytes = new byte[concattedLength];
		System.arraycopy(bytes, this.offset, concattedBytes, 0, this.length);
		System.arraycopy(suffix, 0, concattedBytes, this.length, length);
		return new BytesSubsequence(concattedBytes, 0, length + this.length, true);
	}

	public BytesSubsequence concat(byte[] suffix) {
		return concat(suffix, 0, suffix.length);
	}
	
	public boolean endsWith(BytesSubsequence suffix) {
		Objects.requireNonNull(suffix);
		if (suffix.length > length) {
			return false;
		}
		
		for (int i = 1; i <= suffix.length; i++) {
			if (suffix.bytes[suffix.offset + suffix.length - i] != bytes[offset + length - i]) {
				return false;
			}
		}
		return true;
	}
	
	
	public String toString() {
		return toHexString(false);
	}

	
	public DataInput dataInput() {
		return new BytesDataInput(this.asSubsequence());
	}
	
	
	private BytesSubsequence asSubsequence() {
		return new BytesSubsequence(bytes, 0, bytes.length, true);
	}

	public InputStream inputStream() {
		return new ByteArrayInputStream(bytes, offset, length);
	}

	public void intoByteArray(byte[] b, int off, int len) {
		ByteSequence.assertCorrectByteOffsetLength(b, off, len);
		
		if (len > length) {
			throw new IndexOutOfBoundsException();
		}
		System.arraycopy(bytes, offset, b, off, len);
	}

	public void intoOutputStream(OutputStream os) throws IOException {
		os.write(bytes, offset, length);
	}

	public void intoDataOutput(DataOutput output) throws IOException {
		output.write(bytes, offset, length);
	}

	static BytesSubsequence empty() {
		return EMPTY;
	}

}