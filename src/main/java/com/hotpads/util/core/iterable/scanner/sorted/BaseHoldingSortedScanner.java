package com.hotpads.util.core.iterable.scanner.sorted;

public abstract class BaseHoldingSortedScanner<T extends Comparable<? super T>> 
extends BaseSortedScanner<T>{

	protected T current;
		
	@Override
	public T getCurrent() {
		return current;
	}
}
