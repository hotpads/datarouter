package com.hotpads.datarouter.client.imp.hbase.batching.entity;

import java.util.List;
import java.util.NavigableSet;

import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.compare.FieldSetRangeFilter;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;


public class HBaseEntityPrimaryKeyBatchLoader<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHBaseEntityBatchLoader<EK,E,PK,D,F,PK>{
	private static Logger logger = LoggerFactory.getLogger(HBaseEntityPrimaryKeyBatchLoader.class);
	
	public HBaseEntityPrimaryKeyBatchLoader(final HBaseSubEntityReaderNode<EK,E,PK,D,F> node, 
			final int partition, final byte[] partitionBytes, final Range<PK> range, final Config pConfig, 
			Long batchChainCounter){
		super(node, partition, partitionBytes, range, pConfig, batchChainCounter);
	}
	
	@Override
	protected boolean isKeysOnly(){
		return true;
	}
	
	@Override
	protected List<PK> parseHBaseResult(Result result){
		//the first and last entity may include results outside the range
		NavigableSet<PK> unfilteredResults = node.getResultParser().getPrimaryKeysWithMatchingQualifierPrefix(result);
		List<PK> filteredResults = FieldSetRangeFilter.filter(unfilteredResults, range);
		return filteredResults;
	}
	
	@Override
	protected PK getLastPrimaryKeyFromBatch(){
		return getLast();
	}

	@Override
	public BatchLoader<PK> getNextLoader(){
		Range<PK> nextRange = getNextRange();
		return new HBaseEntityPrimaryKeyBatchLoader<EK,E,PK,D,F>(node, partition, partitionBytes, nextRange, config,
				batchChainCounter + 1);			
	}
}