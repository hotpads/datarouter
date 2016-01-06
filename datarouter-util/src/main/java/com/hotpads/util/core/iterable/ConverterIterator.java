package com.hotpads.util.core.iterable;

import java.util.Iterator;
import java.util.function.Function;

public class ConverterIterator<I,O> implements Iterator<O>{

	private Iterator<I> inputIterator;
	private Function<I,O> function;

	public ConverterIterator(Iterator<I> inputIterator, Function<I,O> function){
		this.inputIterator = inputIterator;
		this.function = function;
	}

	@Override
	public boolean hasNext(){
		return inputIterator.hasNext();
	}

	@Override
	public O next(){
		return function.apply(inputIterator.next());
	}

	@Override
	public void remove(){
		inputIterator.remove();
	}

}
