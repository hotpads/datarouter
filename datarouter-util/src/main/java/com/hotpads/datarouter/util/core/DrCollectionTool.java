package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class DrCollectionTool{

	/************************************ null **************************/

	public static <T> Collection<T> nullSafe(T in){
		if(in == null){
			return new LinkedList<>();
		}
		List<T> out = new LinkedList<>();
		out.add(in);
		return out;
	}

	public static <T> Collection<T> nullSafe(Collection<T> in){
		if(in == null){
			return new LinkedList<>();
		}
		return in;
	}

	/*************************** empty ****************************************/

	public static <T> boolean isEmpty(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return true;
		}
		return false;
	}

	public static <T> boolean notEmpty(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return false;
		}
		return true;
	}

	/****************************** size **************************************/

	public static boolean differentSize(Collection<?> collectionA, Collection<?> collectionB){
		return sizeNullSafe(collectionA) != sizeNullSafe(collectionB);
	}

	public static int size(Collection<?> collection){
		return sizeNullSafe(collection);
	}

	public static int sizeNullSafe(Collection<?> collection){
		if(collection == null){
			return 0;
		}
		return collection.size();
	}

	public static <T,U> int getTotalSizeOfMapOfCollections(Map<T,? extends Collection<U>> map){
		if(map == null){
			return 0;
		}
		int counter = 0;
		for(T t : map.keySet()){
			Collection<U> collection = map.get(t);
			counter += sizeNullSafe(collection);
		}
		return counter;
	}

	public static <T> int getTotalSizeOfCollectionOfCollections(Collection<? extends Collection<T>> outer){
		if(outer == null){
			return 0;
		}
		int counter = 0;
		for(Collection<T> inner : outer){
			counter += sizeNullSafe(inner);
		}
		return counter;
	}

	/********************************* equals *********************************/

	public static <T> boolean equalsAllElementsInIteratorOrder(Collection<T> collectionA, Collection<T> collectionB){
		if(differentSize(collectionA, collectionB)){
			return false;
		}
		if(isEmpty(collectionA)){
			return true;
		}
		List<T> listOfA = DrListTool.createArrayList(collectionA);
		List<T> listOfB = DrListTool.createArrayList(collectionB);
		for(int i = 0; i < listOfA.size(); ++i){
			if(DrObjectTool.nullSafeNotEquals(listOfA.get(i), listOfB.get(i))){
				return false;
			}
		}
		return true;
	}

	/************************ sub-collection **********************************/

	public static <T> T getFirst(Collection<T> collection){
		return getItemAtIndex(collection, 0);
	}

	public static <T> T getItemAtIndex(Collection<T> collection, int index){
		if(DrCollectionTool.isEmpty(collection)){
			return null;
		}
		int currentIndex = 0;
		for(T item : collection){
			if(currentIndex == index){
				return item;
			}
			currentIndex++;
		}
		return null;
	}

	public static <T> T getLast(List<T> list){
		if(isEmpty(list)){
			return null;
		}
		return list.get(list.size() - 1);
	}

	public static <T> List<T> removeNulls(Iterable<T> ts){
		LinkedList<T> lst = new LinkedList<>();
		for(T t : ts){
			if(t != null){
				lst.add(t);
			}
		}
		return lst;
	}

	/****************************** contains **********************************/

	public static <T> boolean doesNotContain(Collection<T> coll, T item){
		if(coll == null){
			return true;
		}
		return !coll.contains(item);
	}

	/************************* set operations *********************************/

	public static <T> Set<T> minus(final Collection<T> first, final Collection<T> second){
		Set<T> result = new HashSet<>(DrCollectionTool.removeNulls(first));
		result.removeAll(second);
		return result;
	}

	public static <T> TreeSet<T> minus(final Collection<T> first, final Collection<T> second, Comparator<T> comparator){
		TreeSet<T> result = new TreeSet<>(comparator);
		result.addAll(DrCollectionTool.removeNulls(first));
		// removeAll() may not use the Comparator to find object to remove and will use the .equals().
		// And some Comparator s are not consistent with .equals(), so use remove() instead
		second.forEach(result::remove);
		return result;
	}

	/******************************** tests ***********************************/

	public static class CollectionToolTests{

		@Test
		public void testNotEmpty(){
			ArrayList<String> list = new ArrayList<>();
			Assert.assertFalse(notEmpty(list));
			Assert.assertFalse(notEmpty(null));
		}

	}

}
