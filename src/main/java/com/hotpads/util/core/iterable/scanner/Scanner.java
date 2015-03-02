package com.hotpads.util.core.iterable.scanner;

public interface Scanner<T>{

	T getCurrent();
	
	/*
	 * Return true if the current value was advanced, otherwise false.  Repeated calls after the initial false should
	 * continue to return false without side effects.
	 */
	boolean advance();
	
}
