package com.hotpads.datarouter.client.imp.hbase.batching.entity;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseEntityResultParser;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;

//TODO merge this with PrimaryKeyBatchLoader.  slightly more complicated than first glance with generics
public class HBaseEntityDatabeanBatchLoader<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHBaseEntityBatchLoader<EK,PK,D,F,D>{
	private static Logger logger = Logger.getLogger(HBaseEntityDatabeanBatchLoader.class);
		
	public HBaseEntityDatabeanBatchLoader(final HBaseEntityReaderNode<EK,PK,D,F> node,
			final Range<PK> range, final Config pConfig, Long batchChainCounter){
		super(node, range, pConfig, batchChainCounter);
	}
	
	@Override
	protected boolean isKeysOnly(){
		return false;
	}
	
	@Override
	protected List<D> parseHBaseResult(Result result){
		return new HBaseEntityResultParser<EK,PK,D,F>(node.getFieldInfo()).getDatabeansWithMatchingQualifierPrefix(result);
	}
	
	@Override
	protected PK getLastPrimaryKeyFromBatch(){
		return getLast()==null ? null : getLast().getKey();
	}

	@Override
	public BatchLoader<D> getNextLoader(){
		Range<PK> nextRange = getNextRange();
		return new HBaseEntityDatabeanBatchLoader<EK,PK,D,F>(node, nextRange, config, batchChainCounter + 1);					
	}
}