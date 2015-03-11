package com.hotpads.util.core.iterable.scanner.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.concurrent.FutureTool;
import com.hotpads.util.core.iterable.scanner.batch.imp.ListBackedBatchLoader;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class BatchingSortedScanner<T extends Comparable<? super T>> 
extends	BaseSortedScanner<T>{
	

	private ExecutorService executorService;	
	private Future<BatchLoader<T>> currentBatchFuture;
	private Future<BatchLoader<T>> loadingBatchFuture;
	private boolean didInitialPrefetch = false;

	/**************** constructors *******************************/
	
	public BatchingSortedScanner(ExecutorService executorService, BatchLoader<T> headOfLoaderChain){
		this.executorService = executorService;
		this.currentBatchFuture = this.executorService.submit(headOfLoaderChain);
	}
	
	
	/****************** methods *************************************/
	

	@Override
	public boolean advance() {
		BatchLoader<T> currentLoader = getCurrentLoader();//synchronous wait for the batch to load
		if(currentLoader==null){ return false; }
		
		triggerInitialPrefetchIfNotDoneAlready();
		
		//not too complicated but could prob be simplified further
		if(!currentLoader.advance()){
			if(currentLoader.isLastBatch()){
				return false;//we were on the last batch so are totally finished now
			}
			
			advanceTheLoaders();
			currentLoader = getCurrentLoader();//refresh the pointer after advancing the batch.  fragile =(
			if(!currentLoader.advance()){//the next batch came back empty
				return false;
			}
		}
		return true;
	}
	
	@Override
	public T getCurrent(){
		BatchLoader<T> currentBatch = getCurrentLoader();
		if(currentBatch==null){ return null; }
		return currentBatch.getCurrent();
	}
	
	private void triggerInitialPrefetchIfNotDoneAlready(){
		BatchLoader<T> currentBatch = getCurrentLoader();
		if(!didInitialPrefetch 
				&& currentBatch!=null
				&& ! currentBatch.isLastBatch()){
			loadingBatchFuture = executorService.submit(currentBatch.getNextLoader());
			didInitialPrefetch = true;
		}
	}
	
	private void advanceTheLoaders(){
		BatchLoader<T> loadingBatch = FutureTool.get(loadingBatchFuture);
		currentBatchFuture = loadingBatchFuture;
		if( ! loadingBatch.isLastBatch()){
			loadingBatchFuture = executorService.submit(loadingBatch.getNextLoader());
		}else{
			loadingBatchFuture = null;
		}
	}
	
//	private void checkIfNextBatchNeedsTriggering(){
//		BatchLoader<T> currentBatch = getCurrentBatch();
//		if(currentBatch!=null 
//				&& !currentBatch.isLastBatch() 
//				&& loadingBatchFuture==null){
//		}
//	}
	
	private BatchLoader<T> getCurrentLoader(){
		return FutureTool.get(currentBatchFuture);
	}
	
	
	/******************* get/set ****************************************/
	
//	public BaseBatchingSortedScanner<T,B> setBatchSize(int batchSize){
//		this.batchSize = batchSize;
//		return this;
//	}
	
	public static class BatchingSortedScannerTests{
		private static final int MULTIPLIER = 3;
		private List<Integer> createTestArray(int numElements){
			List<Integer> testArray = new ArrayList<>();
			for(int i=0; i < numElements; ++i){
				testArray.add(i * MULTIPLIER);//separate the values from the indexes
			}
			return testArray;
		}
		
		@Test public void testNumElements(){
//			testIndividualNumElements(0, 1);//for debugging
			
			for(int numElements=0; numElements < 30; ++numElements){//run 50 times with different batch sizes
				for(int batchSize=1; batchSize < 10; ++batchSize){//watch out: batchSize=0 is no good
					testIndividualNumElements(numElements, batchSize);
				}
			}
		}
		
		protected void testIndividualNumElements(int numElements, int batchSize){
			List<Integer> testArray = createTestArray(numElements);
			BatchLoader<Integer> headLoader = new ListBackedBatchLoader<Integer>(testArray, 0, batchSize);
			SortedScanner<Integer> scanner = new BatchingSortedScanner<Integer>(Executors.newFixedThreadPool(1),
					headLoader);
			int counter = 0;
			int testArrayIndex = -1;
			while(scanner.advance()){
				++counter;
				++testArrayIndex;
				Integer known = testArrayIndex * MULTIPLIER;//matches the input formula
				Integer expected = testArray.get(testArrayIndex);
				Assert.assertEquals(known, expected);
				Integer actual = scanner.getCurrent();
				Assert.assertEquals(expected, actual);
			}
			Assert.assertEquals(numElements, counter);
		}
	}

	
}
