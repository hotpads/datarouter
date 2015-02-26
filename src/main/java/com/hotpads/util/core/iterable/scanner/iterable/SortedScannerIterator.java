package com.hotpads.util.core.iterable.scanner.iterable;

import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.iterable.PeekableIterator;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class SortedScannerIterator<T extends Comparable<? super T>>
implements PeekableIterator<T>{
	
	protected SortedScanner<T> scanner;
	protected T peeked;
	
	public SortedScannerIterator(SortedScanner<T> scanner){
		this.scanner = scanner;
		peek();
	}
	
	@Override
	public T peek() {
		if(peeked!=null){ return peeked; }
		if(scanner.getCurrent()==null){
			scanner.advance();
		}
		peeked = scanner.getCurrent();
		return peeked;
	}

	@Override
	public boolean hasNext() {
		if(peeked!=null){ return true; }
		if(!scanner.advance()){ return false; }
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
