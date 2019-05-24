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
package io.datarouter.util.iterable.scanner.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.iterable.scanner.Scanner;
import io.datarouter.util.iterable.scanner.batch.imp.ListBackedBatchLoader;
import io.datarouter.util.iterable.scanner.sorted.BaseSortedScanner;

public class AsyncBatchLoaderScanner<T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	private final ExecutorService executorService;
	private Future<BatchLoader<T>> currentBatchFuture;
	private Future<BatchLoader<T>> loadingBatchFuture;
	private boolean didInitialPrefetch = false;

	/*------------------------- constructors --------------------------------*/

	public AsyncBatchLoaderScanner(ExecutorService executorService, BatchLoader<T> headOfLoaderChain){
		this.executorService = executorService;
		this.currentBatchFuture = this.executorService.submit(headOfLoaderChain);
	}

	/*------------------------- methods -------------------------------------*/

	@Override
	public boolean advance(){
		BatchLoader<T> currentLoader = getCurrentLoader();//synchronous wait for the batch to load
		if(currentLoader == null){
			return false;
		}

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
	public void cleanup(){
		BatchLoader<T> currentBatch = getCurrentLoader();
		if(currentBatch != null){
			currentBatch.cleanup();
		}
	}

	@Override
	public T getCurrent(){
		BatchLoader<T> currentBatch = getCurrentLoader();
		if(currentBatch == null){
			return null;
		}
		return currentBatch.getCurrent();
	}

	private void triggerInitialPrefetchIfNotDoneAlready(){
		BatchLoader<T> currentBatch = getCurrentLoader();
		if(!didInitialPrefetch
				&& currentBatch != null
				&& !currentBatch.isLastBatch()){
			loadingBatchFuture = executorService.submit(currentBatch.getNextLoader());
			didInitialPrefetch = true;
		}
	}

	private void advanceTheLoaders(){
		BatchLoader<T> loadingBatch = FutureTool.get(loadingBatchFuture);
		currentBatchFuture = loadingBatchFuture;
		if(!loadingBatch.isLastBatch()){
			loadingBatchFuture = executorService.submit(loadingBatch.getNextLoader());
		}else{
			loadingBatchFuture = null;
		}
	}

	private BatchLoader<T> getCurrentLoader(){
		return FutureTool.get(currentBatchFuture);
	}

	/*------------------------- get/set -------------------------------------*/

	public static class BatchingSortedScannerTests{

		private static final int MULTIPLIER = 3;
		private List<Integer> createTestArray(int numElements){
			List<Integer> testArray = new ArrayList<>();
			for(int i = 0; i < numElements; ++i){
				testArray.add(i * MULTIPLIER);//separate the values from the indexes
			}
			return testArray;
		}

		@Test
		public void testNumElements(){
			// testIndividualNumElements(0, 1);//for debugging

			for(int numElements = 0; numElements < 30; ++numElements){//run 50 times with different batch sizes
				for(int batchSize = 1; batchSize < 10; ++batchSize){//watch out: batchSize=0 is no good
					testIndividualNumElements(numElements, batchSize);
				}
			}
		}

		protected void testIndividualNumElements(int numElements, int batchSize){
			List<Integer> testArray = createTestArray(numElements);
			BatchLoader<Integer> headLoader = new ListBackedBatchLoader<>(testArray, 0, batchSize);
			Scanner<Integer> scanner = new AsyncBatchLoaderScanner<>(Executors.newFixedThreadPool(1),
					headLoader);
			int counter = 0;
			int testArrayIndex = -1;
			while(scanner.advance()){
				++counter;
				++testArrayIndex;
				Integer known = testArrayIndex * MULTIPLIER;//matches the input formula
				Integer expected = testArray.get(testArrayIndex);
				Assert.assertEquals(expected, known);
				Integer actual = scanner.getCurrent();
				Assert.assertEquals(actual, expected);
			}
			Assert.assertEquals(counter, numElements);
		}
	}
}
