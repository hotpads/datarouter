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
package io.datarouter.bytes;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class PagedObjectArray<E> implements List<E>{

	private static final int INITIAL_NUM_PAGES = 4;

	private final int pageSize;
	private final int pageBits;
	private final int pageShift;
	private Object[][] pages;
	private int numItems;

	public PagedObjectArray(){
		this(32);
	}

	public PagedObjectArray(int requestedPageSize){
		pageSize = Integer.highestOneBit(requestedPageSize);// round down to nearest power of 2
		pageBits = pageSize - 1;
		pageShift = Integer.bitCount(pageBits);
		pages = new Object[INITIAL_NUM_PAGES][];
		numItems = 0;
	}

	public E[] toArrayOf(Class<E> clazz){
		@SuppressWarnings("unchecked")
		E[] array = (E[])Array.newInstance(clazz, numItems);
		return toArray(array);
	}

	private void expandPagesArray(){
		int newPageArrayLength = pages.length == 0 ? 1 : pages.length * 2;
		var newPages = new Object[newPageArrayLength][];
		System.arraycopy(pages, 0, newPages, 0, pages.length);
		pages = newPages;
	}

	/*--------------------- List -----------------*/

	@Override
	public boolean add(E value){
		int pageIndex = numItems >>> pageShift;
		int indexInPage = numItems & pageBits;
		if(indexInPage == 0){// new page
			if(pageIndex == pages.length){// is pages array full?
				expandPagesArray();
			}
			pages[pageIndex] = new Object[pageSize];
		}
		pages[pageIndex][indexInPage] = value;
		++numItems;
		return true;
	}

	@Override
	public void add(int index, E element){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> objs){
		objs.forEach(this::add);
		return objs.size() > 0;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> collection){
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object theirs){
		return stream()
				.anyMatch(ours -> Objects.equals(ours, theirs));
	}

	@Override
	public boolean containsAll(Collection<?> theirs){
		return theirs.stream()
				.allMatch(this::contains);
	}

	@Override
	@SuppressWarnings("unchecked")
	public E get(int index){
		int pageIndex = index >>> pageShift;
		int indexInPage = index & pageBits;
		return (E)pages[pageIndex][indexInPage];
	}

	@Override
	public int indexOf(Object obj){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty(){
		return numItems == 0;
	}

	@Override
	public Iterator<E> iterator(){
		return new PagedObjectArrayIterator();
	}

	@Override
	public int lastIndexOf(Object obj){
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(){
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(int index){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object obj){
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> objs){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> objs){
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element){
		throw new UnsupportedOperationException();
	}

	@Override
	public int size(){
		return numItems;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex){
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray(){
		return toArray(new Object[numItems]);
	}

	@Override
	public <T> T[] toArray(T[] array){
		@SuppressWarnings("unchecked")
		T[] output = array.length == numItems
				? array
				: (T[])Array.newInstance(array.getClass().getComponentType(), numItems);
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

	/*--------------- Iterator --------------*/

	private class PagedObjectArrayIterator implements Iterator<E>{

		int index;

		PagedObjectArrayIterator(){
			index = 0;
		}

		@Override
		public boolean hasNext(){
			return index < numItems;
		}

		@Override
		public E next(){
			return get(index++);
		}

	}

}
