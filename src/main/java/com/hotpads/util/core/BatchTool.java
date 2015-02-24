package com.hotpads.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Test;

public class BatchTool {
	

	public static int getNumBatches(int totalSize, int batchSize){
		if(totalSize==0){ return 0; }
		int numFullBatches = totalSize / batchSize;
		int numLeftover = totalSize % batchSize;
		if(numLeftover == 0){
			return numFullBatches;
		}else{
			return 1 + numFullBatches;
		}
	}
	
	public static int getBatchStartIndex(int totalSize, int batchSize, int batchNumZeroBased){
		if(totalSize == 0){
			return 0;
		}else{
			return batchNumZeroBased*batchSize;
		}
	}
	
	public static int getBatchEndIndexExclusive(int totalSize, int batchSize, int batchNumZeroBased){
		if(totalSize == 0){
			return 0;
		}else{
			int numBatches = getNumBatches(totalSize, batchSize);
			int endIndex;
			if(batchNumZeroBased == (numBatches-1)){
				endIndex = totalSize - 1;
			}else{
				endIndex = batchNumZeroBased * batchSize + batchSize - 1;
			}
			return endIndex + 1;
		}
	}
	
	
	public static <T> int getNumBatches(Collection<T> collection, int batchSize){
		if(CollectionTool.isEmpty(collection)){ return 0; }
		return getNumBatches(collection.size(), batchSize);
	}
	
	public static <T> List<T> getBatch(List<T> list, int batchSize, int batchNumZeroBased){
		if(list == null || list.size() == 0){
			return new ArrayList<T>();
		}else{
			int startIndex = getBatchStartIndex(list.size(), batchSize, batchNumZeroBased);
			int endIndex = getBatchEndIndexExclusive(list.size(), batchSize, batchNumZeroBased);
			if(startIndex >= list.size() || endIndex > list.size()){
				return new ArrayList<T>();
			}
			List<T> batch = list.subList(startIndex, endIndex);
			return batch;
		}
	}
	
	/**
	 * Get a batch of a set. Note that the set must have predictable iteration 
	 * order or the resulting batches won't be consistent and could potentially
	 * contain duplicate elements.
	 * 
	 * @param <T>
	 * @param set with predictable iteration order
	 * @param batchSize
	 * @param batchNumZeroBased
	 * @return
	 */
	public static <T>List<T> getBatch(Set<T> set, int batchSize, int batchNumZeroBased){
		if(set==null || set.size() == 0){
			return new ArrayList<T>();
		}else{
			int startIndex = 
				getBatchStartIndex(set.size(), batchSize, batchNumZeroBased);
			int endIndex = 
				getBatchEndIndexExclusive(set.size(), batchSize, batchNumZeroBased);
			List<T> batch = new ArrayList<T>(batchSize);
			Iterator<T> iterator = set.iterator();
			int i = 0;
			while(startIndex > i && iterator.hasNext()){
				i++;
				iterator.next(); //ignore it
			}
			
			i = startIndex;
			while(endIndex > i && iterator.hasNext()){
				i++;
				batch.add(iterator.next());
			}
			return batch;
		}
	}
	
	public static <T> List<List<T>> getBatches(List<T> list, int batchSize){
		int numBatches = getNumBatches(list, batchSize);
		List<List<T>> batches = new ArrayList<List<T>>(numBatches);
		for(int i=0; i < numBatches; ++i){
			batches.add(getBatch(list, batchSize, i));
		}
		return batches;
	}
	
	public static <T> List<List<T>> getBatches(Collection<T> all, int batchSize){
		int numBatches = getNumBatches(all, batchSize);
		List<List<T>> batches = new ArrayList<List<T>>(numBatches);
		if(all==null||all.size()<1) return batches;
		int numComplete = 0;
		int batchNum = -1;
		for(T t : all){
			if(numComplete % batchSize == 0){
				batches.add(new ArrayList<T>(batchSize));
				++batchNum;
			}
			batches.get(batchNum).add(t);
			++numComplete;
		}
		return batches;
	}
	
	public static <K,V> List<SortedMap<K,V>> getBatches(SortedMap<K,V> map, int batchSize){
		List<SortedMap<K,V>> out = ListTool.createArrayList();
		if(MapTool.isEmpty(map)){ return out; }
		K startKeyInclusive = map.firstKey();
		int batchCounter = 0;
		for(K k : map.keySet()){
			++batchCounter;
			if(batchCounter == batchSize+1){//need to move the iterator 1 further than the last element
				SortedMap<K,V> batch = map.subMap(startKeyInclusive, k);//k is exclusive
				out.add(batch);
				batchCounter = 1;//1 because we iterated past the end of the batch
				startKeyInclusive = k;
			}
		}
		//there will always be a tailmap
		SortedMap<K,V> tail = map.tailMap(startKeyInclusive);
		out.add(tail);
		return out;
	}

	public static class Tests{
		@Test public void testMapBatching(){
			SortedMap<Integer,Character> map = MapTool.createTreeMap();
			map.put(1,'a');
			map.put(2,'b');
			map.put(3,'c');
			map.put(4,'d');
			map.put(5,'e');
			map.put(6,'f');
			List<SortedMap<Integer,Character>> batchesOf1 = getBatches(map,1);
			List<SortedMap<Integer,Character>> batchesOf2 = getBatches(map,2);
			List<SortedMap<Integer,Character>> batchesOf3 = getBatches(map,3);
			List<SortedMap<Integer,Character>> batchesOf5 = getBatches(map,5);
			List<SortedMap<Integer,Character>> batchesOf6 = getBatches(map,6);
			List<SortedMap<Integer,Character>> batchesOf11 = getBatches(map,11);
			Assert.assertEquals(6, batchesOf1.size());
			Assert.assertEquals(3, batchesOf2.size());
			Assert.assertEquals(2, batchesOf3.size());
			Assert.assertEquals(2, batchesOf5.size());
			Assert.assertEquals(1, batchesOf6.size());
			Assert.assertEquals(1, batchesOf11.size());
			Assert.assertEquals(new Integer(5), batchesOf2.get(2).firstKey());
			
		}
		
		@Test public void testMany(){
			List<String> list = new ArrayList<String>();
			list.add("a");
			list.add("b");
			list.add("c");
			list.add("d");
			list.add("e");
			Assert.assertEquals(3, getNumBatches(5,2));
			Assert.assertEquals(4, getBatchEndIndexExclusive(5,2,1));
			Assert.assertEquals(ListTool.createArrayList("c","d"), getBatch(list, 2, 1));
			Assert.assertEquals(GenericsFactory.makeArrayList("a","b"), list.subList(0,2));
			Assert.assertEquals(GenericsFactory.makeArrayList("c","d"), list.subList(2,4));
		}
		
		@Test public void testSetBatching(){
			//must use a set with predictable iteration order
			Set<String> set = new LinkedHashSet<String>();
			
			List<String> batch = getBatch(set,1,0);
			Assert.assertEquals(0,batch.size());
			
			batch = getBatch(set,1,1);
			Assert.assertEquals(0,batch.size());
			
			set.add("a");
			
			batch = getBatch(set,1,0);
			Assert.assertEquals(1,batch.size());

			batch = getBatch(set,1,1);
			Assert.assertEquals(0,batch.size());
			
			set.add("b");
			set.add("c");
			set.add("d");
			set.add("e");

			List<String> batch0 = getBatch(set,2,0);
			List<String> batch1 = getBatch(set,2,1);
			List<String> batch2 = getBatch(set,2,2);
			Assert.assertEquals(2, batch0.size());
			Assert.assertEquals(2, batch1.size());
			Assert.assertEquals(1, batch2.size());
			
			Set<String> results = new LinkedHashSet<String>();
			for(int i=0; i<set.size(); i++){
				results.add(getBatch(set,1,i).get(0));
			}
			Assert.assertEquals(set.size(),results.size());
		}
		
		@Test public void testUnreachableBatches(){
			List<String> list = new LinkedList<String>();
			list.add("a"); list.add("b"); list.add("c"); list.add("d");
			
			List<String> batch = getBatch(list,2,0);
			Assert.assertEquals(2,batch.size());
			
			batch = getBatch(list,2,1);
			Assert.assertEquals(2,batch.size());
			
			batch = getBatch(list,2,2);
			Assert.assertEquals(0,batch.size());
			
			batch = getBatch(list,3,0);
			Assert.assertEquals(3,batch.size());
			batch = getBatch(list,3,1);
			Assert.assertEquals(1,batch.size());
			batch = getBatch(list,3,2);
			Assert.assertEquals(0,batch.size());
			
			batch = getBatch(list,3,200);
			Assert.assertEquals(0,batch.size());
		}
		
	}
	
	public static void main(String[] args){
	}
}
