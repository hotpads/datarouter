package com.hotpads.util.core.iterable.scanner.sorted;

import com.hotpads.datarouter.util.core.DrComparableTool;

public abstract class BaseSortedScanner<T extends Comparable<? super T>>
extends BaseScanner<T>
implements SortedScanner<T>{

	@Override
	public int compareTo(SortedScanner<T> otherScanner){
		return DrComparableTool.nullFirstCompareTo(getCurrent(), otherScanner.getCurrent());
	}

}
