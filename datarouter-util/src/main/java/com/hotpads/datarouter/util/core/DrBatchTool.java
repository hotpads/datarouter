package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DrBatchTool {


	public static int getNumBatches(int totalSize, int batchSize){
		if(totalSize==0){ return 0; }
		int numFullBatches = totalSize / batchSize;
		int numLeftover = totalSize % batchSize;
		if(numLeftover == 0){
			return numFullBatches;
		}
		return 1 + numFullBatches;
	}

	public static int getBatchStartIndex(int totalSize, int batchSize, int batchNumZeroBased){
		if(totalSize == 0){
			return 0;
		}
		return batchNumZeroBased*batchSize;
	}

	public static int getBatchEndIndexExclusive(int totalSize, int batchSize, int batchNumZeroBased){
		if(totalSize == 0){
			return 0;
		}
		int numBatches = getNumBatches(totalSize, batchSize);
		int endIndex;
		if(batchNumZeroBased == (numBatches-1)){
			endIndex = totalSize - 1;
		}else{
			endIndex = batchNumZeroBased * batchSize + batchSize - 1;
		}
		return endIndex + 1;
	}


	public static <T> int getNumBatches(Collection<T> collection, int batchSize){
		if(DrCollectionTool.isEmpty(collection)){ return 0; }
		return getNumBatches(collection.size(), batchSize);
	}

	public static <T> List<T> getBatch(List<T> list, int batchSize, int batchNumZeroBased){
		if(list == null || list.size() == 0){
			return new ArrayList<>();
		}
		int startIndex = getBatchStartIndex(list.size(), batchSize, batchNumZeroBased);
		int endIndex = getBatchEndIndexExclusive(list.size(), batchSize, batchNumZeroBased);
		if(startIndex >= list.size() || endIndex > list.size()){
			return new ArrayList<>();
		}
		List<T> batch = list.subList(startIndex, endIndex);
		return batch;
	}

	public static <T> List<List<T>> getBatches(Collection<T> all, int batchSize){
		int numBatches = getNumBatches(all, batchSize);
		List<List<T>> batches = new ArrayList<>(numBatches);
		if(all==null||all.size()<1){
			return batches;
		}
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

	public static class Tests{
		@Test public void testMany(){
			List<String> list = new ArrayList<>();
			list.add("a");
			list.add("b");
			list.add("c");
			list.add("d");
			list.add("e");
			Assert.assertEquals(3, getNumBatches(5,2));
			Assert.assertEquals(4, getBatchEndIndexExclusive(5,2,1));
			Assert.assertEquals(DrListTool.createArrayList("c","d"), getBatch(list, 2, 1));
			Assert.assertEquals(Arrays.asList("a","b"), list.subList(0,2));
			Assert.assertEquals(Arrays.asList("c","d"), list.subList(2,4));
		}

		@Test public void testUnreachableBatches(){
			List<String> list = new LinkedList<>();
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
}
