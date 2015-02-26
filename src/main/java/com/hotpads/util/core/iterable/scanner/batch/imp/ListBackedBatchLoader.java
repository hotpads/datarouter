package com.hotpads.util.core.iterable.scanner.batch.imp;

import java.util.List;

import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchLoader;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;

public class ListBackedBatchLoader<T> extends BaseBatchLoader<T>{

	private List<T> list;
	private int firstIndex;
	private int batchSize;
	
	public ListBackedBatchLoader(List<T> list, int firstIndex, int batchSize){
		this.list = list;
		this.firstIndex = firstIndex;
		this.batchSize = batchSize;
	}

	@Override
	public boolean isLastBatch(){
		 return firstIndex + batchSize >= CollectionTool.size(list);
	}

	@Override
	public BatchLoader<T> getNextLoader(){
		int nextBatchStartIndex = firstIndex + batchSize;
		return new ListBackedBatchLoader<T>(list, nextBatchStartIndex, batchSize);
	}

	@Override
	public BatchLoader<T> call() throws Exception{
		int endIndexExclusive = firstIndex + batchSize;
		List<T> newBatch = ListTool.copyOfRange(list, firstIndex, endIndexExclusive);
		updateBatch(newBatch);
		return this;
	}
	
	
	
}
