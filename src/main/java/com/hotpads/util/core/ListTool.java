package com.hotpads.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;


public class ListTool {

	protected static final List<?> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<Object>(0));

	@SuppressWarnings("unchecked")
	public static <T> List<T> emptyFinal(){
		return (List<T>)EMPTY_LIST;
	}

	@Deprecated
	public static <T> List<T> create(){
		return createArrayList();
	}

	@Deprecated
	public static <T> LinkedList<T> createLinkedList(){
		return new LinkedList<T>();
	}

	@Deprecated
	public static <T> ArrayList<T> createArrayList(){
		return new ArrayList<T>();
	}

	public static <T> List<T> nullSafeWrap(T t){
		List<T> list = new LinkedList<T>();
		if(t!=null){
			list.add(t);
		}
		return list;
	}

	public static <T> List<T> wrap(T t){
		return nullSafeWrap(t);
	}

	@Deprecated
	public static <T> ArrayList<T> createArrayList(int size){
		return new ArrayList<T>(size);
	}

	public static <T> ArrayList<T> createArrayListWithSize(Collection<?> c){
		return createArrayList(CollectionTool.sizeNullSafe(c));
	}

	public static <T> ArrayList<T> createArrayListAndInitialize(int size){
		ArrayList<T> out = new ArrayList<T>(size);
		for(int i=0; i < size; ++i){
			out.add(null);
		}
		return out;
	}

	public static <T> ArrayList<T> create(T... in){
		return createArrayList(in);
	}

	public static <T> LinkedList<T> createLinkedList(T... in){
		LinkedList<T> out = new LinkedList<T>();
		if(ArrayTool.isEmpty(in)){
			return out;
		}
		for(int i=0; i < in.length; ++i){
			out.add(in[i]);
		}
		return out;
	}

	public static <T> ArrayList<T> createArrayList(T... in){
		ArrayList<T> out = new ArrayList<T>(ArrayTool.nullSafeLength(in));
		if(ArrayTool.isEmpty(in)){
			return out;
		}
		for(int i=0; i < in.length; ++i){
			out.add(in[i]);
		}
		return out;
	}

	public static <T> List<T> createLinkedList(Collection<T> in){
		List<T> out = new LinkedList<T>();
		if(CollectionTool.isEmpty(in)){
			return out;
		}
		out.addAll(in);
		return out;
	}

	public static <T> ArrayList<T> createArrayList(Iterator<T> ins){
		ArrayList<T> outs = createArrayList();
		while(ins.hasNext()){ outs.add(ins.next()); }
		return outs;
	}

	public static <T> ArrayList<T> createArrayList(Iterable<T> ins){
		ArrayList<T> outs = new ArrayList<T>();
		for(T in : IterableTool.nullSafe(ins)) {
			outs.add(in);
		}
		return outs;
	}

	public static <T> ArrayList<T> createArrayList(Collection<T> in){
		ArrayList<T> out = new ArrayList<T>(CollectionTool.sizeNullSafe(in));
		if(CollectionTool.isEmpty(in)){
			return out;
		}
		out.addAll(in);
		return out;
	}

	public static <T> List<T> nullSafe(List<T> in){
		if(in==null){ return new LinkedList<T>(); }
		return in;
	}

	public static <T> List<T> nullForEmpty(List<T> in){
		if(CollectionTool.isEmpty(in)){ return null; }
		return in;
	}

	public static <T> List<T> nullSafeLinked(List<T> in){
		if(in==null){ return new LinkedList<T>(); }
		return in;
	}

	public static <T> List<T> nullSafeArray(List<T> in){
		if(in==null){ return new ArrayList<T>(); }
		return in;
	}

	public static <T extends Comparable<? super T>> ArrayList<T> createSortedCopy(Collection<T> ins){
		ArrayList<T> outs = new ArrayList<T>(CollectionTool.size(ins));
		for(T in : IterableTool.nullSafe(ins)) {
			outs.add(in);
		}
		Collections.sort(outs);
		return outs;
	}

	public static <T>List<T> asList(Collection<T> coll){
		if(coll == null){ return new LinkedList<>(); }
		if(coll instanceof List){
			return (List<T>)coll;
		}else{
			return new LinkedList<>(coll);
		}
	}

	/*********************** concatenate ********************************/

	public static <T> ArrayList<T> concatenateIterables(Iterable<? extends Iterable<T>> ins){
		ArrayList<T> outs = new ArrayList<>();
		for(Iterable<T> in : ins){
			for(T t : in){
				outs.add(t);
			}
		}
		return outs;
	}

	public static <T> ArrayList<T> concatenate(List<T>... ins){
		int size = CollectionTool.getTotalSizeOfArrayOfCollections(ins);
		ArrayList<T> outs = ListTool.createArrayList(size);
		for(List<T> in : ins){
			outs.addAll(ListTool.nullSafe(in));
		}
		return outs;
	}

	public static <T> ArrayList<T> append(Collection<List<T>> ins){
		int size = CollectionTool.getTotalSizeOfCollectionOfCollections(ins);
		ArrayList<T> outs = ListTool.createArrayList(size);
		for(List<T> in : IterableTool.nullSafe(ins)){
			outs.addAll(ListTool.nullSafe(in));
		}
		return outs;
	}


	/************************ read **********************************/

	public static <T> T nullSafeGet(List<T> list, int index){
		if(list==null){ return null; }
		if(index < 0){ return null; }
		if(index >= list.size()){ return null; }
		return list.get(index);
	}


	/**************************** compare **************************/

	public static <T extends Comparable<T>> int compare(List<T> as, List<T> bs){
		if(as==null){ return bs==null?0:-1; }
		if(bs==null){ return as==null?0:1; }
		Iterator<T> bi = bs.iterator();
		for(T a : as){
			if(!bi.hasNext()){ return 1; } //as are longer than bs
			int comp = ComparableTool.nullFirstCompareTo(a, bi.next());
			if(comp!=0){ return comp; }
		}
		return bi.hasNext()?-1:0;  //bs are longer than as
	}

	public static <T extends Comparable<? super T>> boolean isSorted(List<T> as){
		if(as==null){ return true; }
		T last = null;
		for(T a : as){
			if(ComparableTool.nullFirstCompareTo(last, a) > 0){
				return false;
			}
			last = a;
		}
		return true;
	}
	
	public static <T extends Comparable<T>> boolean areEqual(List<T> aList, List<T> bList){
		return compare(aList, bList) == 0;
	}
	
	public static <T extends Comparable<T>> boolean notEqual(List<T> aList, List<T> bList){
		return compare(aList, bList) != 0;
	}

	/************************** modify *******************************/

	public static <T> List<T> nullSafeLinkedAdd(List<T> list, T newItem){
		list = nullSafeLinked(list);
		if(newItem!=null){ list.add(newItem); }
		return list;
	}

	public static <T> List<T> nullSafeArrayAdd(List<T> list, T newItem){
		list = nullSafeArray(list);
		if(newItem!=null){ list.add(newItem); }
		return list;
	}

	public static <T> List<T> nullSafeLinkedAddAll(List<T> list, Collection<? extends T> newItems){
		list = nullSafeLinked(list);
		list.addAll(CollectionTool.nullSafe(newItems));
		return list;
	}

	public static <T> List<T> nullSafeLinkedAddAll(List<T> list, T... newItems){
		list = nullSafeLinked(list);
		if(ArrayTool.notEmpty(newItems)){
			for(int i=0; i < newItems.length; ++i){
				list.add(newItems[i]);
			}
		}
		return list;
	}

	public static <T> List<T> nullSafeArrayAddAll(List<T> list, Collection<? extends T> newItems){
		list = nullSafeArray(list);
		list.addAll(CollectionTool.nullSafe(newItems));
		return list;
	}

	public static <T> List<T> nullSafeArrayAddAll(List<T> list, T... newItems){
		list = nullSafeArray(list);
		if(ArrayTool.notEmpty(newItems)){
			for(int i=0; i < newItems.length; ++i){
				list.add(newItems[i]);
			}
		}
		return list;
	}

	public static <T> List<T> nullSafeLinkedAddIfNotNull(List<T> list, T newItem){
		list = nullSafeLinked(list);
		if(newItem != null){
			list.add(newItem);
		}
		return list;
	}

	public static <T> List<T> nullSafeArrayAddIfNotNull(List<T> list, T newItem){
		list = nullSafeArray(list);
		if(newItem != null){
			list.add(newItem);
		}
		return list;
	}

	public static <T> List<T> copyOfRange(
			List<T> in, int startInclusive, int endExclusive){
		if(CollectionTool.isEmpty(in)){ return create(); }
		if(startInclusive >= in.size()
				|| endExclusive<=startInclusive
				|| startInclusive<0){
			return create();
		}
		if(endExclusive > in.size()){
			endExclusive = in.size();
		}
		int rangeSize = endExclusive - startInclusive;

		List<T> copy = new ArrayList<T>(rangeSize);
		if(in instanceof RandomAccess){
			for(int i=startInclusive; i < endExclusive; ++i){
				copy.add(in.get(i));
			}
		}else{
			int inIndex = 0;
			for(T t : in){
				if(inIndex >= startInclusive && inIndex < endExclusive){
					copy.add(t);
				}
				++inIndex;
			}
		}
		return copy;
	}

	public static <T> List<T> getFirstNElements(List<T> in, int n) {
		if (CollectionTool.isEmpty(in) || n <= 0) { return create(); }
		return copyOfRange(in, 0, Math.max(0, n));
	}

	public static <T> List<T> getLastNElements(List<T> in, int n) {
		if (CollectionTool.isEmpty(in) || n <= 0) { return create(); }
		return copyOfRange(in, Math.max(0, in.size() - n), in.size());
	}

	public static <T> List<T> removeDupesKeepOrder(List<T> lst) {
		List<T> copy = new ArrayList<T>(lst.size());
		for (T t : lst) {
			if (!copy.contains(t))
				copy.add(t);
		}
		return copy;
	}

	 public static <T> List<T> intersection(List<T> list1, List<T> list2) {
	        List<T> list = new ArrayList<T>();

	        for (T t : list1) {
	            if(list2.contains(t)) {
	                list.add(t);
	            }
	        }

	        return list;
	    }

	 public static <T> List<T> intersection(List<List<T>> list) {
		 if(list.size() <= 0){ return null; }
			List<T> toReturn = list.get(0);
			for(int index = 1; index < list.size(); index++){
				toReturn = ListTool.intersection(toReturn, list.get(index));
			}
			return toReturn;
	    }
	 
	public static <T> List<T> minus( List<T> aList, List<T> bList ) {
			if ( bList == null || bList.size() == 0 ) {
				return aList;
			}
			Set<T> bSet = new HashSet<T>();
			bSet.addAll( bList );
			List<T> result = new ArrayList<T>();
			for ( T item : aList ) {
				if ( !bSet.contains( item ) ) {
					result.add(item);
				}
			}
			return result;
		}
		

	 
	public static class Tests {
		@Test public void checkMinus() {
			List<String> s1 = ListTool.create( "Mary", "had", "a", "little", "lamb", "its", "feet", "were", "white", "as", "snow" );
			List<String> s2 = ListTool.create( "a", "little", "lamb", "its", "were" );
			List<String> expected = ListTool.create( "Mary", "had", "feet", "white", "as", "snow" );
			List<String> actual = ListTool.minus( s1, s2 );
			Assert.assertTrue("Minus : expected list size does not match actual", expected.size() == actual.size() );
			for ( int i = 0; i < actual.size(); i++ ) {
				Assert.assertTrue( "Item " + i + " " + actual.get( i ) + " does not match expected " + expected.get(i), 
						actual.get( i ).equals( expected.get( i ) ) );
			}
		}
		@Test public void copyOfRange(){
			List<Integer> a = ListTool.createLinkedList(1,2,3,4,5);
			List<Integer> b = ListTool.copyOfRange(a, 1, 3);
			Assert.assertArrayEquals(new Integer[]{2,3}, b.toArray());
			List<Integer> c = ListTool.copyOfRange(a, 4, 27);
			Assert.assertArrayEquals(new Integer[]{5}, c.toArray());

			List<Integer> one = ListTool.createLinkedList(1);
			Assert.assertEquals(0,ListTool.copyOfRange(one,0,0).size());
			Assert.assertEquals(0,ListTool.copyOfRange(one,0,-1).size());
			Assert.assertEquals(1,ListTool.copyOfRange(one,0,1).size());
			Assert.assertEquals(1,ListTool.copyOfRange(one,0,2).size());
			Assert.assertEquals(0,ListTool.copyOfRange(one,-1,2).size());
		}

		@Test
		public void testGetLastNElements() {
			List<Integer> a = ListTool.createArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
			List<Integer> b = ListTool.getLastNElements(a, 10);
			List<Integer> c = ListTool.getLastNElements(b, 5);
			List<Integer> d = ListTool.getLastNElements(c, 200);
			List<Integer> e = ListTool.getLastNElements(d, 0);

			Assert.assertEquals(10, b.size());
			Assert.assertArrayEquals(new Integer[] { 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }, b.toArray());
			Assert.assertEquals(5, c.size());
			Assert.assertArrayEquals(new Integer[] { 11, 12, 13, 14, 15 }, c.toArray());
			Assert.assertEquals(c.size(), d.size());
			Assert.assertArrayEquals(c.toArray(), d.toArray());
			Assert.assertEquals(0, e.size());
		}
		
		
		@Test
		public void testGetFirstNElements() {
			List<Integer> list1To15 = ListTool.createArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
			List<Integer> list1To10 = ListTool.getFirstNElements(list1To15, 10);
			List<Integer> list1To5 = ListTool.getFirstNElements(list1To10, 5);
			List<Integer> list1To5TestLimit200 = ListTool.getFirstNElements(list1To5, 200);
			List<Integer> list1To5TestLimit0 = ListTool.getFirstNElements(list1To5TestLimit200, 0);
			List<Integer> list1To5TestLimitNeg1 = ListTool.getFirstNElements(list1To5TestLimit200, -1);
			
			Assert.assertEquals(10, list1To10.size());
			Assert.assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, list1To10.toArray());
			Assert.assertEquals(5, list1To5.size());
			Assert.assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, list1To5.toArray());
			Assert.assertEquals(list1To5.size(), list1To5TestLimit200.size());
			Assert.assertArrayEquals(list1To5.toArray(), list1To5TestLimit200.toArray());
			Assert.assertEquals(0, list1To5TestLimit0.size());
			Assert.assertEquals(0, list1To5TestLimitNeg1.size());
		}

	}


}
