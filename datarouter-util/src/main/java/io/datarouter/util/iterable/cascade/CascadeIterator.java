package io.datarouter.util.iterable.cascade;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * An iterator wrapped around a supplier of iterators.
 * @param <T> the type of the iterated elements
 */
public class CascadeIterator<T> implements Iterator<T>{

	private final Supplier<Iterator<T>> iteratorSupplier;
	private Iterator<T> currentIterator;
	private boolean nextCalled;

	/**
	 * Create a cascading iterator from a supplier. The supplier supplies an iterator at each invocation until it has
	 * run out of iterator supplies; after that, it should return {@code null}.
	 *
	 * @param iteratorSupplier the supplier supplies an iterator at each in
	 */
	public CascadeIterator(final Supplier<Iterator<T>> iteratorSupplier){
		this.iteratorSupplier = iteratorSupplier;
		this.currentIterator = iteratorSupplier.get();
	}

	@Override
	public boolean hasNext(){
		while(currentIterator != null && !currentIterator.hasNext()){
			currentIterator = iteratorSupplier.get();
		}
		return currentIterator != null;
	}

	@Override
	public T next(){
		if(!hasNext()){
			throw new NoSuchElementException();
		}
		nextCalled = true;
		return currentIterator.next();
	}

	@Override
	public void remove(){
		if(!nextCalled){
			throw new IllegalStateException("next() must be called before each remove()"); // per standard spec
		}
		if(currentIterator != null){
			currentIterator.remove();
			nextCalled = false;
		}
	}
}
