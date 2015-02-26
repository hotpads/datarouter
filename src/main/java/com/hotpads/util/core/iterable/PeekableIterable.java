package com.hotpads.util.core.iterable;


public interface PeekableIterable<T> extends Iterable<T>{

	@Override
	public PeekableIterator<T> iterator();
	
}
