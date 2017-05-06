package us.alksol.bytestring;

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

import us.alksol.bytestring.impl.BytesDataInput;
import us.alksol.bytestring.impl.ImmutableBytesSpliterator;

public class Bytes implements ByteSequence, Serializable, Comparable<ByteSequence> {
	private static final long serialVersionUID = 1L;

	private final byte[] bytes;
	private final int offset;
	private final int length;
	
	private static byte[] EMPTY_BYTES = new byte[0];
	private static Bytes EMPTY = new Bytes(EMPTY_BYTES, 0, 0, true);
	
	private static void assertCorrectByteOffsetLength(byte[] input, int offset, int length) {
		Objects.requireNonNull(input);
		if (offset < 0 || offset >= input.length) {
			throw new IndexOutOfBoundsException("offset");
		}
		if (length < 0 || offset + length > input.length) {
			throw new IndexOutOfBoundsException("length");
		}
	}

	public Bytes(byte[] input) {
		this(input, 0, input.length);
	}

	public Bytes(byte[] input, int offset, int length) {
		assertCorrectByteOffsetLength(input, offset, length);

		this.offset = 0;
		this.length = length;
		if (length == 0) {
			bytes = EMPTY_BYTES;
		} else {
			this.bytes = new byte[length];
			System.arraycopy(input, offset, bytes, 0, length);
		}
	}

	/** create a new byte string from an existing byte string. If the existing bytestring is a substring, this will
	 * make a new copy containing just the substring data.
	 * 
	 */
	public Bytes(Bytes bytestring) {
		Objects.requireNonNull(bytestring);
		if (bytestring.offset == 0 && bytestring.length == bytestring.bytes.length) {
			this.offset = 0;
			this.length = bytestring.length;
			this.bytes = bytestring.bytes;
			return;
		}
		this.offset = 0;
		this.length = bytestring.length;		
		this.bytes = new byte[length];
		System.arraycopy(bytestring.bytes, bytestring.offset, bytes, offset, length);
	}
	
	public Bytes(ByteBuffer buffer) {
		offset = 0;
		length = buffer.remaining();
		if (length == 0) {
			bytes = EMPTY_BYTES;
		} 
		else {
			bytes = new byte[length];
			buffer.get(bytes);				
		}
	}

	private Bytes(byte[] owned, int offset, int length, boolean distinguisher) {
		bytes = owned;
		this.offset = offset;
		this.length = length;
	} 

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
		return new Bytes(bytes, 0, bytes.length, true);
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
			length += element.length;
		}
		
		if (length == 0) {
			return EMPTY;
		}
		byte[] bytes = new byte[length];
		int offset = 0;
		for (Bytes element : elements) {
			System.arraycopy(element.bytes, element.offset, bytes, offset, element.length);
			offset += element.length;
		}
		
		return new Bytes(bytes, 0, length, true);
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
	
	public int indexOf(Bytes possibleSubsequence, int fromIndex) {
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
	
	public byte[] toBytes() {
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
	public Bytes subSequence(int start, int end) {
		if (start < 0 || start >= length) {
			throw new IndexOutOfBoundsException("start");
		}
		if (end < 0 || start + end >= length) {
			throw new IndexOutOfBoundsException("end");
		}
		return new Bytes(bytes, offset + start, end - start, true);
	}

	@Override
	public Bytes toByteString() {
		return this;
	}
	
	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(bytes, offset, length).asReadOnlyBuffer();
	}
	
	@Override
	public int length() {
		return length;
	}


	public Bytes concat(Bytes suffix) {
		Objects.requireNonNull(suffix);
		return concat(suffix.bytes, suffix.offset, suffix.length);
	}

	public Bytes concat(byte[] suffix, int offset, int length) {
		Objects.requireNonNull(suffix);
		assertCorrectByteOffsetLength(suffix, offset, length);

		if (suffix.length == 0) {
			return this;
		}
		int concattedLength = length + this.length;
		byte[] concattedBytes = new byte[concattedLength];
		System.arraycopy(bytes, this.offset, concattedBytes, 0, this.length);
		System.arraycopy(suffix, 0, concattedBytes, this.length, length);
		return new Bytes(concattedBytes, 0, length + this.length, true);
	}

	public Bytes concat(byte[] suffix) {
		return concat(suffix, 0, suffix.length);
	}
	
	public boolean endsWith(Bytes suffix) {
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
		return new BytesDataInput(this);
	}
	
	
	public InputStream inputStream() {
		return new ByteArrayInputStream(bytes, offset, length);
	}

	public void intoByteArray(byte[] b, int off, int len) {
		assertCorrectByteOffsetLength(b, off, len);
		
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

}