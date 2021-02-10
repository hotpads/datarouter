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
package io.datarouter.util.array;

import java.util.Iterator;

public class PagedObjectArray<T> implements Iterable<T>{

	private static final int INITIAL_NUM_PAGES = 4;

	private final int pageSize;
	private final int pageBits;
	private final int pageShift;
	private T[][] pages;
	private int numItems;

	public PagedObjectArray(int requestedPageSize){
		pageSize = Integer.highestOneBit(requestedPageSize);// round down to nearest power of 2
		pageBits = pageSize - 1;
		pageShift = Integer.bitCount(pageBits);
		pages = makePagesArray(INITIAL_NUM_PAGES);
		numItems = 0;
	}

	public PagedObjectArray<T> add(T value){
		int pageIndex = numItems >>> pageShift;
		int indexInPage = numItems & pageBits;
		if(indexInPage == 0){// new page
			if(pageIndex == pages.length){// is pages array full?
				expandPagesArray();
			}
			pages[pageIndex] = makePage(pageSize);
		}
		pages[pageIndex][indexInPage] = value;
		++numItems;
		return this;
	}

	public int size(){
		return numItems;
	}

	public T get(int index){
		int pageIndex = index >>> pageShift;
		int indexInPage = index & pageBits;
		return pages[pageIndex][indexInPage];
	}

	public T[] concat(){
		T[] output = makePage(size());
		int count = 0;
		int remaining = numItems;
		int nextPage = 0;
		while(remaining > 0){
			int thisPageSize = Math.min(pageSize, remaining);
			System.arraycopy(pages[nextPage], 0, output, count, thisPageSize);
			count += thisPageSize;
			remaining -= thisPageSize;
			++nextPage;
		}
		return output;
	}

	/*----------- private -------------*/

	private void expandPagesArray(){
		int newPageArrayLength = pages.length == 0 ? 1 : pages.length * 2;
		T[][] newPages = makePagesArray(newPageArrayLength);
		System.arraycopy(pages, 0, newPages, 0, pages.length);
		pages = newPages;
	}

	@SuppressWarnings("unchecked")
	private T[] makePage(int length){
		return (T[])new Object[length];
	}

	@SuppressWarnings("unchecked")
	private T[][] makePagesArray(int length){
		return (T[][])new Object[length][];
	}

	/*----------- Iterable -------------*/

	@Override
	public Iterator<T> iterator(){
		return new PagedObjectArrayIterator();
	}

	private class PagedObjectArrayIterator implements Iterator<T>{

		int index;

		PagedObjectArrayIterator(){
			index = 0;
		}

		@Override
		public boolean hasNext(){
			return index < numItems;
		}

		@Override
		public T next(){
			return get(index++);
		}

	}

}