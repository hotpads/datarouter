package com.hotpads.util.core.iterable;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * An iterable wrapped around a supplier of iterators.
 * @param <T> the type of the iterated elements
 * @see CascadeIterator
 */
public class CascadeIterable<T> implements Iterable<T>{

	private final Supplier<Iterator<T>> iteratorSupplier;

	public CascadeIterable(final Supplier<Iterator<T>> iteratorSupplier){
		this.iteratorSupplier = iteratorSupplier;
	}

	@Override
	public Iterator<T> iterator(){
		return new CascadeIterator<>(iteratorSupplier);
	}

}
