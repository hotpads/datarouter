package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchingSortedScanner;

public abstract class BaseJdbcScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		T extends Comparable<? super T>>//T should be either PK or D
extends BaseBatchingSortedScanner<T,T>{
	
	protected Range<PK> range;
	protected Config config;
	
	public BaseJdbcScanner(Range<PK> range, Config pConfig){
		this.range = range;
		this.config = pConfig == null ? new Config() : pConfig.getDeepCopy();
		this.config.setIterateBatchSizeIfNull(Config.DEFAULT_ITERATE_BATCH_SIZE);//why is this necessary?
		this.noMoreBatches = false;
	}
	
	protected abstract PK getPrimaryKey(T fieldSet);
	protected abstract List<T> doLoad(Range<PK> range, Config config);
	
	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		PK lastRowOfPreviousBatch = range.getStart();
		boolean isStartInclusive = range.getStartInclusive();//only on the first load
		if(currentBatch != null){
			T endOfLastBatch = DrCollectionTool.getLast(currentBatch);
			if(endOfLastBatch==null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = getPrimaryKey(endOfLastBatch);
			isStartInclusive = false;
		}
		Range<PK> batchRange = Range.create(lastRowOfPreviousBatch, isStartInclusive, range.getEnd(), 
				range.getEndInclusive());
		
		//unfortunately we need to overwrite the limit.  the original pConfig should be unaffected
		config.setLimit(config.getIterateBatchSize());
		currentBatch = doLoad(batchRange, config);
		if(DrCollectionTool.size(currentBatch) < config.getIterateBatchSize()){
			noMoreBatches = true;//tell the advance() method not to call this method again
		}
	}
	
}
