package com.hotpads.util.core.iterable.scanner.iterable;

import java.util.Iterator;

import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.iterable.scanner.Scanner;

public class ScannerIterator<T> implements Iterator<T>{

	private final Scanner<T> scanner;
	private T peeked;

	public ScannerIterator(Scanner<T> scanner){
		this.scanner = scanner;
	}


	@Override
	public boolean hasNext() {
		if(peeked!=null){
			return true;
		}
		if(!scanner.advance()){
			return false;
		}
		peeked = scanner.getCurrent();
		return true;
	}

	@Override
	public T next() {
		if(!hasNext()){
			return null;
		}
		T ret = peeked;
		if(scanner.advance()){
			peeked = scanner.getCurrent();
		}else{
			peeked = null;
		}
		return ret;

	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}

}
