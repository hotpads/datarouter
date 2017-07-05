package io.datarouter.util.iterable.scanner.sorted;

import io.datarouter.util.ComparableTool;

public abstract class BaseSortedScanner<T extends Comparable<? super T>>
extends BaseScanner<T>
implements SortedScanner<T>{

	@Override
	public int compareTo(SortedScanner<T> otherScanner){
		return ComparableTool.nullFirstCompareTo(getCurrent(), otherScanner.getCurrent());
	}

}
