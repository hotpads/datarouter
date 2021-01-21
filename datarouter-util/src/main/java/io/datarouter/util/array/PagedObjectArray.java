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

public class PagedObjectArray<T>{

	private final int pageSize;
	private T[][] pages;
	private int numPages;// allocated pages which can be less than pages.length
	private int numItems;
	private int nextItemOffset;

	public PagedObjectArray(int requestedPageSize){
		pageSize = Integer.highestOneBit(requestedPageSize);// round down to nearest power of 2
		pages = makePagesArray(0);
		numPages = 0;
		numItems = 0;
		nextItemOffset = 0;
	}

	public PagedObjectArray<T> add(T value){
		addPageIfFull();
		int lastPageIndex = numPages - 1;
		T[] lastPage = pages[lastPageIndex];
		lastPage[nextItemOffset] = value;
		++numItems;
		++nextItemOffset;
		if(nextItemOffset == pageSize){
			nextItemOffset = 0;
		}
		return this;
	}

	public int size(){
		return numItems;
	}

	public T get(int index){
		int pageIndex = index / pageSize;
		int indexInPage = index % pageSize;
		T[] page = pages[pageIndex];
		return page[indexInPage];
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

	private void addPageIfFull(){
		if(isLastPageFull()){
			if(isPagesArrayFull()){
				expandPagesArray();
			}
			pages[numPages] = makePage(pageSize);
			++numPages;
		}
	}

	private boolean isLastPageFull(){
		return nextItemOffset == 0;
	}

	private boolean isPagesArrayFull(){
		return numPages == pages.length;
	}

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

}