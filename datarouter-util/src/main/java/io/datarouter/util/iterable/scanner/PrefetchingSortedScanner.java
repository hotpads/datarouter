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
package io.datarouter.util.iterable.scanner;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.iterable.scanner.imp.ListBackedSortedScanner;
import io.datarouter.util.iterable.scanner.sorted.BaseSortedScanner;
import io.datarouter.util.iterable.scanner.sorted.SortedScanner;

public class PrefetchingSortedScanner<T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	private final ExecutorService exec;
	private final SortedScanner<T> inputScanner;
	private final int batchSize;
	private Future<ArrayList<T>> nextBatchFuture;
	private ListBackedSortedScanner<T> batchScanner;
	private boolean loadedLastBatch;

	public PrefetchingSortedScanner(ExecutorService exec, SortedScanner<T> inputScanner, int batchSize){
		this.exec = exec;
		this.inputScanner = inputScanner;
		this.batchSize = batchSize;
		this.nextBatchFuture = exec.submit(new Prefetcher<>(inputScanner, batchSize));
		this.loadedLastBatch = false;
	}

	@Override
	public T getCurrent(){
		return batchScanner.getCurrent();
	}

	@Override
	public boolean advance(){
		if(batchScanner == null){//first batch
			nextBatch();
			return batchScanner.advance();
		}
		if(batchScanner.advance()){
			return true;
		}
		if(loadedLastBatch){
			return false;
		}
		nextBatch();
		return batchScanner.advance();
	}

	private void nextBatch(){
		ArrayList<T> batch = FutureTool.get(nextBatchFuture);
		batchScanner = new ListBackedSortedScanner<>(batch);
		if(batch.size() < batchSize){
			loadedLastBatch = true;
		}else{
			nextBatchFuture = exec.submit(new Prefetcher<>(inputScanner, batchSize));
		}
	}

	private static class Prefetcher<T extends Comparable<? super T>> implements Callable<ArrayList<T>>{
		private final SortedScanner<T> inputScanner;
		private final int batchSize;

		public Prefetcher(SortedScanner<T> inputScanner, int batchSize){
			this.inputScanner = inputScanner;
			this.batchSize = batchSize;
		}

		@Override
		public ArrayList<T> call(){
			ArrayList<T> batch = new ArrayList<>(batchSize);
			while(batch.size() < batchSize && inputScanner.advance()){//avoid advancing if already full
				batch.add(inputScanner.getCurrent());
			}
			return batch;
		}
	}

}
