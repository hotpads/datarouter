/*
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
package io.datarouter.scanner;

import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * A RandomAccess List that grows more efficiently than ArrayList.
 * Reduces allocations and copying of data as the List grows.
 * Uses slightly more memory than ArrayList, but performance should be more predictable.
 * Memory overhead is reduced with larger pageSize.
 */
public class PagedList<E> extends AbstractList<E>
implements RandomAccess{

	private static final int INITIAL_NUM_PAGES = 4;
	private static final int DEFAULT_PAGE_SIZE = 32;

	private final int pageSize;
	private final int pageBits;
	private final int pageShift;
	private Object[][] pages;
	private int size;

	/*---------- construct ----------*/

	public PagedList(int requestedPageSize){
		pageSize = Integer.highestOneBit(requestedPageSize);// round down to nearest power of 2
		pageBits = pageSize - 1;
		pageShift = Integer.bitCount(pageBits);
		pages = new Object[INITIAL_NUM_PAGES][];
		size = 0;
	}

	public PagedList(){
		this(DEFAULT_PAGE_SIZE);
	}

	public PagedList(Iterable<E> iterable){
		this(DEFAULT_PAGE_SIZE);
		iterable.forEach(this::add);
	}

	public PagedList(Scanner<E> scanner){
		this(DEFAULT_PAGE_SIZE);
		try(Scanner<E> $ = scanner){
			while(scanner.advance()){
				add(scanner.current());
			}
		}
	}

	/*----------- AbstractList ------------*/

	@Override
	public boolean add(E value){
		int pageIndex = pageIndex(size);
		int indexInPage = indexInPage(size);
		if(indexInPage == 0){// new page
			if(pageIndex == pages.length){// is pages array full?
				expandPagesArray();
			}
			pages[pageIndex] = new Object[pageSize];
		}
		pages[pageIndex][indexInPage] = value;
		++size;
		return true;
	}

	@Override
	public void add(int index, E element){
		add(null);// make new space at the end
		// shift things at/after the index down
		for(int i = size - 1; i > index; --i){
			set(i, get(i - 1));
		}
		set(index, element);
	}

	@Override
	@SuppressWarnings("unchecked")
	public E get(int index){
		Object[] page = page(index);
		int indexInPage = indexInPage(index);
		return (E)page[indexInPage];
	}

	@Override
	public E remove(int index){
		E previous = get(index);
		// shift things after the index up
		for(int i = index; i < size - 1; ++i){
			set(i, get(i + 1));
		}
		set(size - 1, null);
		--size;
		//TODO possibly shrink the parent array
		return previous;
	}

	@Override
	public E set(int index, E element){
		Object[] page = page(index);
		int indexInPage = indexInPage(index);
		@SuppressWarnings("unchecked")
		E previous = (E)page[indexInPage];
		page[indexInPage] = element;
		return previous;
	}

	@Override
	public int size(){
		return size;
	}

	/*----------- private ---------------*/

	private void expandPagesArray(){
		int newPageArrayLength = pages.length == 0 ? 1 : pages.length * 2;
		var newPages = new Object[newPageArrayLength][];
		System.arraycopy(pages, 0, newPages, 0, pages.length);
		pages = newPages;
	}

	private Object[] page(int index){
		int pageIndex = pageIndex(index);
		return pages[pageIndex];
	}

	private int pageIndex(int index){
		return index >>> pageShift;
	}

	private int indexInPage(int index){
		return index & pageBits;
	}

}
