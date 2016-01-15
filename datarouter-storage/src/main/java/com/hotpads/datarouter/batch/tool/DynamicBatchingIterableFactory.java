package com.hotpads.datarouter.batch.tool;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.batch.BatchSizeOptimizer;

@Singleton
public class DynamicBatchingIterableFactory{

	@Inject
	private BatchSizeOptimizer optimizer;

	public <T> DynamicBatchingIterable<T> batch(Iterable<T> backingIterable, String opName){
		return new DynamicBatchingIterable<>(backingIterable, optimizer, opName);
	}

}
