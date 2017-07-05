package io.datarouter.util.iterable.scanner.filter;

/*
 * predicate class dedicated to scanners
 */
public interface Filter<T>{

	boolean include(T element);

}
