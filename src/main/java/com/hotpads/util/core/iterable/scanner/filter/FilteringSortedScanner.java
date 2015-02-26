package com.hotpads.util.core.iterable.scanner.filter;

import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

/**
 * wraps a scanner rather than extending one.  does not hold a copy of the current element
 * 
 * keeps advancing the underlying scanner until filter.include(..) returns true
 */
public class FilteringSortedScanner<T extends Comparable<? super T>> 
extends BaseSortedScanner<T>{

	protected SortedScanner<T> scanner;
	protected Filter<T> filter;
	
	
	public FilteringSortedScanner(SortedScanner<T> scanner, Filter<T> filter){
		this.scanner = scanner;
		this.filter = filter;
	}
	
	
	@Override
	public boolean advance(){
		do{
			boolean foundSomething = scanner.advance();//move to the next unfiltered item
			if(!foundSomething){ return false; }//there weren't any more items, filtered or unfiltered
		}while(FilterTool.excludes(filter, scanner.getCurrent()));//if current doesn't pass the filter, move to the next one
		return true;//current passed the filter, so indicate that our advance was successful
	}
	
	@Override
	public T getCurrent() {
		return scanner.getCurrent();
	}
}
