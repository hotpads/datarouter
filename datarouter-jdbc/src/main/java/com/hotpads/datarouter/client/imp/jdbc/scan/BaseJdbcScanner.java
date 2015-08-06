package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchBackedScanner;

public abstract class BaseJdbcScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		T extends Comparable<? super T>>//T should be either PK or D
extends BaseBatchBackedScanner<T,T>{
	
	private long resultCount;
	private Range<PK> range;
	private Config config;
	private Config batchConfig;
	
	public BaseJdbcScanner(Range<PK> range, Config config){
		this.range = range;
		this.config = Config.nullSafe(config);
		this.noMoreBatches = false;
		this.resultCount = 0;
		this.batchConfig = this.config.getDeepCopy();
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
		

		int batchConfigLimit = this.config.getIterateBatchSize();
		if(this.config.getLimit() != null && this.config.getLimit() - resultCount < batchConfigLimit){
			batchConfigLimit = (int) (this.config.getLimit() - resultCount);
		}
		batchConfig.setLimit(batchConfigLimit);
		
		currentBatch = doLoad(batchRange, batchConfig);
		resultCount += currentBatch.size();
		if(DrCollectionTool.size(currentBatch) < batchConfig.getLimit()
				|| (config.getLimit() != null && resultCount >= config.getLimit())){
			noMoreBatches = true;//tell the advance() method not to call this method again
		}
	}
	
}
