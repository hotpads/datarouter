package com.hotpads.util.core.iterable.scanner.filter;

public class FilterTool{

	public static <T> boolean includes(Filter<T> filter, T t){
		return filter==null || filter.include(t);
	}
	
	public static <T> boolean excludes(Filter<T> filter, T t){
		return !includes(filter, t);
	}
}
