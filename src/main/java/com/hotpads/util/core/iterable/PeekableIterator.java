package com.hotpads.util.core.iterable;

import java.util.Iterator;

/*
 * java's built-in Iterator has a hasNext() method, and if you are going to have that, it's usually a low-cost
 *  operation to peek() the next value
 *  
 * if it's easy to do hasNext() but difficult to peek(), then use built-in Iterator
 * 
 * if it's difficult to do both, then use a Scanner
 */
public interface PeekableIterator<T> extends Iterator<T>{

	T peek();
	
}
