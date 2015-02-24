package com.hotpads.util.core.collections;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

public class SkipNullIterable<T> implements Iterable<T> {

	private final Iterator<T> iter;
	
	public SkipNullIterable(Iterable<T> iter) {
		this.iter = iter.iterator();
	}

	@Override
	public Iterator<T> iterator() {
		return new AbstractIterator<T>() {
			protected T computeNext() {
				while (iter.hasNext()) {
					T item = iter.next();
					if (item != null) {
						return item;
					}
				}
				return endOfData();
			}
		};
	}
}
