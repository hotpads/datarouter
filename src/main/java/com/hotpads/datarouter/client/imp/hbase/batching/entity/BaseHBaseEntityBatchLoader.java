package com.hotpads.datarouter.client.imp.hbase.batching.entity;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchLoader;

public abstract class BaseHBaseEntityBatchLoader<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T> //T will be either PK or D, but not going to express that (or think about how to)
extends BaseBatchLoader<T>{
	private static Logger logger = Logger.getLogger(BaseHBaseEntityBatchLoader.class);

	private static final int DEFAULT_iterateBatchSize = 1000;
	
	protected final HBaseEntityReaderNode<EK,PK,D,F> node;
	protected final Range<PK> range;
	protected final Config config;
	protected final Integer iterateBatchSize;//break this out of config for safety
	protected Long batchChainCounter;
	
	public BaseHBaseEntityBatchLoader(final HBaseEntityReaderNode<EK,PK,D,F> node,
			final Range<PK> range, final Config pConfig, Long batchChainCounter){
		this.node = node;
		this.range = range;
		this.config = Config.nullSafe(pConfig);
		this.iterateBatchSize = config.getIterateBatchSizeOverrideNull(DEFAULT_iterateBatchSize);
		config.setIterateBatchSize(iterateBatchSize);
		this.batchChainCounter = batchChainCounter;
	}

	abstract boolean isKeysOnly();
	abstract List<T> parseHBaseResult(Result result);
	abstract PK getLastPrimaryKeyFromBatch();
	

	@Override
	public BaseHBaseEntityBatchLoader<EK,PK,D,F,T> call(){
		//do the RPC
		List<Result> hBaseRows = node.getResultsInSubRange(range, isKeysOnly(), config);
		
		List<T> outs = ListTool.createArrayListWithSize(hBaseRows);
		for(Result row : hBaseRows){
			if(row==null || row.isEmpty()){ continue; }
			List<T> results = parseHBaseResult(row);
			outs.addAll(CollectionTool.nullSafe(results));
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

	
}