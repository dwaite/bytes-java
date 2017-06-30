package com.github.dwaite.bytestring;

import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

public class BytesBuffer implements MutableByteSequence {
	
	private java.nio.ByteBuffer byteBuffer;

	private BytesBuffer(java.nio.ByteBuffer byteBuffer, boolean ownedBuffer) {
		Objects.requireNonNull(byteBuffer.duplicate());
		if (ownedBuffer) {
			this.byteBuffer = byteBuffer;
		}
		else {
			this.byteBuffer = byteBuffer.duplicate();
		}
	}
	
	public static BytesBuffer allocate(int capacity) {
		return new BytesBuffer(java.nio.ByteBuffer.allocate(capacity), true);
	}

	public static BytesBuffer allocateDirect(int capacity) {
		return new BytesBuffer(java.nio.ByteBuffer.allocateDirect(capacity), true);
	}
	
	public static BytesBuffer wrap(byte[] bytes, int offset, int length) {
		return new BytesBuffer(java.nio.ByteBuffer.wrap(bytes, offset, length), true);
	}

	public static BytesBuffer wrap(byte[] bytes) {
		return new BytesBuffer(java.nio.ByteBuffer.wrap(bytes), true);
	}

	public static BytesBuffer wrap(java.nio.ByteBuffer byteBuffer) {
		return new BytesBuffer(byteBuffer, false);
	}

	// bytesequence methods
	@Override
	public byte get(int index) {
		return byteBuffer.get(index);
	}

	@Override
	public BytesBuffer subSequence(int start, int end) {
		java.nio.ByteBuffer dup = byteBuffer.duplicate();
		dup.position(start);
		dup.limit(end);
		return new BytesBuffer(dup.slice(), true);
	}

	public Bytes toBytes() {
		return new Bytes(byteBuffer);
	}

	public int length() {
		return limit();
	}

	public String asString(Charset charset) {
		java.nio.ByteBuffer output = byteBuffer.duplicate();
		output.position(0);
		return charset.decode(output).toString();
	}

	// Buffer methods
	public final int capacity() {
		return byteBuffer.capacity();
	}
	
	public final int position() {
		return byteBuffer.position();
	}
	
	public final BytesBuffer position(int newPosition) {
		byteBuffer.position(newPosition);
		return this;
	}
	
	public final int limit() {
		return byteBuffer.limit();
	}
	
	public BytesBuffer limit(int newLimit) {
		byteBuffer.limit(newLimit);
		return this;
	}
	
	public BytesBuffer mark() {
		byteBuffer.mark();
		return this;
	}
	
	public BytesBuffer reset() {
		byteBuffer.reset();
		return this;
	}
	
	public BytesBuffer clear() {
		byteBuffer.clear();
		return this;
	}
	
	public BytesBuffer flip() {
		byteBuffer.flip();
		return this;
	}
	
	public BytesBuffer rewind() {
		byteBuffer.rewind();
		return this;
	}

	public final int remaining() {
		return byteBuffer.remaining();
	}
	
	public final boolean hasRemaining() {
		return byteBuffer.hasRemaining();
	}
	
	public boolean isReadOnly() {
		return byteBuffer.isReadOnly();
	}
	
	public boolean hasArray() {
		return byteBuffer.hasArray();
	}
	
	public byte[] array() {
		return byteBuffer.array();
	}
	
	public int arrayOffset() {
		return byteBuffer.arrayOffset();
	}
	
	public boolean isDirect() {
		return byteBuffer.isDirect();
	}
	
	public BytesBuffer slice() {
		java.nio.ByteBuffer slice = byteBuffer.slice();
		return new BytesBuffer(slice, true);
	}
	
	public BytesBuffer duplicate() {
		java.nio.ByteBuffer dup = byteBuffer.duplicate();
		return new BytesBuffer(dup, true);
	}
	
	// byte buffer members
	public BytesBuffer asReadOnlyBuffer() {
		if (isReadOnly()) {
			return this;
		}
		return new BytesBuffer(byteBuffer.asReadOnlyBuffer(), true);
	}
	
	public byte get() {
		return byteBuffer.get();
	}
	
	public BytesBuffer put(byte b) {
		byteBuffer.put(b);
		return this;
	}
	
	public BytesBuffer put(int index,  byte b) {
		byteBuffer.put(index, b);
		return this;
	}
	
	public BytesBuffer get(byte[] dst,
            int offset,
            int length) {
		byteBuffer.get(dst, offset, length);
		return this;
	}

	public BytesBuffer get(byte[] dst) {
		byteBuffer.get(dst);
		return this;
	}

	public BytesBuffer put(BytesBuffer src) {
		byteBuffer.put(src.byteBuffer);
		return this;
	}

	public BytesBuffer put(byte[] src,
            int offset,
            int length) {
		byteBuffer.put(src, offset, length);
		return this;
	}
	
	// mapped byte buffer members
	public boolean isMappedByteBuffer() {
		return byteBuffer instanceof MappedByteBuffer;
	}
	public boolean isLoaded() {
		return ((MappedByteBuffer)byteBuffer).isLoaded();
	}
	
	public BytesBuffer load() {
		((MappedByteBuffer)byteBuffer).load();
		return this;
	}
	
	public BytesBuffer force() {
		((MappedByteBuffer)byteBuffer).force();
		return this;
	}
	
	public final BytesBuffer put(byte[] src) {
		byteBuffer.put(src);
		return this;
	}
	
	public final BytesBuffer compact() {
		byteBuffer.compact();
		return this;
	}

	public ByteOrder order() {
		return byteBuffer.order();
	}
	
	public final BytesBuffer order(ByteOrder bo) {
		byteBuffer.order(bo);
		return this;
	}

	//char 
	public char getChar() {
		return byteBuffer.getChar();
	}
	public BytesBuffer putChar(char value) {
		byteBuffer.putChar(value);
		return this;
	}

	public CharBuffer asCharBuffer() {
		return byteBuffer.asCharBuffer();
	}
	
	@Override
	public char getChar(int index) {
		return byteBuffer.getChar(index);
	}
	
	@Override
	public BytesBuffer putChar(int index, char ch) {
		byteBuffer.putChar(index, ch);
		return this;
	}

	//short
	public short getShort() {
		return byteBuffer.getShort();
	}
	public BytesBuffer putShort(short value) {
		byteBuffer.putShort(value);
		return this;
	}

	public ShortBuffer asShortBuffer() {
		return byteBuffer.asShortBuffer();
	}
	
	@Override
	public short getShort(int index) {
		return byteBuffer.getShort(index);
	}
	
	@Override
	public BytesBuffer putShort(int index, short sh) {
		byteBuffer.putShort(index, sh);
		return this;
	}
	
	//int
	public char getInt() {
		return byteBuffer.getChar();
	}
	public BytesBuffer putInt(int value) {
		byteBuffer.putInt(value);
		return this;
	}

	public IntBuffer asIntBuffer() {
		return byteBuffer.asIntBuffer();
	}
	
	@Override
	public int getInt(int index) {
		return byteBuffer.getChar(index);
	}
	
	@Override
	public BytesBuffer putInt(int index, int value) {
		byteBuffer.putInt(index, value);
		return this;
	}
	//long
	public long getLong() {
		return byteBuffer.getLong();
	}
	public BytesBuffer putLong(long value) {
		byteBuffer.putLong(value);
		return this;
	}

	public LongBuffer asLongBuffer() {
		return byteBuffer.asLongBuffer();
	}
	
	@Override
	public long getLong(int index) {
		return byteBuffer.getLong(index);
	}
	
	@Override
	public BytesBuffer putLong(int index, long value) {
		byteBuffer.putLong(index, value);
		return this;
	}
	//float
	public float getFloat() {
		return byteBuffer.getFloat();
	}
	public BytesBuffer putFloat(float value) {
		byteBuffer.putFloat(value);
		return this;
	}

	public FloatBuffer asFloatBuffer() {
		return byteBuffer.asFloatBuffer();
	}
	
	@Override
	public float getFloat(int index) {
		return byteBuffer.getFloat(index);
	}
	
	@Override
	public BytesBuffer putFloat(int index, float value) {
		byteBuffer.putFloat(index, value);
		return this;
	}
	//double
	public double getDouble() {
		return byteBuffer.getDouble();
	}
	public BytesBuffer putDouble(double value) {
		byteBuffer.putDouble(value);
		return this;
	}

	public DoubleBuffer asDoubleBuffer() {
		return byteBuffer.asDoubleBuffer();
	}
	
	@Override
	public double getDouble(int index) {
		return byteBuffer.getChar(index);
	}
	
	@Override
	public BytesBuffer putDouble(int index, double value) {
		byteBuffer.putDouble(index, value);
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
