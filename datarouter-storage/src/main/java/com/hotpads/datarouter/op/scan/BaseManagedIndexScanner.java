package com.hotpads.datarouter.op.scan;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchBackedScanner;

public abstract class BaseManagedIndexScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>>
extends BaseBatchBackedScanner<IE,IE>{

	private final Range<IK> range;
	private final Config config;
	private final Config batchConfig;
	private long resultCount;

	public BaseManagedIndexScanner(Range<IK> range, Config config){
		this.range = Range.nullSafe(range);
		this.config = Config.nullSafe(config).getDeepCopy();
		this.batchConfig = this.config.getDeepCopy();
		this.resultCount = 0;
	}

	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		IK lastRowOfPreviousBatch = range.getStart();
		boolean isStartInclusive = range.getStartInclusive();
		if (currentBatch != null){
			IE endOfLastBatch = DrCollectionTool.getLast(currentBatch);
			if (endOfLastBatch == null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = endOfLastBatch.getKey();
			isStartInclusive = false;
		}
		Range<IK> batchRange = new Range<>(lastRowOfPreviousBatch, isStartInclusive, range.getEnd(),
				range.getEndInclusive());

		int batchConfigLimit = config.getIterateBatchSize();
		if(config.getLimit() != null && config.getLimit() - resultCount < batchConfigLimit){
			batchConfigLimit = (int) (config.getLimit() - resultCount);
		}
		batchConfig.setLimit(batchConfigLimit);

		currentBatch = doLoad(batchRange, batchConfig);
		resultCount += currentBatch.size();

		if(DrCollectionTool.size(currentBatch) < batchConfig.getLimit()
				|| config.getLimit() != null && resultCount >= config.getLimit()){
			noMoreBatches = true;
		}
	}

	@Override
	protected void setCurrentFromResult(IE result){
		this.current = result;
	}

	protected abstract List<IE> doLoad(Range<IK> batchRange, Config config);

}
