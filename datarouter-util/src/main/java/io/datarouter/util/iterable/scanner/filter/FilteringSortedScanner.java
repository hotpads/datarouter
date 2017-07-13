/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
