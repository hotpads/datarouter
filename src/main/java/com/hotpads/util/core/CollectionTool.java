package com.hotpads.util.core;

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
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.datastructs.MutableBoolean;


public class CollectionTool {
	
	private CollectionTool(){}
	
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
	
	public static <T> int countNulls(Collection<T> c){
		int count = 0;
		for(T e : c){
			if(e==null){ ++count; }
		}
		return count;
	}
	
	public static <T> int countNonNulls(Collection<T> c){
		if(isEmpty(c)){
			return 0;
		}
		return c.size() - countNulls(c);
	}
	
	public static <T> boolean hasNonNulls(Collection<T> c){
		if(countNonNulls(c)==0){
			return false;
		}
		return true;
	}
	
	public static <T> Collection<T> emptyForNull(Collection<T> in){
		if(in==null){ 
			return Collections.emptyList();
		}
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
	
	public static <A,B> boolean sameSize(Collection<A> a, Collection<B> b){
		return sizeNullSafe(a) == sizeNullSafe(b);
	}
	
	public static <A,B> boolean notSameSize(Collection<A> a, Collection<B> b){
		return sizeNullSafe(a) != sizeNullSafe(b);
	}
	
	public static <A,B> boolean differentSize(Collection<A> a, Collection<B> b){
		return sizeNullSafe(a) != sizeNullSafe(b);
	}
			
	public static int size(Collection<?> collection){
		return sizeNullSafe(collection);
	}
	
	public static boolean hasMultiple(Collection<?> collection) {
		return sizeNullSafe(collection) > 1;
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
	
	public static <T,U> int getTotalSizeOfMapOfArrays(Map<T, short[]> map){
		if(map==null){ return 0; }
		int counter = 0;
		for(T t : map.keySet()){
			short[] array = map.get(t);
			counter += ArrayTool.nullSafeLength(array);
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

	public static <T> boolean addAll(Collection<T> to, Collection<T> from){
		if(from==null) return false;
		return to.addAll(from);
	}
	
	public static <T> int addIteratorElementsToCollection(Collection<T> to, Iterator<T> from){
		int counter =0;
		if(from==null){ return 0; }
		while(from.hasNext()){
			++counter;
			to.add(from.next());
		}
		return counter;
	}
	
	/********************************* equals *********************************/
	
	public static <T> boolean equalsAllElementsInIteratorOrder(Collection<T> as, Collection<T> bs){
		if(differentSize(as,bs)){ return false; }
		if(isEmpty(as)){ return true; }
		List<T> aList = ListTool.createArrayList(as);
		List<T> bList = ListTool.createArrayList(bs);
		for(int i=0; i < aList.size(); ++i){
			if(ObjectTool.nullSafeNotEquals(aList.get(i), bList.get(i))){
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

	public static <T> int lastEqualElementInSortedList(List<? extends Comparable<? super T>> sortedList, int index, Comparator<T> comparator){
		while(true){
			if(index+1 >= sortedList.size() || ! (comparator.compare((T)sortedList.get(index), (T)sortedList.get(index+1))==0)){
				return index;
			}
			index++;
		}
	}
	
	/************************ sub-collection **********************************/
	
	public static <T> ArrayList<T> getTruncatedCopy(Collection<T> list, int newSize){
		ArrayList<T> newList = new ArrayList<T>(newSize);
		int n=0;
		for(T t : list){
			if(++n>newSize) return newList;
			newList.add(t);
		}
		return newList;
	}
	
	public static <T> ArrayList<T> getSublistCopy(
			List<T> list, int page, int perPage){
		ArrayList<T> newList = new ArrayList<T>(perPage);
		if(list.size()<((page-1)*perPage)) return newList;
		for(T t : list.subList((page-1)*perPage,list.size())){
			if(newList.size()==perPage) return newList;
			newList.add(t);
		}
		return newList;
	}
	
	public static <T> List<T> getSublistCopyKeepingOrder(PriorityQueue<T> pq, int fromIndex, int toIndex){
		List<T> toReturn = ListTool.create();
		int size = pq.size();
		toIndex = toIndex > size ? size : toIndex;
		int index = 0;
		while(index < fromIndex){
			pq.poll();
			index++;
		}
		while(index >= fromIndex && index < toIndex){
			toReturn.add(pq.poll());
			index++;
		}
		return toReturn;
	}
	
	
	public static <T> List<T> fromIndex(Collection<T> collection, int fromIndex){
		int size = sizeNullSafe(collection);
		int numElementsIncludingAndAfterFromIndex = size - fromIndex;
		int index = -1;
		List<T> tail = new ArrayList<T>(numElementsIncludingAndAfterFromIndex);
		for(T t : collection){
			++index;
			if(index >= fromIndex){
				tail.add(t);
			}
		}
		return tail;
	}
	
	
	public static <T> T getFirst(Collection<T> collection){
		return getItemAtIndex(collection,0);
	}
	
	public static <T> T getItemAtIndex(Collection<T> collection, int index){
		if(CollectionTool.isEmpty(collection)){
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
	
	private static Random RANDOM_getRandomElement = new Random();
	
	public static <T> T getRandomElement(List<T> list){
		if(isEmpty(list)){ return null; }
		int index = RANDOM_getRandomElement.nextInt(list.size());
		return list.get(index);
	}
	
	public static <T> Set<T> getRandomSubset(Set<T> set, int maxResults){
		return new HashSet<T>(getRandomSublist(new ArrayList<T>(set), maxResults));
	}
	
	public static <T> List<T> getRandomSublist(List<T> list, int maxResults){
		
		List<T> superList = new ArrayList<T>(list);
		List<T> result = new ArrayList<T>();
		
		if(list.size() <= maxResults){
			result.addAll(list);
			Collections.shuffle(result);
		} else {
			Random rand = new Random();
			while(result.size() < maxResults){;
				int index = rand.nextInt(superList.size());
				result.add(superList.remove(index));
			}
		}
		
		return result;
	}
	
	public static <T> List<T> removeNulls(Iterable<T> ts) {
		LinkedList<T> lst = GenericsFactory.makeLinkedList();
		for (T t : ts)
			if (t != null)
				lst.add(t);
		return lst;
	}

	/**
	 * get a number of random items from a list, without replacement.
	 * similar to getRandomSublist, but optimized for very large lists that have linear lookup complexity, and you're
	 * looking for a small subset.
	 * 
	 * assumes there is a large difference between list.size() and numItems - it's possible to get around that, but 
	 * you'll need to improve how this method selects random indices.
	 * 
	 * @param list to search (does not need to implement RandomAccess)
	 * @param numItems number of items to select at random
	 * @return list of randomly selected items with size = min(numItems, list.size()). although the items were randomly
	 * selected, there is no guarantee that the order of the returned items will be random.
	 */
	public static <K> List<K> getRandomSublistFromLargeList(List<K> list, int numItems){
		ArrayList<K> result = ListTool.createArrayList(numItems);
		if(list.size() <= numItems){
			result.addAll(list);
			return result;
		}
		
		//we keep a sorted list to efficiently retrieve all selected values in one list scan
		SortedSet<Integer> indicesToReturn = Sets.newTreeSet();
		//find numIntems random indices
		
		/*
		 * this method of choosing random indices could be a problem if list.size() and numItems are close!
		 */
		while(indicesToReturn.size() < numItems){
			indicesToReturn.add(RandomTool.getRandomIntBetweenTwoNumbers(0, list.size()-1));
		}
		
		
		//pull those indices from the list and accumulate into result
		Iterator<K> itr = list.iterator();
		int i = 0;
		//itr.hasNext() should never return false - bounds checked above
		while(/*itr.hasNext() && */ !indicesToReturn.isEmpty()){
			if(indicesToReturn.first().equals(i)){
				result.add(itr.next());
				indicesToReturn.remove(indicesToReturn.first());
			}
			else{
				itr.next();
			}
			i++;
		}
		
		
		return result;
	}
	
	
	/*************************** re-order *************************************/
	
	public static <T> ArrayList<T> getReversedList(SortedSet<T> input){
		if(CollectionTool.isEmpty(input)){ return new ArrayList<T>(0); }
		ArrayList<T> reversedList = new ArrayList<T>(input.size());
		for(int i=0;i<input.size();i++){ reversedList.add(null); }
		int numCopied = 0;
		for(T item : input){
			reversedList.set(reversedList.size() - numCopied - 1, item);
			++numCopied;
		}
		return reversedList;
	}
	
	public static <T extends Comparable<T>> List<T> makeSortedList(Collection<T> c){
		List<T> list = new LinkedList<T>(c);
		Collections.sort(list);
		return list;
	}
	
	
	/************************ re-wrap *****************************************/
	
	public static <T> void padToSizeWith(List<T> list, int newSize, T filler){
		int numToAdd = newSize - list.size();
		for(int i=0; i < numToAdd; ++i){
			list.add(filler);
		}
	}
	
	
//	public static <T> T[] getArray(Collection<T> ins){
//		@SuppressWarnings("unchecked")
//		T[] outs = (T[])new Object[size(ins)];
//		int i = 0;
//		for(T t : nullSafe(ins)){
//			outs[i++] = t;
//		}
//		return outs;
//	}
	
	
	public static <T> String getCsvList(Collection<T> collection){
		StringBuilder sb = new StringBuilder();
		if(isEmpty(collection)){
			return "";
		}else{
			int remainingToAppend = collection.size();
			for(Object o : collection){
				if(o==null) continue;
				sb.append(o.toString());
				--remainingToAppend;
				if(remainingToAppend > 0){
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}
	
	
	/****************************** contains **********************************/
	
	public static <T> boolean containsAny(final Collection<T> collectionToSearchThrough, final Collection<T> elementsToFind){
		for(T t : elementsToFind){
			if(collectionToSearchThrough.contains(t)){
				return true;
			}
		}
		return false;
	}
	
	public static <T> boolean hasSameElements(final Collection<T> first, final Collection<T> second){
		if(isEmpty(first) && isEmpty(second)){ return true; }
		if(isEmpty(first) && ! isEmpty(second)){ return false; }
		if(isEmpty(second) && ! isEmpty(first)){ return false; }
		Set<T> union = new HashSet<T>();
		union.addAll(first);
		union.addAll(second);
		if(union.size()==first.size() && union.size()==second.size()){ return true; }
		return false;
	}
	
	
	/**
	 * <li/>For collections taglib, so be careful modifying
	 * <li/>More importantly enables type-safe usage of the contains method which is not supported in the native method.
	 * @param coll
	 * @param item
	 * @return
	 */
	public static <T> boolean contains(Collection<T> coll, T item){
		if(coll==null){ return false; }
		return coll.contains(item);
	}
	
	public static <T> boolean doesNotContain(Collection<T> coll, T item){
		if(coll==null){ return true; }
		return ! coll.contains(item);
	}
	
	public static <T,K> boolean containsKey(Map<T,K> coll, T key){
		if(coll==null){ return false; }
		return coll.containsKey(key);
	}
	
	
	/************************* set operations *********************************/
	
 	public static <T> List<T> getUnion(List<T> first, List<T> second){
		List<T> union = new ArrayList<T>(first);
		List<T> secondClone = new ArrayList<T>(second);
		
		secondClone.removeAll(union);
		union.addAll(secondClone);
		return union;
	}

	public static <T> Set<T> minus(final Collection<T> first, final Collection<T> second){
		Set<T> result = new HashSet<T>(CollectionTool.removeNulls(first));
		result.removeAll(second);
		return result;
	}
	public static <T> TreeSet<T> minus(final Collection<T> first, final Collection<T> second, Comparator<T> c){
		TreeSet<T> result = new TreeSet<T>(c);
		result.addAll(CollectionTool.removeNulls(first));
		result.removeAll(second);
		return result;
	}
	public static <T> Set<T> union(final Collection<T> first, final Collection<T> second){
		Set<T> union = new HashSet<T>(CollectionTool.removeNulls(first));
		union.addAll(second);
		return union;
	}
	
	public static <T> Set<T> intersection(final Collection<T> first, final Collection<T> second){
		Set<T> intersection = new HashSet<T>(CollectionTool.removeNulls(first));
		intersection.retainAll(second);
		return intersection;
	}

	public static <T> Set<T> difference(final Collection<T> first, final Collection<T> second){
		Set<T> difference = new HashSet<T>(CollectionTool.removeNulls(first));
		difference.removeAll(second);
		return difference;
	}
	
	public static <T> TreeSet<T> union(final Collection<T> first, final Collection<T> second, Comparator<T> c){
		TreeSet<T> union = new TreeSet<T>(c);
		union.addAll(CollectionTool.removeNulls(first));
		union.addAll(second);
		return union;
	}
	
	public static <T> TreeSet<T> intersection(final Collection<T> first, final Collection<T> second, Comparator<T> c){
		TreeSet<T> intersection = new TreeSet<T>(c);
		intersection.addAll(CollectionTool.removeNulls(first));
		intersection.retainAll(second);
		return intersection;
	}

	public static <T> TreeSet<T> difference(final Collection<T> first, final Collection<T> second, Comparator<T> c){
		TreeSet<T> difference = new TreeSet<T>(c);
		difference.addAll(CollectionTool.removeNulls(first));
		difference.removeAll(second);
		return difference;
	}
	public static <T> ArrayList<T> getCopyWithoutElements(Collection<T> ts, Set<T> toOmit){
		ArrayList<T> out = ListTool.createArrayList();
		for(T t : nullSafe(ts)){
			if(toOmit==null || ! toOmit.contains(t)){
				out.add(t);
			}
		}
		return out;
	}
	
	/**
	 * powerset of [a,b,c] is [[a],[b],[c],[a,b],[a,c],[b,c],[a,b,c]]
	 * note: I'm sure there's a smarter way to do this, I just wrote this quick
	 * 		one to drive some test cases. - drp
	 * @param <T>
	 * @param set
	 * @return
	 */
	public static <T> Set<Set<T>> powerSet(Set<T> set){
		Set<Set<T>> powerSet = Sets.newHashSet();
		for(T item : set){
			Set<Set<T>> setCopy = Sets.newHashSet();
			for(Set<T> subSet : powerSet){
				Set<T> subSetCopy = Sets.newHashSet();
				for(T i : subSet){
					subSetCopy.add(i);
				}
				subSetCopy.add(item);
				setCopy.add(subSetCopy);
			}
			Set<T> singleItem = Sets.newHashSet();
			singleItem.add(item);
			setCopy.add(singleItem);
			powerSet.addAll(setCopy);
		}
		return powerSet;
	}
	
	/*********************** sum of elements **********************************/
	
	public static Integer getSumOfIntegers(Collection<? extends Integer> items){
		Integer sum = 0;
		for(Integer item : items){
			sum+=item;
		}
		return sum;
	}
	
	public static Long getSumOfLongs(Collection<? extends Long> items){
		Long sum = 0l;
		for(Long item : items){
			sum+=item;
		}
		return sum;
	}
	
	public static <T> int getSumOfMapOfIntegers(Map<T, ? extends Integer> map){
		if(map==null){ return 0; }
		Integer counter = 0;
		for(T t : map.keySet()){
			counter += map.get(t);
		}
		return counter;
	}
	
	
	/******************************** functor *******************************/
	
	/**
	 * apply func to each object in iter, changing objects but not iter itself
	 * @param <T>
	 * @param func
	 * @param iter
	 */
	public static <T> void apply(Functor<Void,T> func, Iterable<T> iter) {
		for (T t : iter)
			func.invoke(t);
	}
	
	/**
	 * apply func to each object in list, replacing previous list component
	 * @param <T>
	 * @param func
	 * @param list
	 */
	public static <T> void applyInPlace(Functor<T,T> func, List<T> list) {
		int i = 0;
		for (T t : list) {
			list.set(i++, func.invoke(t));
		}
	}
	public static <T> void applyInPlaceWithIndex(Functor<T,Pair<T,Integer>> func, List<T> list) {
		int i = 0;
		for (T t : list) {
			list.set(i++, func.invoke(Pair.create(t,i-1)));
		}
	}
	
	public static <T,Q> LinkedList<Q> map(Functor<Q,T> func, Iterable<T> iter) {
		LinkedList<Q> ret = new LinkedList<Q>();
		for (T t : iter)
			ret.add(func.invoke(t));
		return ret;
	}
		
	public static <T> LinkedList<T> filter(Predicate<T> predicate, Iterable<T> iter) {
		LinkedList<T> lst = new LinkedList<T>();
		for (T t : iter)
			if (predicate.check(t))
				lst.add(t);
		return lst;
	}
	
	
	public static <T, K> Map<T,K> collect(Iterable<T> iter, Functor<K,T> func) {
		Map<T,K> map = MapTool.createHashMap();
		for (T t : iter) 
			map.put(t, func.invoke(t));
		return map;
	}
	
	public static <T,K> Map<K,T> extract(Iterable<T> iter, Functor<K,T> func) {
		Map<K,T> map = MapTool.createHashMap();
		for(T t:iter){
			map.put(func.invoke(t), t);
		}
		return map;
	}
	
	/**
	 * given f(Y)->Z, g(X)->Y returns a functor that does f(g(X))->Z
	 */
	public static <Z, X, Y> Functor<Z, X> compose(final Functor<Z, Y> f, final Functor<Y, X> g) {
		return new Functor<Z,X>() {
			public Z invoke(X param) {
				return f.invoke(g.invoke(param));
			}
		};
	}
	
	
	/************************** binary search *********************************/
	
	//move to ListTool
	public static <T> int firstInsertionPoint(List<? extends Comparable<? super T>> sortedList, T key){
		if(isEmpty(sortedList)){ return 0; }
		int p = Collections.binarySearch(sortedList, key);
		if(p >= 0){
			p = firstEqualElementInSortedList(sortedList, p);
			return p;
		}else{
			p = (-1*p)-1;
			return p;
		}
	}

	//move to ListTool
	public static <T> int lastInsertionPoint(List<? extends Comparable<? super T>> sortedList, T key){
		if(isEmpty(sortedList)){ return 0; }
		int p = Collections.binarySearch(sortedList, key);
		if(p >= 0){
			p = 1 + lastEqualElementInSortedList(sortedList, p);
			return p;
		}else{
			p = (-1*p)-1;
			return p;
		}
	}


	/******************************** tests ***********************************/
	
	public static class CollectionToolTests {
		@Test public void testGetTruncatedCopy(){
			List<Integer> is = new ArrayList<Integer>();
			for(int i=0;i<500;i++){
				is.add(i);
			}
			Assert.assertEquals(500, is.size());
			Assert.assertEquals(0,getTruncatedCopy(is, 0).size());
			Assert.assertEquals(1,getTruncatedCopy(is, 1).size());
			Assert.assertEquals(500,getTruncatedCopy(is, 500).size());
			Assert.assertEquals(500,getTruncatedCopy(is, 501).size());
			Assert.assertEquals(40,getTruncatedCopy(is, 40).size());
		}
		
		@Test public void testMakeSortedList() {
			Set<String> ss = new HashSet<String>();
			ss.add("d word");
			ss.add("e word");
			ss.add("b word");
			ss.add("a word");
			ss.add("c word");
			List<String> l = makeSortedList(ss);
			Assert.assertEquals("a word",l.get(0));
			Assert.assertEquals("b word",l.get(1));
			Assert.assertEquals("c word",l.get(2));
			Assert.assertEquals("d word",l.get(3));
			Assert.assertEquals("e word",l.get(4));
		}
		
		@Test public void testGetSublistCopy(){
			List<Integer> is = new ArrayList<Integer>();
			for(int i=0;i<500;i++){
				is.add(i);
			}
			Assert.assertEquals(20, getSublistCopy(is, 1, 20).size());
			Assert.assertEquals(0, getSublistCopy(is, 1, 20).get(0).intValue());
			Assert.assertEquals(19, getSublistCopy(is, 1, 20).get(19).intValue());
			Assert.assertEquals(20, getSublistCopy(is, 2, 20).size());
			Assert.assertEquals(20, getSublistCopy(is, 2, 20).get(0).intValue());
			Assert.assertEquals(39, getSublistCopy(is, 2, 20).get(19).intValue());
		}
		
		@Test public void testNotEmpty(){
			ArrayList<String> l = new ArrayList<String>();
			Assert.assertFalse(notEmpty(l));
			Assert.assertFalse(notEmpty(null));
//			l.add(null);
//			Assert.assertTrue(notEmpty(l));
			Joiner.on(",").join(l);
		}
		
		@Test public void testGetReversedList(){
			SortedSet<String> x = Sets.newTreeSet();
			x.add("something");
			x.add("something else");
			List<String> reversed = getReversedList(x);
			Assert.assertEquals("something else",reversed.get(0));
			Assert.assertEquals("something",reversed.get(1));
		}
		
		
		@Test public void testPowerSet(){
			Set<String> a = s("a");
			Set<String> b = s("b");
			Set<String> c = s("c");
			Set<String> ab = s("a","b");
			Set<String> bc = s("b","c");
			Set<String> ca = s("c","a");
			Set<String> abc = s("a","b","c");

			Set<Set<String>> powersetA = powerSet(a);
			Assert.assertEquals(1,powersetA.size());
			Assert.assertEquals(1,getFirst(powersetA).size());

			Set<Set<String>> powersetAB = powerSet(ab);
			Assert.assertEquals(3,powersetAB.size());
			Assert.assertTrue(powersetAB.contains(a));
			Assert.assertTrue(powersetAB.contains(b));
			Assert.assertTrue(powersetAB.contains(ab));
			
			Set<Set<String>> powersetABC = powerSet(abc);
			Assert.assertEquals(7,powersetABC.size());
			Assert.assertTrue(powersetABC.contains(a));
			Assert.assertTrue(powersetABC.contains(b));
			Assert.assertTrue(powersetABC.contains(c));
			Assert.assertTrue(powersetABC.contains(ab));
			Assert.assertTrue(powersetABC.contains(bc));
			Assert.assertTrue(powersetABC.contains(ca));
			Assert.assertTrue(powersetABC.contains(abc));
		}
		private static <T> Set<T> s(T... ts){
			Set<T> s = Sets.newHashSet();
			for(T t : ts) s.add(t);
			return s;
		}

		@Test public void testApplyInPlace(){
			List<String> ss = Lists.newArrayList();
			ss.add("a");
			ss.add("b");
			ss.add("c");
			applyInPlace(new Functor<String,String>(){@Override
							public String invoke(String param) {
								return param.toUpperCase();
						}}, ss);
			Assert.assertEquals("A",ss.get(0));
			Assert.assertEquals("B",ss.get(1));
			Assert.assertEquals("C",ss.get(2));
		}

		@Test public void testApplyInPlaceWithIndex(){
			List<String> ss = Lists.newArrayList();
			ss.add("a");
			ss.add("b");
			ss.add("c");
			applyInPlaceWithIndex(new Functor<String,Pair<String,Integer>>(){@Override
							public String invoke(Pair<String,Integer> paramAndIndex) {
								return paramAndIndex.getLeft()+paramAndIndex.getRight();
						}}, ss);
			Assert.assertEquals("a0",ss.get(0));
			Assert.assertEquals("b1",ss.get(1));
			Assert.assertEquals("c2",ss.get(2));
		}
		
		@Test public void testApply(){
			List<String> ss = Lists.newArrayList();
			ss.add("a");
			ss.add("b");
			apply(new Functor<Void,String>(){@Override
							public Void invoke(String param) {
								param.toUpperCase();
								return null;
						}}, ss);
			Assert.assertEquals("a",ss.get(0));
			Assert.assertEquals("b",ss.get(1));
			
			List<MutableBoolean> bb = Lists.newArrayList();
			bb.add(new MutableBoolean(true));
			bb.add(new MutableBoolean(false));
			apply(new Functor<Void,MutableBoolean>(){@Override
				public Void invoke(MutableBoolean param) {
					param.set(!param.get());
					return null;
				}}, bb);
			Assert.assertEquals(false,bb.get(0).get());
			Assert.assertEquals(true,bb.get(1).get());
		}
		
		@Test public void testRandom(){
			List<String> l = Lists.newLinkedList();
			
			l.add("a");	l.add("b");	l.add("c");	l.add("d");	l.add("e");	l.add("f");	l.add("g");	l.add("h");	l.add("i");
			l.add("j");	l.add("k");	l.add("l"); l.add("m");	l.add("n");	l.add("o"); l.add("p");	l.add("q");	l.add("r");
			
			List<String> randomElementsFromLargeList = getRandomSublistFromLargeList(l, 4);
			
			Assert.assertEquals(4, randomElementsFromLargeList.size());
			System.out.println(randomElementsFromLargeList);
		}
	}
		
}
