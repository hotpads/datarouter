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
package io.datarouter.batchsizeoptimizer.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.datarouter.batchsizeoptimizer.BatchSizeOptimizer;

public class DynamicBatchingIterator<T> implements Iterator<List<T>>{

	private BatchSizeOptimizer optimizer;
	private Iterator<T> backingIterator;
	private String opName;
	private Integer currentBatchSize;
	private Long lastCallTimestamp;

	public DynamicBatchingIterator(Iterator<T> backingIterator, BatchSizeOptimizer optimizer, String opName){
		this.backingIterator = backingIterator;
		this.optimizer = optimizer;
		this.opName = opName;
	}

	@Override
	public boolean hasNext(){
		if(currentBatchSize != null && lastCallTimestamp != null){
			long timeSpent = System.currentTimeMillis() - lastCallTimestamp;
			optimizer.recordBatchSizeAndTime(opName, currentBatchSize, currentBatchSize, timeSpent);
		}
		return backingIterator.hasNext();
	}

	@Override
	public List<T> next(){
		currentBatchSize = optimizer.getRecommendedBatchSize(opName);
		List<T> batch = new ArrayList<>(currentBatchSize);
		while(batch.size() < currentBatchSize && backingIterator.hasNext()){
			batch.add(backingIterator.next());
		}
		currentBatchSize = batch.size();
		lastCallTimestamp = System.currentTimeMillis();
		return batch;
	}

}
