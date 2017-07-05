package io.datarouter.util.iterable.scanner.iterable;

import java.util.Iterator;

import io.datarouter.util.exception.NotImplementedException;
import io.datarouter.util.iterable.scanner.Scanner;

public class ScannerIterator<T> implements Iterator<T>{

	private final Scanner<T> scanner;
	private T peeked;

	public ScannerIterator(Scanner<T> scanner){
		this.scanner = scanner;
	}


	@Override
	public boolean hasNext(){
		if(peeked != null){
			return true;
		}
		if(!scanner.advance()){
			return false;
		}
		peeked = scanner.getCurrent();
		return true;
	}

	@Override
	public T next(){
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
	public void remove(){
		throw new NotImplementedException();
	}

}
