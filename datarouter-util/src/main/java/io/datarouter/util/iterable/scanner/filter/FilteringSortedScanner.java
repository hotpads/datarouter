package io.datarouter.util.iterable.scanner.filter;

import io.datarouter.util.iterable.scanner.Scanner;
import io.datarouter.util.iterable.scanner.sorted.BaseSortedScanner;

/**
 * wraps a scanner rather than extending one.  does not hold a copy of the current element
 *
 * keeps advancing the underlying scanner until filter.include(..) returns true
 */
public class FilteringSortedScanner<T extends Comparable<? super T>> extends BaseSortedScanner<T>{

	protected Scanner<T> scanner;
	protected Filter<T> filter;

	public FilteringSortedScanner(Scanner<T> scanner, Filter<T> filter){
		this.scanner = scanner;
		this.filter = filter;
	}

	@Override
	public boolean advance(){
		do{
			boolean foundSomething = scanner.advance();//move to the next unfiltered item
			if(!foundSomething){//there weren't any more items, filtered or unfiltered
				return false;
			}
		//if current doesn't pass the filter, move to the next one
		}while(FilterTool.excludes(filter, scanner.getCurrent()));
		return true;//current passed the filter, so indicate that our advance was successful
	}

	@Override
	public T getCurrent(){
		return scanner.getCurrent();
	}
}
