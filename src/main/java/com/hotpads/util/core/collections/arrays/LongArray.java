package com.hotpads.util.core.collections.arrays;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class LongArray implements List<Long>, Comparable<List<Long>>{
	
	public static final int DEFAULT_initialCapacity = 2;
	public static final long NULL = Long.MIN_VALUE;//compares before all other longs
	
	long[] array;
	int size;
	
	public LongArray(){
		this(DEFAULT_initialCapacity);
	}
	
	public LongArray(int initialCapacity){
		this.array = new long[initialCapacity];
		this.size = 0;
	}
	
	public LongArray(long[] toWrap){
		this.array = toWrap;
		this.size = array.length;
	}
	
	public LongArray(Collection<Long> elements){
		this(CollectionTool.size(elements));
		for(Long element : elements){
			this.add(element);
		}
	}
		
	protected void expandIfNecessary(int delta){
		int neededSize = this.size + delta;
		if(neededSize > this.array.length){
			int newSize = Integer.highestOneBit(neededSize)<<1;
//			System.out.println("resizing from "+this.array.length+" to "+newSize);
			long[] newArray = new long[newSize];
			System.arraycopy(this.array, 0, newArray, 0, this.size);
			this.array = newArray;
		}
	}
	
	protected void shrinkIfNecessary(int delta){
		//when shrinking, the size has already been adjusted
		if(this.size < this.array.length / 4){
			int newSize = Integer.highestOneBit(this.size)<<2;
//			System.out.println("resizing from "+this.array.length+" to "+newSize);
			long[] newArray = new long[newSize];
			System.arraycopy(this.array, 0, newArray, 0, this.size);
			this.array = newArray;
		}
	}

	@Override
	public void add(int index, Long value) {
		this.expandIfNecessary(1);
		this.array[index] = value==null?NULL:value;
		++this.size;
	}
	
	public void add(int index, long value) {
		this.expandIfNecessary(1);
		this.array[index] = value;
		++this.size;
	}

	@Override
	public boolean add(Long value) {
		this.add(this.size, value);
		return true;//collections return true if they were modified
	}
	
	public boolean add(long value){
		this.add(this.size, value);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Long> values) {
		for(Long value : CollectionTool.nullSafe(values)){
			add(value);
		}
		return true;
	}

	@Override
	public boolean addAll(int firstIndex, Collection<? extends Long> values) {
		int delta = CollectionTool.size(values);
		if(delta==0){ return false; }
		this.expandIfNecessary(delta);
		System.arraycopy(this.array, firstIndex, this.array, firstIndex+delta, delta);
		int nextIndex = firstIndex;
		for(Long value : values){
			this.array[nextIndex] = value==null?NULL:value;
			++nextIndex;
		}
		this.size += delta;
		return true;
	}

	@Override
	public void clear() {
		this.array = new long[1];
		this.size = 0;
	}

	@Override
	public boolean contains(Object obj) {
		if(! (obj==null || obj instanceof Long)){ return false; }
		long value;
		if(obj==null){
			value = NULL;
		}else{
			value = (Long)obj;
		}
		for(int i=0; i < this.size; ++i){
			if(this.array[i] == value){ return true; }
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> objects) {
		if(objects==null){ throw new IllegalArgumentException("no nulls"); }
		if(objects.size()==0){ return true; }
		for(Object obj : objects){
			if(!contains(obj)){ return false; }
		}
		return true;
	}

	@Override
	public Long get(int index) {
		if(index > size){ throw new ArrayIndexOutOfBoundsException(index); }
		return this.array[index]==NULL?null:this.array[index];
	}
	
	public long getPrimitive(int index){
		if(index > size){ throw new ArrayIndexOutOfBoundsException(index); }
		return this.array[index];
	}

	@Override
	public int indexOf(Object obj) {
		if(! (obj==null || obj instanceof Long)){ return -1; }
		long value;
		if(obj==null){
			value = NULL;
		}else{
			value = (Long)obj;
		}
		for(int i=0; i < this.size; ++i){
			if(this.array[i] == value){ return i; }
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return this.size==0;
	}

	@Override
	public Iterator<Long> iterator() {
		return new LongArrayIterator(this);
	}

	@Override
	public int lastIndexOf(Object obj) {
		if(! (obj==null || obj instanceof Long)){ return -1; }
		long value;
		if(obj==null){
			value = NULL;
		}else{
			value = (Long)obj;
		}
		for(int i=this.size-1; i >= 0; --i){
			if(this.array[i] == value){ return i; }
		}
		return -1;
	}

	@Override
	public ListIterator<Long> listIterator() {
		return new LongArrayIterator(this);
	}

	@Override
	public ListIterator<Long> listIterator(int startIndex) {
		return new LongArrayIterator(this, startIndex);
	}

	@Override
	public Long remove(int index) {
//		System.out.println("removing idx "+index);
		if(index >= this.size){ throw new IllegalArgumentException("out of range"); }
		long value = this.array[index];
		if(index<size-1){ 
			System.arraycopy(this.array, index+1, this.array, index, this.size-index-1);
		}//otherwise we can just decrement the size
		--this.size;
		this.shrinkIfNecessary(-1);
		return value;
	}

	@Override
	public boolean remove(Object obj) {
		if(! (obj==null || obj instanceof Long)){ return false; }
		long value;
		if(obj==null){
			value = NULL;
		}else{
			value = (Long)obj;
		}
		for(int i=0; i < this.size; ++i){
			if(this.array[i]==value){
				this.remove(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> objects) {
		//TODO could flag them all and copy/resize at once
		boolean modified = false;
		for(Object obj : IterableTool.nullSafe(objects)){
			if(this.remove(obj)){ modified = true; }
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> objects) {
		boolean modified = false;
		for(int i=0; i < this.size; ++i){
			if(!objects.contains(this.array[i]==NULL?null:this.array[i])){
				this.remove(i);
				--i;//need to check the same index again because we shifted left
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public Long set(int index, Long value) {
		if(index < 0 || index >= size){ 
			throw new IllegalArgumentException(index+" "+this.size); 
		}
		this.array[index] = value==null?NULL:value;
		return value;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public List<Long> subList(int fromIndex, int toIndex) {
		int newSize = toIndex - fromIndex;
		LongArray result = new LongArray(newSize);
		System.arraycopy(this.array, fromIndex, result.array, 0, newSize);
		return result;
	}

	@Override
	public Object[] toArray() {
		Long[] wrapperArray = new Long[this.size];
		return toArray(wrapperArray);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] wrapperArray) {
		for(int i=0; i < this.size; ++i){
			wrapperArray[i] = (T)(Long)(this.array[i]==NULL?null:this.array[i]);
		}
		return wrapperArray;
	}

	@Override
	public int compareTo(List<Long> other) {
		return ListTool.compare(this, other);//NULL compares before all other longs
	}
	
	public long[] getPrimitiveArray() {
		if(size == array.length){
			return array;
		}
		
		long[] copy = new long[size];
		System.arraycopy(this.array, 0, copy, 0, this.size);
		
		return copy;
	}
	
	
	
	public static class LongArrayIterator implements ListIterator<Long>{
		
		LongArray wrapper;
		int lastIndex;
		
		public LongArrayIterator(LongArray wrapper){
			this.wrapper = wrapper;
			this.lastIndex = -1;
		}
		
		public LongArrayIterator(LongArray wrapper, int startIndex){
			this(wrapper);
			this.lastIndex = startIndex - 1;
		}

		@Override
		public boolean hasNext() {
			return this.lastIndex + 1 < this.wrapper.size;
		}

		@Override
		public Long next() {
			++this.lastIndex;
			return this.wrapper.get(lastIndex);
		}

		@Override
		public void remove() {
			this.wrapper.remove(this.lastIndex+1);
		}

		@Override
		public void add(Long value) {
			this.wrapper.set(this.lastIndex+1, value);
		}

		@Override
		public boolean hasPrevious() {
			return this.lastIndex > -1;
		}

		@Override
		public int nextIndex() {
			return this.lastIndex + 1;
		}

		@Override
		public Long previous() {
			--this.lastIndex;
			return this.wrapper.get(this.lastIndex+1);
		}

		@Override
		public int previousIndex() {
			return this.lastIndex;
		}

		@Override
		public void set(Long value) {
			this.wrapper.set(this.lastIndex+1, value);
		}
		
	}
	

	
	/************************ tests ***************************************/
	
	public static class Tests{
		List<Long> list = new LongArray();
		int max = 150;
		
		@Test public void testBasics(){
			
			//expanding
			for(long l=0; l < max; ++l){
				list.add(l);
				Assert.assertEquals(l, list.size()-1);
			}
			Assert.assertEquals(max, list.size());
			
			//shrinking
			for(long l=max-1; l >=0; --l){
				list.remove(l);
				Assert.assertEquals(l, list.size());
			}
			Assert.assertEquals(0, list.size());
			
			//re-expanding
			for(long l=0; l < max; ++l){
				list.add(l);
				Assert.assertEquals(l, list.size()-1);
			}
			
			//removing indexes
			long valueRemoved = list.remove(31);
			Assert.assertTrue(31L==valueRemoved);
			Assert.assertEquals(max-1, list.size());
			Assert.assertTrue(list.get(31).equals(32L));
			
			//removing objects
			boolean modified = list.remove(5L);
			Assert.assertTrue(modified);
			Assert.assertEquals(max-2, list.size());
			Assert.assertTrue(list.get(5).equals(6L));
			modified = list.remove(5L);
			Assert.assertFalse(modified);
			Assert.assertEquals(max-2, list.size());
			Assert.assertTrue(list.get(5).equals(6L));
			
			//retain objects
			List<Long> toRetain = ListTool.create(55L,57L,7L);
			modified = list.retainAll(toRetain);
			Assert.assertTrue(modified);
			Assert.assertEquals(3, list.size());
			Assert.assertTrue(list.get(0).equals(7L));
			Assert.assertTrue(list.get(1).equals(55L));
			Assert.assertTrue(list.get(2).equals(57L));
			modified = list.retainAll(ListTool.create(55L,57L,7L));
			Assert.assertFalse(modified);
			Assert.assertEquals(3, list.size());
			Assert.assertTrue(list.get(0).equals(7L));
			Assert.assertTrue(list.get(1).equals(55L));
			Assert.assertTrue(list.get(2).equals(57L));
			
			//nulls
			List<Long> nullableList = new LinkedList<Long>();
			LongArray primitiveList = new LongArray();
			nullableList.add(-7L); primitiveList.add(-7L);
			nullableList.add(null); primitiveList.add(null);
			nullableList.add(-8L); primitiveList.add(-8L);
			nullableList.add(null); primitiveList.add(null);
			nullableList.add(Long.MAX_VALUE); primitiveList.add(Long.MAX_VALUE);
			Assert.assertTrue(5==CollectionTool.size(nullableList));
			Assert.assertTrue(5==CollectionTool.size(primitiveList));
			Assert.assertTrue(0==primitiveList.compareTo(nullableList));
			Assert.assertEquals(null, primitiveList.get(1));
		}
	}
	
}




