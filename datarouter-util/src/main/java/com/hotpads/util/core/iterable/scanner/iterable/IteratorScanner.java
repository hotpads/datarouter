package com.hotpads.util.core.iterable.scanner.iterable;

import java.util.Iterator;

import com.hotpads.util.core.iterable.scanner.sorted.BaseHoldingScanner;

public class IteratorScanner<T> extends BaseHoldingScanner<T>{

	private Iterator<T> iterator;

	public IteratorScanner(Iterator<T> iterator){
		this.iterator = iterator;
	}

	@Override
	public boolean advance(){
		if(iterator.hasNext()){
			this.current = iterator.next();
			return true;
		}
		return false;
	}
}
