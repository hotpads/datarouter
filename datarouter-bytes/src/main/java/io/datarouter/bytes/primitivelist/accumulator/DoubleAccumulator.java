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
package io.datarouter.bytes.primitivelist.accumulator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.DoubleStream;

/**
 * Accumulates a large quantity of primitive doubles.
 * Stores them in paged primitive double arrays.
 * Avoids a large resizing (usually doubling) of the backing array.
 * Not thread safe.
 *
 * This could be promoted to datarouter, but we'd want to improve performance using bit shifting.
 * It could also be enhanced into a PrimitiveDoubleList, but would need support for more operations, more tests.
 */
public class DoubleAccumulator
implements Iterable<Double>{

	private static final int INITIAL_NUM_PAGES = 4;
	private static final int DEFAULT_PAGE_SIZE = 32;// 256 bytes memory

	private final int pageSize;
	private final int pageBits;
	private final int pageShift;
	private double[][] pages;
	private int size;

	/*-------- construct ----------*/

	public DoubleAccumulator(){
		this(DEFAULT_PAGE_SIZE);
	}

	public DoubleAccumulator(int requestedPageSize){
		pageSize = Integer.highestOneBit(requestedPageSize);// round down to nearest power of 2
		pageBits = pageSize - 1;
		pageShift = Integer.bitCount(pageBits);
		pages = new double[INITIAL_NUM_PAGES][];
	}

	/*-------- write ----------*/

	public void add(double value){
		int pageIndex = pageIndex(size);
		int indexInPage = indexInPage(size);
		if(indexInPage == 0){// new page
			if(pageIndex == pages.length){// is pages array full?
				expandPagesArray();
			}
			pages[pageIndex] = new double[pageSize];
		}
		pages[pageIndex][indexInPage] = value;
		++size;
	}

	public void addMulti(double[] values){
		for(int i = 0; i < values.length; ++i){
			add(values[i]);
		}
	}

	public void addAll(List<Double> values){
		values.forEach(this::add);
	}

	public double set(int index, double value){
		Objects.checkIndex(index, size);
		double[] page = page(index);
		int indexInPage = indexInPage(index);
		double previous = page[indexInPage];
		page[indexInPage] = value;
		return previous;
	}

	/*---------- read ----------*/

	public boolean isEmpty(){
		return size == 0;
	}

	public int size(){
		return size;
	}

	public double get(int index){
		Objects.checkIndex(index, size);
		double[] page = pages[pageIndex(index)];
		return page[indexInPage(index)];
	}

	public double[] toPrimitiveArray(){
		double[] output = new double[size];
		int index = 0;
		while(index < size){
			int pageLength = Math.min(pageSize, size - index);
			double[] page = page(index);
			System.arraycopy(page, 0, output, index, pageLength);
			index += pageLength;
		}
		return output;
	}

	public DoubleStream stream(){
		return Arrays.stream(pages)
				.flatMapToDouble(Arrays::stream)
				.limit(size);
	}

	@Override
	public Iterator<Double> iterator(){
		return stream().iterator();
	}

	/*-------- private ----------*/

	private void expandPagesArray(){
		int newPageArrayLength = pages.length == 0 ? 1 : pages.length * 2;
		var newPages = new double[newPageArrayLength][];
		System.arraycopy(pages, 0, newPages, 0, pages.length);
		pages = newPages;
	}

	private int pageIndex(int index){
		return index >>> pageShift;
	}

	private double[] page(int index){
		int pageIndex = pageIndex(index);
		return pages[pageIndex];
	}

	private int indexInPage(int index){
		return index & pageBits;
	}

}
