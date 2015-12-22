package com.hotpads.datarouter.client.imp.noop;

import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.BaseScanner;

public class EmptySortedScannerIterable<T extends Comparable<? super T>> extends SingleUseScannerIterable<T>{

	public EmptySortedScannerIterable(){
		super(new BaseScanner<T>(){

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
