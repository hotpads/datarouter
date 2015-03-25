package com.hotpads.util.core.iterable.scanner.iterable;

import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.PeekableIterator;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class SortedScannerIterable<T extends Comparable<? super T>>
implements PeekableIterable<T> {

	protected SortedScanner<T> scanner;
	
	
	public SortedScannerIterable(SortedScanner<T> scanner){
		this.scanner = scanner;
	}
	
	
	@Override
	public PeekableIterator<T> iterator() {
		return new SortedScannerIterator<T>(scanner);
	}
	
	
	public SortedScanner<T> getScanner(){
		return scanner;
	}
	
}
