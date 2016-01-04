package com.hotpads.datarouter.batch;

import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.batch.databean.OpOptimizedBatchSize;
import com.hotpads.datarouter.batch.databean.OpOptimizedBatchSizeKey;
import com.hotpads.util.core.cache.Cached;

public class CachedOpOptimizedBatchSize extends Cached<OpOptimizedBatchSize>{

	private OpOptimizedBatchSizeKey opOptimizedBatchSizeKey;
	private BatchSizeOptimizerNodes nodes;

	public CachedOpOptimizedBatchSize(BatchSizeOptimizerNodes nodes, OpOptimizedBatchSizeKey opOptimizedBatchSizeKey){
		super(10, TimeUnit.SECONDS);
		this.nodes = nodes;
		this.opOptimizedBatchSizeKey = opOptimizedBatchSizeKey;
	}

	@Override
	protected OpOptimizedBatchSize reload(){
		OpOptimizedBatchSize opOptimizedBatchSize = nodes.getOpOptimizedBatchSize().get(opOptimizedBatchSizeKey, null);
		if(opOptimizedBatchSize == null){
			return OpOptimizedBatchSize.createDefault(opOptimizedBatchSizeKey.getOpName());
		}
		return opOptimizedBatchSize;
	}

}
