package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.testng.AssertJUnit;

public class DrComparableTool{

	public static <T extends Comparable<? super T>> boolean lt(T object1, T object2){
		int diff = nullFirstCompareTo(object1, object2);
		return diff < 0;
	}

	/**
	 * is a > b
	 */
	public static <T extends Comparable<? super T>> boolean gt(T object1, T object2){
		int diff = nullFirstCompareTo(object1, object2);
		return diff > 0;
	}

	//treat start=null as -Infinity, end=null as Infinity
	public static <T extends Comparable<? super T>> boolean between(
			T min, boolean minInclusive, T value, T max, boolean maxInclusive){
		int minDiff = nullFirstCompareTo(value, min);
		int maxDiff = nullLastCompareTo(value, max);
		if(minInclusive && minDiff == 0){
			return true;
		}
		if(maxInclusive && maxDiff == 0){
			return true;
		}
		return minDiff > 0 && maxDiff < 0;
	}

	public static <T extends Comparable<? super T>> int nullFirstCompareTo(T object1, T object2){
		if(object1 == null && object2 == null){
			return 0;
		}else if(object1 == null){
			return -1;
		}else if(object2 == null){
			return 1;
		}else{
			return object1.compareTo(object2);
		}
	}

	public static <T extends Comparable<? super T>> int nullLastCompareTo(T object1, T object2){
		if(object1 == null && object2 == null){
			return 0;
		}else if(object1 == null){
			return 1;
		}else if(object2 == null){
			return -1;
		}else{
			return object1.compareTo(object2);
		}
	}

	public static <T extends Comparable<T>> T min(T... elements){
		// null is greater than non-null
		return Collections.min(Arrays.asList(elements), Comparator.nullsLast(Comparator.naturalOrder()));
	}

	public static <T extends Comparable<T>> T max(T... elements){
		// null is less than non-null
		return Collections.max(Arrays.asList(elements), Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	//smallest
	public static <T extends Comparable<T>> T getFirst(Collection<T> collection){
		if(DrCollectionTool.isEmpty(collection)){
			return null;
		}
		T first = DrCollectionTool.getFirst(collection);
		for(T i : collection){
			if(i.compareTo(first) < 0){
				first = i;
			}
		}
		return first;
	}

	//biggest
	public static <T extends Comparable<T>> T getLast(Collection<T> collection){
		if(DrCollectionTool.isEmpty(collection)){
			return null;
		}
		T last = DrCollectionTool.getFirst(collection);
		for(T i : collection){
			if(i.compareTo(last) > 0){
				last = i;
			}
		}
		return last;
	}

	public static <T extends Comparable<? super T>> boolean isSorted(Iterable<? extends T> ins){
		if(ins == null){
			return true;
		}//null is considered sorted
		Iterator<? extends T> iter = ins.iterator();
		if(!iter.hasNext()){
			return true;
		}//0 elements is sorted
		T previous = iter.next();
		if(!iter.hasNext()){
			return true;
		}//1 element is sorted
		T current = null;
		while(iter.hasNext()){
			current = iter.next();
			if(previous.compareTo(current) > 0){
				return false;
			}
			previous = current;
		}
		return true;
	}

	public static <T extends Comparable<? super T>> int compareAndAssertReflexive(T object1, T object2){
		int forwardDiff = nullFirstCompareTo(object1, object2);
		int backwardDiff = nullFirstCompareTo(object2, object1);
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
			Assert.assertTrue(between(null, true, 12345,
					null, true));//treat start=null as -Infinity, end=null as Infinity
		}
		@Test public void testIsSorted(){
			Assert.assertTrue(isSorted(null));
			Assert.assertTrue(isSorted(new ArrayList<Integer>()));
			List<Integer> listA = DrListTool.create(1,2,3,4);
			Assert.assertTrue(isSorted(listA));
			List<Integer> listB = DrListTool.create(1,2,55,4);
			Assert.assertFalse(isSorted(listB));

		}
		@Test
		public void testMin(){
			AssertJUnit.assertEquals(null, min((Integer)null));
			AssertJUnit.assertEquals(null, min((Integer)null, null));
			AssertJUnit.assertEquals(null, min((Integer)null, null, null));
			AssertJUnit.assertEquals(null, min((Double)null));
			AssertJUnit.assertEquals(null, min((Double)null, null));
			AssertJUnit.assertEquals(null, min((Double)null, null, null));
			AssertJUnit.assertEquals(new Integer(3), min(null, null, 3));
			AssertJUnit.assertEquals(new Integer(3), min(3, null));
			AssertJUnit.assertEquals(new Integer(1), min(3, 1));
			AssertJUnit.assertEquals(new Integer(1), min(1, 3));
			AssertJUnit.assertEquals(new Integer(1), min(1, 2, 3));
			AssertJUnit.assertEquals(new Integer(1), min(1, 3, 2));
			AssertJUnit.assertEquals(new Integer(1), min(1, 3, null, 2));
			AssertJUnit.assertEquals(new Integer(2), min(3, null, 2));
			AssertJUnit.assertEquals(new Double(3), min(null, null, 3d));
			AssertJUnit.assertEquals(new Double(3), min(3d, null));
			AssertJUnit.assertEquals(new Double(1), min(3d, 1d));
			AssertJUnit.assertEquals(new Double(1), min(1d, 3d));
			AssertJUnit.assertEquals(new Double(1), min(1d, 2d, 3d));
			AssertJUnit.assertEquals(new Double(1), min(1d, 3d, 2d));
			AssertJUnit.assertEquals(new Double(1), min(1d, 3d, null, 2d));
			AssertJUnit.assertEquals(new Double(2), min(3d, null, 2d));
		}

		@Test
		public void testMax(){
			AssertJUnit.assertNull(max((Integer)null, null));
			AssertJUnit.assertEquals(new Integer(0), max(null, 0));
			AssertJUnit.assertEquals(new Integer(0), max(0, null));
			AssertJUnit.assertEquals(new Integer(0), max(0, 0));
			AssertJUnit.assertEquals(new Integer(3), max(0, 3));
			AssertJUnit.assertEquals(new Integer(3), max(3, 0));
			AssertJUnit.assertEquals(null, max((Double)null));
			AssertJUnit.assertEquals(null, max((Double)null, null));
			AssertJUnit.assertEquals(null, max((Double)null, null, null));
			AssertJUnit.assertEquals(new Double(3), max(null, null, 3d));
			AssertJUnit.assertEquals(new Double(3), max(3d, null));
			AssertJUnit.assertEquals(new Double(3), max(3d, 1d));
			AssertJUnit.assertEquals(new Double(3), max(1d, 3d));
			AssertJUnit.assertEquals(new Double(3), max(1d, 2d, 3d));
			AssertJUnit.assertEquals(new Double(3), max(1d, 3d, 2d));
			AssertJUnit.assertEquals(new Double(3), max(1d, 3d, null, 2d));
			AssertJUnit.assertEquals(new Double(2), max(1d, null, 2d));
		}
	}
}
