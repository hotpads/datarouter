package com.hotpads.util.core.iterable.scanner.sorted;

import com.hotpads.datarouter.util.core.ComparableTool;

public abstract class BaseSortedScanner<T extends Comparable<? super T>>
implements SortedScanner<T>{
	
	@Override
	public int compareTo(SortedScanner<T> other) {
		return ComparableTool.nullFirstCompareTo(
				getCurrent(), 
				other.getCurrent());
	}
	
}
