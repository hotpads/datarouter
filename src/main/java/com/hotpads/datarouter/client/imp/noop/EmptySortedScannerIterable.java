package com.hotpads.datarouter.client.imp.noop;

import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;

public class EmptySortedScannerIterable<T extends Comparable<? super T>> extends SortedScannerIterable<T>{

	public EmptySortedScannerIterable(){
		super(new BaseSortedScanner<T>(){

			@Override
			public T getCurrent(){
				return null;
			}

			@Override
			public boolean advance(){
				return false;
			}
		});
	}

}
