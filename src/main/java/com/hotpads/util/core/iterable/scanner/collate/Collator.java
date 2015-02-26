package com.hotpads.util.core.iterable.scanner.collate;

import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public interface Collator<T extends Comparable<? super T>> 
extends SortedScanner<T> {

	void add(SortedScanner<T> scanner);
	
}
