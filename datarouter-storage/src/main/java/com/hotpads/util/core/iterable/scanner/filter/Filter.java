package com.hotpads.util.core.iterable.scanner.filter;

/*
 * predicate class dedicated to scanners
 */
public interface Filter<T>{

	boolean include(T t);
	
}
