package com.hotpads.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;


public class ComparableTool {

	public static boolean lt(Comparable a, Comparable b){
		int diff = nullFirstCompareTo(a, b);
		return diff < 0;
	}
	

	/**
	 * is a > b
	 */
	public static boolean gt(Comparable a, Comparable b){
		int diff = nullFirstCompareTo(a, b);
		return diff > 0;
	}
	
	//treat start=null as -Infinity, end=null as Infinity
	public static <T extends Comparable<? super T>> boolean between(
			T min, boolean minInclusive, T value, T max, boolean maxInclusive){
		int minDiff = nullFirstCompareTo(value, min);
		int maxDiff = nullLastCompareTo(value, max);
		if(minInclusive && minDiff==0){ return true; }
		if(maxInclusive && maxDiff==0){ return true; }
		return minDiff > 0 && maxDiff < 0;
	}
	
	public static <T extends Comparable<? super T>> int nullFirstCompareTo(T a, T b){
		if(a==null && b==null){
			return 0;
		}else if(a==null){
			return -1;
		}else if(b==null){
			return 1;
		}else{
			return a.compareTo(b);
		}
	}
	
	public static <T extends Comparable<? super T>> int nullLastCompareTo(T a, T b){
		if(a==null && b==null){
			return 0;
		}else if(a==null){
			return 1;
		}else if(b==null){
			return -1;
		}else{
			return a.compareTo(b);
		}
	}
	
	//smallest
	public static <T extends Comparable<T>> T getFirst(Collection<T> c){
		if(CollectionTool.isEmpty(c)){ return null; }
		T first = CollectionTool.getFirst(c);
		for(T i : c){
			if(i.compareTo(first) < 0){
				first = i;
			}
		}
		return first;
	}
	
	//biggest
	public static  <T extends Comparable<T>> T getLast(Collection<T> c){
		if(CollectionTool.isEmpty(c)){ return null; }
		T last = CollectionTool.getFirst(c);
		for(T i : c){
			if(i.compareTo(last) > 0){
				last = i;
			}
		}
		return last;
	}
	
	public static <T extends Comparable<? super T>> boolean isSorted(Iterable<? extends T> ins){
		if(ins==null){ return true; }//null is considered sorted
		Iterator<? extends T> iter = ins.iterator();
		if(!iter.hasNext()){ return true; }//0 elements is sorted
		T previous = iter.next();
		if(!iter.hasNext()){ return true; }//1 element is sorted
		T current = null;
		while(iter.hasNext()){
			current = iter.next();
			if(previous.compareTo(current) > 0){ return false; }
			previous = current;
		}
		return true;
	}
	
	public static <T extends Comparable<? super T>> int compareAndAssertReflexive(T a, T b){
		int forwardDiff = nullFirstCompareTo(a, b);
		int backwardDiff = nullFirstCompareTo(b, a);
		Assert.assertEquals(forwardDiff, -backwardDiff);
		return forwardDiff;
	}
	
	public static class ComparableToolTests{
		@Test public void testLessThan(){
			Assert.assertTrue(lt("a", "b"));
			Assert.assertTrue(lt(null, "b"));
			Assert.assertFalse(lt("a", null));
			Assert.assertFalse(lt(null, null));
			Assert.assertFalse(lt("eq", "eq"));
		}
		@Test public void testGreaterThan(){
			Assert.assertFalse(gt("a", "b"));
			Assert.assertFalse(gt(null, "b"));
			Assert.assertTrue(gt("a", null));
			Assert.assertFalse(gt(null, null));
			Assert.assertFalse(gt("eq", "eq"));
		}
		@Test public void testBetween(){
			Assert.assertTrue(between(-3f, false, -1f, 7f, false));
			Assert.assertFalse(between(-3f, false, -17.5f, 7f, false));
			Assert.assertTrue(between(-3f, true, -3f, 7f, false));
			Assert.assertTrue(between(0, true, 0, 0, false));
			Assert.assertTrue(between(null, true, 12345, null, true));//treat start=null as -Infinity, end=null as Infinity
		}
		@Test public void testIsSorted(){
			Assert.assertTrue(isSorted(null));
			Assert.assertTrue(isSorted(new ArrayList<Integer>()));
			List<Integer> a = ListTool.create(1,2,3,4);
			Assert.assertTrue(isSorted(a));
			List<Integer> b = ListTool.create(1,2,55,4);
			Assert.assertFalse(isSorted(b));
			
		}
	}
}
