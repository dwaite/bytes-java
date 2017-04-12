package us.alksol.bytestring.impl;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntConsumer;

public class ImmutableBytesSpliterator implements Spliterator.OfInt {
    private final byte[] bytes;
    private int pos;
    private int end;
    
    public ImmutableBytesSpliterator(byte[] bytes, int offset, int length) {
    	this.bytes = bytes;
    	this.pos = offset;
    	this.end = offset + length;
    }

    @Override
    public int characteristics() {
        return ORDERED | SIZED | IMMUTABLE | SUBSIZED;

    }

    @Override
    public OfInt trySplit() {
    	int split = (pos + end) / 2;
    	if (split > pos) {
    		this.pos = split;
    		return new ImmutableBytesSpliterator(bytes, pos, split);
    	}
    	return null;
    }

    @Override
    public void forEachRemaining(IntConsumer action) {
    	Objects.requireNonNull(action);
    	for (int i = pos; i < end; i++) {
    		action.accept(bytes[i] & 0xff);
    	}
    }

    @Override
    public boolean tryAdvance(IntConsumer action) {
    	Objects.requireNonNull(action);
    	if (pos < end) {
    		action.accept(bytes[pos++] & 0xff);
    		return true;
    	}
    	return false;
    }

    @Override
    public long estimateSize() { return (long)(end - pos); }
}