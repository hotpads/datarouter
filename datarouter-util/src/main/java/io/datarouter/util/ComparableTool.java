/**
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
package io.datarouter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.collection.ListTool;

public class ComparableTool{

	public static <T extends Comparable<? super T>> boolean lt(T object1, T object2){
		int diff = nullFirstCompareTo(object1, object2);
		return diff < 0;
	}

	/**
	 * is a &gt; b
	 */
	public static <T extends Comparable<? super T>> boolean gt(T object1, T object2){
		int diff = nullFirstCompareTo(object1, object2);
		return diff > 0;
	}

	//treat start=null as -Infinity, end=null as Infinity
	public static <T extends Comparable<? super T>> boolean between(T min, boolean minInclusive, T value, T max,
			boolean maxInclusive){
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

	/**
	 * null is greater than non-null
	 */
	@SafeVarargs
	public static <T extends Comparable<T>> T min(T... elements){
		return min(Arrays.asList(elements));
	}

	/**
	 * null is greater than non-null
	 */
	public static <T extends Comparable<T>> T min(Collection<T> elements){
		return Collections.min(elements, Comparator.nullsLast(Comparator.naturalOrder()));
	}

	/**
	 * null is less than non-null
	 */
	@SafeVarargs
	public static <T extends Comparable<T>> T max(T... elements){
		return max(Arrays.asList(elements));
	}

	/**
	 * null is less than non-null
	 */
	public static <T extends Comparable<T>> T max(Collection<T> elements){
		return Collections.max(elements, Comparator.nullsFirst(Comparator.naturalOrder()));
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
		Assert.assertEquals(-backwardDiff, forwardDiff);
		return forwardDiff;
	}

	public static class ComparableToolTests{

		@Test
		public void testLessThan(){
			Assert.assertTrue(lt("a", "b"));
			Assert.assertTrue(lt(null, "b"));
			Assert.assertFalse(lt("a", null));
			Assert.assertFalse(lt(null, null));
			Assert.assertFalse(lt("eq", "eq"));
		}

		@Test
		public void testGreaterThan(){
			Assert.assertFalse(gt("a", "b"));
			Assert.assertFalse(gt(null, "b"));
			Assert.assertTrue(gt("a", null));
			Assert.assertFalse(gt(null, null));
			Assert.assertFalse(gt("eq", "eq"));
		}

		@Test
		public void testBetween(){
			Assert.assertTrue(between(-3f, false, -1f, 7f, false));
			Assert.assertFalse(between(-3f, false, -17.5f, 7f, false));
			Assert.assertTrue(between(-3f, true, -3f, 7f, false));
			Assert.assertTrue(between(0, true, 0, 0, false));
			//treat start=null as -Infinity, end=null as Infinity
			Assert.assertTrue(between(null, true, 12345, null, true));
		}

		@Test
		public void testIsSorted(){
			Assert.assertTrue(isSorted(null));
			Assert.assertTrue(isSorted(new ArrayList<Integer>()));
			List<Integer> listA = ListTool.create(1,2,3,4);
			Assert.assertTrue(isSorted(listA));
			List<Integer> listB = ListTool.create(1,2,55,4);
			Assert.assertFalse(isSorted(listB));
		}

		@Test
		public void testMin(){
			Assert.assertEquals(min((Integer)null), null);
			Assert.assertEquals(min((Integer)null, null), null);
			Assert.assertEquals(min((Integer)null, null, null), null);
			Assert.assertEquals(min((Double)null), null);
			Assert.assertEquals(min((Double)null, null), null);
			Assert.assertEquals(min((Double)null, null, null), null);
			Assert.assertEquals(min(null, null, 3), new Integer(3));
			Assert.assertEquals(min(3, null), new Integer(3));
			Assert.assertEquals(min(3, 1), new Integer(1));
			Assert.assertEquals(min(1, 3), new Integer(1));
			Assert.assertEquals(min(1, 2, 3), new Integer(1));
			Assert.assertEquals(min(1, 3, 2), new Integer(1));
			Assert.assertEquals(min(1, 3, null, 2), new Integer(1));
			Assert.assertEquals(min(3, null, 2), new Integer(2));
			Assert.assertEquals(min(null, null, 3d), new Double(3));
			Assert.assertEquals(min(3d, null), new Double(3));
			Assert.assertEquals(min(3d, 1d), new Double(1));
			Assert.assertEquals(min(1d, 3d), new Double(1));
			Assert.assertEquals(min(1d, 2d, 3d), new Double(1));
			Assert.assertEquals(min(1d, 3d, 2d), new Double(1));
			Assert.assertEquals(min(1d, 3d, null, 2d), new Double(1));
			Assert.assertEquals(min(3d, null, 2d), new Double(2));
		}

		@Test
		public void testMax(){
			Assert.assertNull(max((Integer)null, null));
			Assert.assertEquals(max(null, 0), new Integer(0));
			Assert.assertEquals(max(0, null), new Integer(0));
			Assert.assertEquals(max(0, 0), new Integer(0));
			Assert.assertEquals(max(0, 3), new Integer(3));
			Assert.assertEquals(max(3, 0), new Integer(3));
			Assert.assertEquals(max((Double)null), null);
			Assert.assertEquals(max((Double)null, null), null);
			Assert.assertEquals(max((Double)null, null, null), null);
			Assert.assertEquals(max(null, null, 3d), new Double(3));
			Assert.assertEquals(max(3d, null), new Double(3));
			Assert.assertEquals(max(3d, 1d), new Double(3));
			Assert.assertEquals(max(1d, 3d), new Double(3));
			Assert.assertEquals(max(1d, 2d, 3d), new Double(3));
			Assert.assertEquals(max(1d, 3d, 2d), new Double(3));
			Assert.assertEquals(max(1d, 3d, null, 2d), new Double(3));
			Assert.assertEquals(max(1d, null, 2d), new Double(2));
		}
	}
}
