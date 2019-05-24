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
package io.datarouter.util.iterable.scanner.imp;

import java.util.ArrayList;

import io.datarouter.util.iterable.scanner.sorted.BaseSortedScanner;

/**
 * Users of this class can assume it will keep RandomAccess performance, currently implemented with ArrayList
 */
public class ListBackedSortedScanner<T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	private final ArrayList<T> sortedItems;
	private int currentIndex;

	public ListBackedSortedScanner(ArrayList<T> sortedItems){
		this.sortedItems = sortedItems;
		this.currentIndex = -1;
	}

	@Override
	public boolean advance(){
		++currentIndex;
		return currentIndex < sortedItems.size();
	}

	/**
	 * Exception is thrown at invalid index, like before calling advance() or calling it too many times.
	 */
	@Override
	public T getCurrent(){
		return sortedItems.get(currentIndex);
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + currentIndex + ":" + sortedItems.get(currentIndex) + "]";
	}

}
