package us.alksol.bytestring;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class ByteArray implements MutableByteSequence {
	private byte[] data;
	
	public ByteArray(byte[] data) {
		Objects.requireNonNull(data);
		this.data = data;
	}

	public byte get(int index) {
		return data[index];
	}

	public ByteSequence subSequence(int start, int end) {
		return new ByteArray(Arrays.copyOfRange(data, start, end));
	}

	public Bytes toByteString() {
		return new Bytes(data);
	}

	public int length() {
		return data.length;
	}

	public String asString(Charset charset) {
		return new String(data, charset);
	}
	
	public ByteArray put(int index,
            byte value) {
    	if (index < 0 || length() < index + 1) {
    		throw new IndexOutOfBoundsException("index");
    	}

		data[index] = value;
		return this;
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
			hash = 31 * hash + get(i);
		}
		return hash;
	}

}