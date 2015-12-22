package com.hotpads.datarouter.batch.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hotpads.datarouter.batch.BatchSizeOptimizer;

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
