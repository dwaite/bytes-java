package us.alksol.bytestring.impl;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

import us.alksol.bytestring.Bytes;

public class BytesDataInput implements DataInput {
	private Bytes byteString;
	int index;
	
	public BytesDataInput(Bytes byteString) {
		this.byteString = byteString;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		if (len > byteString.length() - index) {
			throw new EOFException();
		}
		index += len;
		byteString.subSequence(index - len, index).intoByteArray(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		if (n < 0) {
			throw new IllegalArgumentException("n");
		}
		if (n == 0) {
			return 0;
		}
		int newIndex = Math.max(index +n, byteString.length());
		int result = newIndex - index;
		index = newIndex;
		return result;
	}

	@Override
	public boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	@Override
	public byte readByte() throws IOException {
		assertIndexAdvancableBy(1);
		byte result = byteString.get(index);
		index +=1;
		return result;
	}


	private void assertIndexAdvancableBy(int i) throws EOFException {
		if (index + i >= byteString.length()) {
			throw new EOFException();
		}
	}

	@Override
	public int readUnsignedByte() throws IOException {
		int result = byteString.getUnsignedByte(index);
		index +=1;
		return result;
	}

	@Override
	public short readShort() throws IOException {
		assertIndexAdvancableBy(2);
		short result = byteString.getShort(index);
		index +=2;
		return result;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		assertIndexAdvancableBy(2);
		int result = byteString.getUnsignedShort(index);
		index +=2;
		return result;
	}

	@Override
	public char readChar() throws IOException {
		assertIndexAdvancableBy(2);
		char result = byteString.getChar(index);
		index +=2;
		return result;
	}

	@Override
	public int readInt() throws IOException {
		assertIndexAdvancableBy(4);
		int result = byteString.getInt(index);
		index +=4;
		return result;
	}

	@Override
	public long readLong() throws IOException {
		assertIndexAdvancableBy(8);
		long result = byteString.getLong(index);
		index +=8;
		return result;
	}

	@Override
	public float readFloat() throws IOException {
		assertIndexAdvancableBy(4);
		float result = byteString.getFloat(index);
		index +=4;
		return result;
	}

	@Override
	public double readDouble() throws IOException {
		assertIndexAdvancableBy(8);
		double result = byteString.getDouble(index);
		index +=8;
		return result;
	}

	@Override
	public String readLine() throws IOException {
		//meh. really?
		StringBuilder builder = new StringBuilder();
		try {
			while(true) {
				int i = readUnsignedByte();
				if (i == '\n') {
					return builder.toString();
				}
				if (i == '\r') {
					if (index +1 < byteString.length() && byteString.getChar(index + 1) == '\n') {
						readByte();
						return builder.toString();
					}
				}
				builder.append((char)i);
			}
		}
		catch (EOFException e) {
			return builder.toString();
		}
	}

	@Override
	public String readUTF() throws IOException {
		int end = byteString.indexOf((byte)0, index);
		if (end == -1) {
			end = byteString.length();
		}
		String result = byteString.subSequence(index, end).asUTF8String();
		index = end;
		return result;
	}
}
