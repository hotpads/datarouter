package com.hotpads.datarouter.op.scan;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchBackedScanner;

public abstract class BaseManagedIndexKeyScanner<
		PK extends PrimaryKey<PK>, 
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>>
extends BaseBatchBackedScanner<IK,IK>{
	
	private final Range<IK> range;
	protected final Config config;
	
	public BaseManagedIndexKeyScanner(Range<IK> range, Config config){
		this.range = Range.nullSafe(range);
		this.config = Config.nullSafe(config).getDeepCopy();
		this.config.setLimit(this.config.getIterateBatchSize());
	}

	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		IK lastRowOfPreviousBatch = range.getStart();
		boolean isStartInclusive = range.getStartInclusive();
		if (currentBatch != null){
			IK endOfLastBatch = DrCollectionTool.getLast(currentBatch);
			if (endOfLastBatch == null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = endOfLastBatch;
			isStartInclusive = false;
		}
		Range<IK> batchRange = new Range<>(lastRowOfPreviousBatch, isStartInclusive, range.getEnd(),
				range.getEndInclusive());

		currentBatch = doLoad(batchRange);
		
		if (DrCollectionTool.size(currentBatch) < config.getIterateBatchSize()){
			noMoreBatches = true;
		}
	}
	
	@Override
	protected void setCurrentFromResult(IK result){
		this.current = result;
	}

	protected abstract List<IK> doLoad(Range<IK> batchRange);
	
}
