package com.hotpads.datarouter.client.imp.hbase.batching.entity;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchLoader;

public abstract class BaseHBaseEntityBatchLoader<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T extends Comparable<? super T>> //T will be either PK or D, but not going to express that (or think about how to)
extends BaseBatchLoader<T>{
	
	private static final boolean ASSERT_PARTITION = true;
	private static final boolean ASSERT_ORDERING = true;

	protected final HBaseSubEntityReaderNode<EK,E,PK,D,F> node;
	protected final int partition;
	protected final byte[] partitionBytes;
	protected final Range<PK> range;
	protected final Config config;
	protected final Integer iterateBatchSize;//break this out of config for safety
	protected Long batchChainCounter;

	public BaseHBaseEntityBatchLoader(final HBaseSubEntityReaderNode<EK,E,PK,D,F> node, int partition,
			final byte[] partitionBytes, final Range<PK> range, final Config config, Long batchChainCounter){
		this.node = node;
		this.partition = partition;
		this.partitionBytes = partitionBytes;
		this.range = range;
		this.config = Config.nullSafe(config);
		this.iterateBatchSize = this.config.getIterateBatchSize();
		this.config.setIterateBatchSize(iterateBatchSize);
		this.batchChainCounter = batchChainCounter;
	}

	abstract boolean isKeysOnly();
	abstract List<T> parseHBaseResult(Result result);
	abstract PK getLastPrimaryKeyFromBatch();


	@Override
	public BaseHBaseEntityBatchLoader<EK,E,PK,D,F,T> call(){
		//do the RPC
		List<Result> hbaseRows = node.getResultsInSubRange(partition, range, isKeysOnly(), config);
		List<T> outs = DrListTool.createArrayListWithSize(hbaseRows);
		for(Result row : hbaseRows){
			if (row == null || row.isEmpty()){
				continue;
			}
			if(ASSERT_PARTITION){
				assertPartition(row);
			}
			List<T> results = parseHBaseResult(row);
			if(ASSERT_ORDERING){
				assertOrdering(outs, results);
			}
			outs.addAll(DrCollectionTool.nullSafe(results));
		}
		updateBatch(outs);

		return this;
	}

	protected Range<PK> getNextRange(){
		PK lastPkFromPreviousBatch = getLastPrimaryKeyFromBatch();
		Range<PK> nextRange = Range.create(lastPkFromPreviousBatch, false, range.getEnd(), range.getEndInclusive());
		return nextRange;
	}

	@Override
	public boolean isLastBatch(){
		//refer to the dedicated iterateBatchSize field in case someone changed Config down the line
		return isBatchHasBeenLoaded() && isBatchSmallerThan(iterateBatchSize);
	}
	
	
	/********************* private *****************************/
	
	private void assertPartition(Result row){
		int length = partitionBytes.length;
		if(!DrByteTool.equals(partitionBytes, 0, length, row.getRow(), 0, length)){
			String rowString = Bytes.toStringBinary(row.getRow());
			String prefixString = Bytes.toStringBinary(partitionBytes);
			String message = String.format("result %s is not in partition %d with prefix %s", rowString, partition,
					prefixString);
			throw new RuntimeException(message);
		}
	}
	
	private void assertOrdering(List<T> existingResults, List<T> newResults){
		if(DrCollectionTool.notEmpty(existingResults) && DrCollectionTool.notEmpty(newResults)){
			T existingResult = DrCollectionTool.getLast(existingResults);
			T newResult = DrCollectionTool.getFirst(newResults);
			if(DrComparableTool.lt(newResult, existingResult)){
				String message = String.format("newResult %s is before previousResult %s", newResult, existingResult);
				throw new RuntimeException(message);
			}
		}
	}
}
