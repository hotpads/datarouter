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
package io.datarouter.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.ComparableTool;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.iterable.IterableTool;

public class ListTool{

	public static <T> List<T> wrap(T item){
		List<T> list = new LinkedList<>();
		if(item != null){
			list.add(item);
		}
		return list;
	}

	public static <T> ArrayList<T> createArrayListWithSize(Collection<?> collection){
		return new ArrayList<>(CollectionTool.sizeNullSafe(collection));
	}

	public static <T> ArrayList<T> createArrayListAndInitialize(int size){
		ArrayList<T> out = new ArrayList<>(size);
		for(int i = 0; i < size; ++i){
			out.add(null);
		}
		return out;
	}

	public static <T> void replaceLast(List<T> list, T replacement){
		int lastIndex = list.size() - 1;
		list.set(lastIndex, replacement);
	}

	@SafeVarargs
	public static <T> ArrayList<T> create(T... in){
		return createArrayList(in);
	}

	@SafeVarargs
	public static <T> LinkedList<T> createLinkedList(T... in){
		LinkedList<T> out = new LinkedList<>();
		if(ArrayTool.isEmpty(in)){
			return out;
		}
		for(T element : in){
			out.add(element);
		}
		return out;
	}

	public static <T> List<T> createLinkedList(Collection<T> in){
		return new LinkedList<>(in);
	}

	@SafeVarargs
	public static <T> ArrayList<T> createArrayList(T... in){
		ArrayList<T> out = new ArrayList<>(ArrayTool.length(in));
		if(ArrayTool.isEmpty(in)){
			return out;
		}
		for(T element : in){
			out.add(element);
		}
		return out;
	}

	public static <T> ArrayList<T> createArrayList(Iterable<T> ins){
		return createArrayList(ins, Integer.MAX_VALUE);
	}

	public static <T> ArrayList<T> createArrayList(Iterable<T> ins, int limit){
		ArrayList<T> outs = new ArrayList<>();// don't pre-size array in case limit is huge
		for(T in : IterableTool.nullSafe(ins)){
			outs.add(in);
			if(outs.size() >= limit){
				break;
			}
		}
		return outs;
	}

	public static <T> List<T> nullSafe(List<T> in){
		if(in == null){
			return new LinkedList<>();
		}
		return in;
	}

	private static <T> List<T> nullSafeLinked(List<T> in){
		if(in == null){
			return new LinkedList<>();
		}
		return in;
	}

	private static <T> List<T> nullSafeArray(List<T> in){
		if(in == null){
			return new ArrayList<>();
		}
		return in;
	}

	public static <T> List<T> asList(Collection<T> coll){
		if(coll == null){
			return new ArrayList<>();
		}
		if(coll instanceof List){
			return (List<T>)coll;
		}
		return new ArrayList<>(coll);
	}

	/*---------------------------- concatenate ------------------------------*/

	public static <T> List<T> concatenate(Collection<T> collectionA, Collection<T> collectionB){
		int sizeA = CollectionTool.size(collectionA);
		int sizeB = CollectionTool.size(collectionB);
		ArrayList<T> outs = new ArrayList<>(sizeA + sizeB);
		if(sizeA > 0){
			outs.addAll(collectionA);
		}
		if(sizeB > 0){
			outs.addAll(collectionB);
		}
		return outs;
	}

	/*------------------------------ compare --------------------------------*/

	public static <T extends Comparable<T>> int compare(List<T> as, List<T> bs){
		if(as == null){
			return bs == null ? 0 : -1;
		}
		if(bs == null){
			return 1;
		}
		Iterator<T> bi = bs.iterator();
		for(T a : as){
			if(!bi.hasNext()){
				return 1;
			} // as are longer than bs
			int comp = ComparableTool.nullFirstCompareTo(a, bi.next());
			if(comp != 0){
				return comp;
			}
		}
		return bi.hasNext() ? -1 : 0; // bs are longer than as
	}

	public static <T extends Comparable<? super T>> boolean isSorted(List<T> as){
		if(as == null){
			return true;
		}
		T last = null;
		for(T a : as){
			if(ComparableTool.nullFirstCompareTo(last, a) > 0){
				return false;
			}
			last = a;
		}
		return true;
	}

	/*------------------------------ modify ---------------------------------*/

	public static <T> List<T> nullSafeLinkedAddAll(List<T> list, T[] newItems){
		list = nullSafeLinked(list);
		if(ArrayTool.notEmpty(newItems)){
			for(T newItem : newItems){
				list.add(newItem);
			}
		}
		return list;
	}

	public static <T> List<T> nullSafeArrayAddAll(List<T> list, Collection<? extends T> newItems){
		list = nullSafeArray(list);
		list.addAll(CollectionTool.nullSafe(newItems));
		return list;
	}

	public static <T> List<T> copyOfRange(List<T> in, int startInclusive, int endExclusive){
		if(CollectionTool.isEmpty(in)){
			return new ArrayList<>();
		}
		if(startInclusive >= in.size() || endExclusive <= startInclusive || startInclusive < 0){
			return new ArrayList<>();
		}
		if(endExclusive > in.size()){
			endExclusive = in.size();
		}
		int rangeSize = endExclusive - startInclusive;

		List<T> copy = new ArrayList<>(rangeSize);
		if(in instanceof RandomAccess){
			for(int i = startInclusive; i < endExclusive; ++i){
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

	public static <T> List<T> getFirstNElements(List<T> in, int indexN){
		if(CollectionTool.isEmpty(in) || indexN <= 0){
			return new ArrayList<>();
		}
		return copyOfRange(in, 0, Math.max(0, indexN));
	}

	/*-------------------------------- test ---------------------------------*/

	public static class Tests{
		@Test
		public void copyOfRange(){
			List<Integer> resultA = ListTool.createLinkedList(1, 2, 3, 4, 5);
			List<Integer> resultB = ListTool.copyOfRange(resultA, 1, 3);
			Assert.assertEquals(resultB.toArray(), new Integer[]{2, 3});
			List<Integer> resultC = ListTool.copyOfRange(resultA, 4, 27);
			Assert.assertEquals(resultC.toArray(), new Integer[]{5});

			List<Integer> one = ListTool.createLinkedList(1);
			Assert.assertEquals(ListTool.copyOfRange(one, 0, 0).size(), 0);
			Assert.assertEquals(ListTool.copyOfRange(one, 0, -1).size(), 0);
			Assert.assertEquals(ListTool.copyOfRange(one, 0, 1).size(), 1);
			Assert.assertEquals(ListTool.copyOfRange(one, 0, 2).size(), 1);
			Assert.assertEquals(ListTool.copyOfRange(one, -1, 2).size(), 0);
		}

		@Test
		public void testGetFirstNElements(){
			List<Integer> list1To15 = ListTool.createArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
			List<Integer> list1To10 = ListTool.getFirstNElements(list1To15, 10);
			List<Integer> list1To5 = ListTool.getFirstNElements(list1To10, 5);
			List<Integer> list1To5TestLimit200 = ListTool.getFirstNElements(list1To5, 200);
			List<Integer> list1To5TestLimit0 = ListTool.getFirstNElements(list1To5TestLimit200, 0);
			List<Integer> list1To5TestLimitNeg1 = ListTool.getFirstNElements(list1To5TestLimit200, -1);

			Assert.assertEquals(list1To10.size(), 10);
			Assert.assertEquals(list1To10.toArray(), new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
			Assert.assertEquals(list1To5.size(), 5);
			Assert.assertEquals(list1To5.toArray(), new Integer[]{1, 2, 3, 4, 5});
			Assert.assertEquals(list1To5.size(), list1To5TestLimit200.size());
			Assert.assertEquals(list1To5.toArray(), list1To5TestLimit200.toArray());
			Assert.assertEquals(list1To5TestLimit0.size(), 0);
			Assert.assertEquals(list1To5TestLimitNeg1.size(), 0);
		}
	}
}
