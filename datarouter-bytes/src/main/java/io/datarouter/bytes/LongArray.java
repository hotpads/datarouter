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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

//Implement RandomAccess for proper treatment by some JDK utils
public class LongArray implements List<Long>, RandomAccess{

	private static final int DEFAULT_INITIAL_CAPACITY = 2;

	private long[] array;
	private int size;

	public LongArray(){
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public LongArray(int initialCapacity){
		array = new long[initialCapacity];
		size = 0;
	}

	public LongArray(long[] toWrap){
		this.array = toWrap;
		size = array.length;
	}

	public LongArray(Collection<Long> elements){
		this(elements == null ? 0 : elements.size());
		addAll(0, elements);
	}

	private void expandAndShiftIfNecessary(int insertIndex, int delta){
		int neededSize = size + delta;
		if(neededSize > array.length){
			int newSize = Integer.highestOneBit(neededSize) << 1;
			var newArray = new long[newSize];
			System.arraycopy(array, 0, newArray, 0, size);
			this.array = newArray;
		}
		System.arraycopy(array, insertIndex, array, insertIndex + delta, size - insertIndex);
	}

	protected void shrinkIfNecessary(){
		// when shrinking, the size has already been adjusted
		if(size < array.length / 4){
			int newSize = Integer.highestOneBit(size) << 2;
			var newArray = new long[newSize];
			System.arraycopy(array, 0, newArray, 0, size);
			this.array = newArray;
		}
	}

	@Override
	public void add(int index, Long value){
		expandAndShiftIfNecessary(index, 1);
		array[index] = value;
		++size;
	}

	public void add(int index, long value){
		expandAndShiftIfNecessary(index, 1);
		array[index] = value;
		++size;
	}

	@Override
	public boolean add(Long value){
		add(size, value);
		return true;// collections return true if they were modified
	}

	public boolean add(long value){
		add(size, value);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Long> values){
		return addAll(size, values);
	}

	@Override
	public boolean addAll(int firstIndex, Collection<? extends Long> values){
		int delta = values == null ? 0 : values.size();
		if(delta == 0){
			return false;
		}
		expandAndShiftIfNecessary(firstIndex, delta);
		int nextIndex = firstIndex;
		for(Long value : values){
			array[nextIndex] = value;
			++nextIndex;
		}
		size += delta;
		return true;
	}

	@Override
	public void clear(){
		array = new long[1];
		size = 0;
	}

	@Override
	public boolean contains(Object obj){
		if(!(obj == null || obj instanceof Long)){
			return false;
		}
		long value = (Long)obj;
		for(int i = 0; i < size; ++i){
			if(array[i] == value){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> objects){
		if(objects == null){
			throw new IllegalArgumentException("no nulls");
		}
		if(objects.size() == 0){
			return true;
		}
		for(Object obj : objects){
			if(!contains(obj)){
				return false;
			}
		}
		return true;
	}

	@Override
	public Long get(int index){
		if(index >= size){
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return array[index];
	}

	public long getPrimitive(int index){
		if(index >= size){
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return array[index];
	}

	@Override
	public int indexOf(Object obj){
		if(!(obj == null || obj instanceof Long)){
			return -1;
		}
		long value = (Long)obj;
		for(int i = 0; i < size; ++i){
			if(array[i] == value){
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty(){
		return size == 0;
	}

	@Override
	public Iterator<Long> iterator(){
		return new LongArrayIterator(this);
	}

	@Override
	public int lastIndexOf(Object obj){
		if(!(obj == null || obj instanceof Long)){
			return -1;
		}
		long value = (Long)obj;
		for(int i = size - 1; i >= 0; --i){
			if(array[i] == value){
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<Long> listIterator(){
		return new LongArrayIterator(this);
	}

	@Override
	public ListIterator<Long> listIterator(int startIndex){
		return new LongArrayIterator(this, startIndex);
	}

	@Override
	public Long remove(int index){
		if(index >= size){
			throw new IllegalArgumentException("out of range");
		}
		long value = array[index];
		if(index < size - 1){
			System.arraycopy(array, index + 1, array, index, size - index - 1);
		}// otherwise we can just decrement the size
		--size;
		shrinkIfNecessary();
		return value;
	}

	@Override
	public boolean remove(Object obj){
		if(!(obj == null || obj instanceof Long)){
			return false;
		}
		long value = (Long)obj;
		for(int i = 0; i < size; ++i){
			if(array[i] == value){
				remove(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> objects){
		// TODO could flag them all and copy/resize at once
		boolean modified = false;
		for(Object obj : objects){
			if(remove(obj)){
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> objects){
		boolean modified = false;
		for(int i = 0; i < size; ++i){
			if(!objects.contains(array[i])){
				remove(i);
				--i;// need to check the same index again because we shifted left
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public Long set(int index, Long value){
		if(index < 0 || index >= size){
			throw new IllegalArgumentException(index + " " + size);
		}
		array[index] = value;
		return value;
	}

	@Override
	public int size(){
		return this.size;
	}

	@Override
	public List<Long> subList(int fromIndex, int toIndex){
		int newSize = toIndex - fromIndex;
		var result = new LongArray(newSize);
		System.arraycopy(array, fromIndex, result.array, 0, newSize);
		return result;
	}

	@Override
	public Object[] toArray(){
		var wrapperArray = new Long[size];
		return toArray(wrapperArray);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] wrapperArray){
		for(int i = 0; i < size; ++i){
			Long longValue = array[i];
			wrapperArray[i] = (T)longValue;
		}
		return wrapperArray;
	}

	// copied from AbstractList
	@Override
	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(!(obj instanceof List)){
			return false;
		}

		ListIterator<Long> e1 = listIterator();
		ListIterator<?> e2 = ((List<?>)obj).listIterator();
		while(e1.hasNext() && e2.hasNext()){
			Long o1 = e1.next();
			Object o2 = e2.next();
			if(!(o1 == null ? o2 == null : o1.equals(o2))){
				return false;
			}
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	@Override
	public int hashCode(){
		int hashCode = 1;
		for(Long element : this){
			hashCode = 31 * hashCode + (element == null ? 0 : element.hashCode());
		}
		return hashCode;
	}

	@Override
	public String toString(){
		return String.format("LongArray [array=%s, size=%d]", Arrays.toString(array), size);
	}

	public long[] getPrimitiveArray(){
		if(size == array.length){
			return array;
		}
		var copy = new long[size];
		System.arraycopy(array, 0, copy, 0, size);
		return copy;
	}

	public LongArray trimmedCopy(){
		return new LongArray(Arrays.copyOf(array, size));
	}

	public LongArray sortInPlace(){
		Arrays.sort(array, 0, size);
		return this;
	}

	public LongArray copyDedupeConsecutive(){
		if(size == 0){
			return new LongArray(0);
		}
		int newSize = 1;
		for(int i = 1; i < size; ++i){
			if(array[i] != array[i - 1]){
				++newSize;
			}
		}
		var newArray = new long[newSize];
		newArray[0] = array[0];
		int nextNewIndex = 1;
		for(int i = 1; i < size; ++i){
			if(array[i] != array[i - 1]){
				newArray[nextNewIndex] = array[i];
				++nextNewIndex;
			}
		}
		return new LongArray(newArray);
	}

	public static class LongArrayIterator implements ListIterator<Long>{

		private LongArray wrapper;
		private int index;

		public LongArrayIterator(LongArray wrapper){
			this.wrapper = wrapper;
			index = -1;
		}

		public LongArrayIterator(LongArray wrapper, int startIndex){
			this(wrapper);
			index = startIndex - 1;
		}

		@Override
		public boolean hasNext(){
			return index + 1 < wrapper.size;
		}

		@Override
		public Long next(){
			++index;
			return wrapper.get(index);
		}

		@Override
		public void remove(){
			wrapper.remove(index);
		}

		@Override
		public void add(Long value){
			wrapper.add(index, value);
		}

		@Override
		public boolean hasPrevious(){
			return index > 0;
		}

		@Override
		public int nextIndex(){
			return index + 1;
		}

		@Override
		public Long previous(){
			--index;
			return wrapper.get(index);
		}

		@Override
		public int previousIndex(){
			return index;
		}

		@Override
		public void set(Long value){
			wrapper.set(index, value);
		}

	}

}
