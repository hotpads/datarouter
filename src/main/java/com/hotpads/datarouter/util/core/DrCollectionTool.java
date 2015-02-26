package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hotpads.util.core.Predicate;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.datastructs.MutableBoolean;


public class DrCollectionTool {
	
	/************************************ null **************************/

	public static <T> Collection<T> nullSafe(T in){
		if(in==null){ return new LinkedList<T>(); }
		List<T> out = new LinkedList<T>();
		out.add(in);
		return out;
	}

	public static <T> Collection<T> nullSafe(Collection<T> in){
		if(in==null){ return new LinkedList<T>(); }
		return in;
	}
	
	/*************************** empty ****************************************/
		
	public static <T> boolean isEmpty(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return true;
		}else{
			return false;
		}
	}

	public static <T> boolean notEmpty(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return false;
		}else{
			return true;
		}
	}
	
	/****************************** size **************************************/
	
	public static <A,B> boolean differentSize(Collection<A> a, Collection<B> b){
		return sizeNullSafe(a) != sizeNullSafe(b);
	}
			
	public static int size(Collection<?> collection){
		return sizeNullSafe(collection);
	}
	
	public static <T> int sizeNullSafe(Collection<T> collection){
		if(collection == null){ return 0; }
		return collection.size();
	}
	
	public static <T,U> int getTotalSizeOfMapOfCollections(Map<T, ? extends Collection<U>> map){
		if(map==null){ return 0; }
		int counter = 0;
		for(T t : map.keySet()){
			Collection<U> c = map.get(t);
			counter += sizeNullSafe(c);
		}
		return counter;
	}
	
	public static <T> int getTotalSizeOfCollectionOfCollections(Collection<? extends Collection<T>> outer){
		if(outer==null){ return 0; }
		int counter = 0;
		for(Collection<T> inner : outer){
			counter += sizeNullSafe(inner);
		}
		return counter;
	}
	
	public static <T> int getTotalSizeOfArrayOfCollections(Collection<T>... collections){
		if(collections==null){ return 0; }
		int totalSize = 0;
		for(Collection<T> inner : collections){
			totalSize += sizeNullSafe(inner);
		}
		return totalSize;
	}
	
	/******************************** add *************************************/
	
	/********************************* equals *********************************/
	
	public static <T> boolean equalsAllElementsInIteratorOrder(Collection<T> as, Collection<T> bs){
		if(differentSize(as,bs)){ return false; }
		if(isEmpty(as)){ return true; }
		List<T> aList = DrListTool.createArrayList(as);
		List<T> bList = DrListTool.createArrayList(bs);
		for(int i=0; i < aList.size(); ++i){
			if(DrObjectTool.nullSafeNotEquals(aList.get(i), bList.get(i))){
				return false; 
			}
		}
		return true;
	}
	
	public static <T> int firstEqualElementInSortedList(List<? extends Comparable<? super T>> sortedList, int index){
		while(true){
			if(index-1 < 0 || ! sortedList.get(index).equals(sortedList.get(index-1))){
				return index;
			}
			index--;
		}
	}
	
	public static <T> int lastEqualElementInSortedList(List<? extends Comparable<? super T>> sortedList, int index){
		while(true){
			if(index+1 >= sortedList.size() || ! sortedList.get(index).equals(sortedList.get(index+1))){
				return index;
			}
			index++;
		}
	}
	
	/************************ sub-collection **********************************/
	
	public static <T> T getFirst(Collection<T> collection){
		return getItemAtIndex(collection,0);
	}
	
	public static <T> T getItemAtIndex(Collection<T> collection, int index){
		if(DrCollectionTool.isEmpty(collection)){
			return null;
		}
		int i = 0;
		for(T item : collection){
			if(i==index){
				return item;
			}
			i++;
		}
		return null;
	}
	
	public static <T> T getLast(List<T> list){
		if(isEmpty(list)){ return null; }
		return list.get(list.size()-1);
	}
	
	public static <T> List<T> removeNulls(Iterable<T> ts) {
		LinkedList<T> lst = DrGenericsFactory.makeLinkedList();
		for (T t : ts)
			if (t != null)
				lst.add(t);
		return lst;
	}
	
	
	/****************************** contains **********************************/
	
	public static <T> boolean doesNotContain(Collection<T> coll, T item){
		if(coll==null){ return true; }
		return ! coll.contains(item);
	}
	
	
	/************************* set operations *********************************/

	public static <T> Set<T> minus(final Collection<T> first, final Collection<T> second){
		Set<T> result = new HashSet<T>(DrCollectionTool.removeNulls(first));
		result.removeAll(second);
		return result;
	}
	
	public static <T> TreeSet<T> minus(final Collection<T> first, final Collection<T> second, Comparator<T> c){
		TreeSet<T> result = new TreeSet<T>(c);
		result.addAll(DrCollectionTool.removeNulls(first));
		result.removeAll(second);
		return result;
	}
	
	
	/*********************** sum of elements **********************************/
	
	public static Long getSumOfLongs(Collection<? extends Long> items){
		Long sum = 0l;
		for(Long item : items){
			sum+=item;
		}
		return sum;
	}
	
	
	/******************************** functor *******************************/

	public static <T> LinkedList<T> filter(Predicate<T> predicate, Iterable<T> iter) {
		LinkedList<T> lst = new LinkedList<T>();
		for (T t : iter)
			if (predicate.check(t))
				lst.add(t);
		return lst;
	}


	/******************************** tests ***********************************/
	
	public static class CollectionToolTests {
		
		@Test public void testNotEmpty(){
			ArrayList<String> l = new ArrayList<String>();
			Assert.assertFalse(notEmpty(l));
			Assert.assertFalse(notEmpty(null));
//			l.add(null);
//			Assert.assertTrue(notEmpty(l));
			Joiner.on(",").join(l);
		}
	}
		
}
