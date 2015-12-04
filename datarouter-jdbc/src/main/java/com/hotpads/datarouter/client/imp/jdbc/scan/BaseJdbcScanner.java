package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
	private SortedSet<Range<PK>> ranges;
	private Config config;
	private Config batchConfig;

	public BaseJdbcScanner(Collection<Range<PK>> ranges, Config config){
		this.ranges = new TreeSet<>(ranges);
		this.config = Config.nullSafe(config);
		this.noMoreBatches = false;
		this.resultCount = 0;
		this.batchConfig = this.config.getDeepCopy();
	}

	protected abstract PK getPrimaryKey(T fieldSet);
	protected abstract List<T> doLoad(Collection<Range<PK>> ranges, Config config);

	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		if(currentBatch != null){
			T endOfLastBatch = DrCollectionTool.getLast(currentBatch);
			if(endOfLastBatch==null){
				currentBatch = null;
				return;
			}
			PK lastRowOfPreviousBatch = getPrimaryKey(endOfLastBatch);
			Range<PK> previousRange = null;
			SortedSet<Range<PK>> remainingRanges = new TreeSet<>(ranges);
			for(Range<PK> range : ranges){
				if(previousRange != null){
					if(range.getStart() != null && range.getStart().compareTo(lastRowOfPreviousBatch) <= 0){
						remainingRanges.remove(previousRange);
					}else{
						break;
					}
				}
				previousRange = range;
			}
			ranges = remainingRanges;
			Range<PK> firstRange = ranges.first().copy();
			firstRange.setStart(lastRowOfPreviousBatch);
			firstRange.setStartInclusive(false);
			ranges.remove(ranges.first());
			ranges.add(firstRange);
		}

		int batchConfigLimit = this.config.getIterateBatchSize();
		if(this.config.getLimit() != null && this.config.getLimit() - resultCount < batchConfigLimit){
			batchConfigLimit = (int) (this.config.getLimit() - resultCount);
		}
		batchConfig.setLimit(batchConfigLimit);

		currentBatch = doLoad(ranges, batchConfig);
		batchConfig.setOffset(0);
		resultCount += currentBatch.size();
		if(DrCollectionTool.size(currentBatch) < batchConfig.getLimit()
				|| config.getLimit() != null && resultCount >= config.getLimit()){
			noMoreBatches = true;//tell the advance() method not to call this method again
		}
	}

}
