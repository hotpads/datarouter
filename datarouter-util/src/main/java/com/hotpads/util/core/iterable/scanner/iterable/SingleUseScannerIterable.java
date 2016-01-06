package com.hotpads.util.core.iterable.scanner.iterable;

import com.hotpads.util.core.iterable.scanner.Scanner;

public class SingleUseScannerIterable<T> implements Iterable<T> {

	private final Scanner<T> scanner;
	private boolean createdIterator = false;

	public SingleUseScannerIterable(Scanner<T> scanner){
		this.scanner = scanner;
	}

	@Override
	public ScannerIterator<T> iterator(){
		if(createdIterator){
			throw new RuntimeException("the only Iterator has already been created");
		}
		createdIterator = true;
		return new ScannerIterator<>(scanner);
	}

}
