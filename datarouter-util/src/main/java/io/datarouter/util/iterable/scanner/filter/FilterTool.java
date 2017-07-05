package io.datarouter.util.iterable.scanner.filter;

public class FilterTool{

	public static <T> boolean includes(Filter<T> filter, T element){
		return filter == null || filter.include(element);
	}

	public static <T> boolean excludes(Filter<T> filter, T element){
		return !includes(filter, element);
	}
}
