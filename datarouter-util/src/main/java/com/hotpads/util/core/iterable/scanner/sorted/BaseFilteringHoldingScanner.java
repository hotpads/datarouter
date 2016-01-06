package com.hotpads.util.core.iterable.scanner.sorted;

import java.util.Iterator;

public abstract class BaseFilteringHoldingScanner<T> extends BaseHoldingScanner<T>{

	private Iterator<T> input;

	public BaseFilteringHoldingScanner(Iterator<T> input){
		this.input = input;
	}

	@Override
	public boolean advance(){
		while(input.hasNext()){
			T candidate = input.next();
			if(check(candidate)){
				current = candidate;
				return true;
			}
		}
		return false;
	}

	protected abstract boolean check(T candidate);

}
