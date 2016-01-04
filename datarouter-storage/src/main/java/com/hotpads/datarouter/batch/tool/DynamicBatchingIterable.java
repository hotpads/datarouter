package com.hotpads.datarouter.batch.tool;

import java.util.Iterator;
import java.util.List;

import com.hotpads.datarouter.batch.BatchSizeOptimizer;

public class DynamicBatchingIterable<T> implements Iterable<List<T>>{

	private Iterable<T> backingIterable;
	private BatchSizeOptimizer optimizer;
	private String opName;

	public DynamicBatchingIterable(Iterable<T> backingIterable, BatchSizeOptimizer optimizer, String opName){
		this.backingIterable = backingIterable;
		this.optimizer = optimizer;
		this.opName = opName;
	}

	@Override
	public Iterator<List<T>> iterator(){
		return new DynamicBatchingIterator<>(backingIterable.iterator(), optimizer, opName);
	}

}
