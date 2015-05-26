package com.hotpads.datarouter.client.imp.jdbc.scan;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderOps;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchingSortedScanner;

public class JdbcManagedIndexScanner<
		PK extends PrimaryKey<PK>, 
		D extends Databean<PK, D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends BaseBatchingSortedScanner<IE,IE>{
	
	private final DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final Range<IK> range;
	private final JdbcReaderOps<PK, D, F> jdbcReaderOps;
	private final Config config;

	public JdbcManagedIndexScanner(JdbcReaderOps<PK, D, F> jdbcReaderOps,
			DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo, Range<IK> range, Config config){
		this.jdbcReaderOps = jdbcReaderOps;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.range = Range.nullSafe(range);
		this.config = Config.nullSafe(config).setIterateBatchSizeIfNull(JdbcNode.DEFAULT_ITERATE_BATCH_SIZE);
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

		currentBatch = doLoad(batchRange);
		
		if (DrCollectionTool.size(currentBatch) < BATCH_SIZE_DEFAULT){
			noMoreBatches = true;
		}
	}

	@Override
	protected void setCurrentFromResult(IE result){
		this.current = result;
	}

	private List<IE> doLoad(Range<IK> range){
		return jdbcReaderOps.getIndexRange(range, config, indexEntryFieldInfo);
	}
	
}
