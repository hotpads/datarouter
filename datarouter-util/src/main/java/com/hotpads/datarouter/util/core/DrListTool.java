package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;

import org.junit.Assert;
import org.junit.Test;


public class DrListTool {

	public static <T> List<T> nullSafeWrap(T item){
		List<T> list = new LinkedList<T>();
		if(item!=null){
			list.add(item);
		}
		return list;
	}

	public static <T> List<T> wrap(T item){
		return nullSafeWrap(item);
	}

	public static <T> ArrayList<T> createArrayListWithSize(Collection<?> collection){
		return new ArrayList<>(DrCollectionTool.sizeNullSafe(collection));
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
		if(DrArrayTool.isEmpty(in)){
			return out;
		}
		for (T element : in){
			out.add(element);
		}
		return out;
	}

	public static <T> ArrayList<T> createArrayList(T... in){
		ArrayList<T> out = new ArrayList<T>(DrArrayTool.nullSafeLength(in));
		if(DrArrayTool.isEmpty(in)){
			return out;
		}
		for (T element : in){
			out.add(element);
		}
		return out;
	}

	public static <T> List<T> createLinkedList(Collection<T> in){
		List<T> out = new LinkedList<T>();
		if(DrCollectionTool.isEmpty(in)){
			return out;
		}
		out.addAll(in);
		return out;
	}

	public static <T> ArrayList<T> createArrayList(Iterator<T> ins){
		ArrayList<T> outs = new ArrayList<>();
		while(ins.hasNext()){
			outs.add(ins.next());
		}
		return outs;
	}

	public static <T> ArrayList<T> createArrayList(Iterable<T> ins){
		return createArrayList(ins, Integer.MAX_VALUE);
	}

	public static <T> ArrayList<T> createArrayList(Iterable<T> ins, int limit){
		ArrayList<T> outs = new ArrayList<T>();//don't pre-size array in case limit is huge
		for(T in : DrIterableTool.nullSafe(ins)) {
			outs.add(in);
			if(outs.size() >= limit){
				break;
			}
		}
		return outs;
	}

	public static <T> ArrayList<T> createArrayList(Collection<T> in){
		ArrayList<T> out = new ArrayList<T>(DrCollectionTool.sizeNullSafe(in));
		if(DrCollectionTool.isEmpty(in)){
			return out;
		}
		out.addAll(in);
		return out;
	}

	public static <T> List<T> nullSafe(List<T> in){
		if(in == null) {
			return new LinkedList<T>();
		}
		return in;
	}

	public static <T> List<T> nullSafeLinked(List<T> in){
		if(in == null) {
			return new LinkedList<T>();
		}
		return in;
	}

	public static <T> List<T> nullSafeArray(List<T> in){
		if(in == null) {
			return new ArrayList<T>();
		}
		return in;
	}

	public static <T>List<T> asList(Collection<T> coll){
		if(coll == null) {
			return new LinkedList<>();
		}
		if(coll instanceof List){
			return (List<T>)coll;
		}
		return new LinkedList<>(coll);
	}


	/*********************** concatenate ********************************/

	public static <T> List<T> concatenate(List<T> listA, List<T> listB){
		int sizeA = DrCollectionTool.size(listA);
		int sizeB = DrCollectionTool.size(listB);
		ArrayList<T> outs = new ArrayList<>(sizeA + sizeB);
		if(sizeA > 0){
			outs.addAll(listA);
		}
		if(sizeB > 0){
			outs.addAll(listB);
		}
		return outs;
	}


	/**************************** compare **************************/

	public static <T extends Comparable<T>> int compare(List<T> as, List<T> bs){
		if(as == null) {
			return bs == null ? 0 : -1;
		}
		if(bs == null) {
			return as == null ? 0 : 1;
		}
		Iterator<T> bi = bs.iterator();
		for(T a : as){
			if(!bi.hasNext()) {
				return 1;
			} // as are longer than bs
			int comp = DrComparableTool.nullFirstCompareTo(a, bi.next());
			if(comp != 0) {
				return comp;
			}
		}
		return bi.hasNext()?-1:0;  //bs are longer than as
	}

	public static <T extends Comparable<? super T>> boolean isSorted(List<T> as){
		if(as == null) {
			return true;
		}
		T last = null;
		for(T a : as){
			if(DrComparableTool.nullFirstCompareTo(last, a) > 0){
				return false;
			}
			last = a;
		}
		return true;
	}


	/************************** modify *******************************/

	public static <T> List<T> nullSafeLinkedAddAll(List<T> list, T... newItems){
		list = nullSafeLinked(list);
		if(DrArrayTool.notEmpty(newItems)){
			for (T newItem : newItems){
				list.add(newItem);
			}
		}
		return list;
	}

	public static <T> List<T> nullSafeArrayAddAll(List<T> list, Collection<? extends T> newItems){
		list = nullSafeArray(list);
		list.addAll(DrCollectionTool.nullSafe(newItems));
		return list;
	}

	public static <T> List<T> copyOfRange(
			List<T> in, int startInclusive, int endExclusive){
		if(DrCollectionTool.isEmpty(in)) {
			return create();
		}
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


	/****************** tests *********************/

	public static class Tests {
		@Test
		public void copyOfRange(){
			List<Integer> resultA = DrListTool.createLinkedList(1,2,3,4,5);
			List<Integer> resultB = DrListTool.copyOfRange(resultA, 1, 3);
			Assert.assertArrayEquals(new Integer[]{2,3}, resultB.toArray());
			List<Integer> resultC = DrListTool.copyOfRange(resultA, 4, 27);
			Assert.assertArrayEquals(new Integer[]{5}, resultC.toArray());

			List<Integer> one = DrListTool.createLinkedList(1);
			Assert.assertEquals(0,DrListTool.copyOfRange(one,0,0).size());
			Assert.assertEquals(0,DrListTool.copyOfRange(one,0,-1).size());
			Assert.assertEquals(1,DrListTool.copyOfRange(one,0,1).size());
			Assert.assertEquals(1,DrListTool.copyOfRange(one,0,2).size());
			Assert.assertEquals(0,DrListTool.copyOfRange(one,-1,2).size());
		}

	}


}
