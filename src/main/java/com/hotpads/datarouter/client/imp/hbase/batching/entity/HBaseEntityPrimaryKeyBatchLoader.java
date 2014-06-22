package com.hotpads.datarouter.client.imp.hbase.batching.entity;

import java.util.List;
import java.util.NavigableSet;

import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseEntityResultParser;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;


public class HBaseEntityPrimaryKeyBatchLoader<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHBaseEntityBatchLoader<EK,PK,D,F,PK>{
	private static Logger logger = Logger.getLogger(HBaseEntityPrimaryKeyBatchLoader.class);
	
	public HBaseEntityPrimaryKeyBatchLoader(final HBaseEntityReaderNode<EK,PK,D,F> node, 
			final Range<PK> range, final Config pConfig, Long batchChainCounter){
		super(node, range, pConfig, batchChainCounter);
	}
	
	@Override
	protected boolean isKeysOnly(){
		return true;
	}
	
	@Override
	protected List<PK> parseHBaseResult(Result result){
		NavigableSet<PK> pks = new HBaseEntityResultParser<EK,PK,D,F>(node.getFieldInfo())
				.getPrimaryKeysWithMatchingQualifierPrefix(result);
		return ListTool.createArrayList(pks);
	}
	
	@Override
	protected PK getLastPrimaryKeyFromBatch(){
		return getLast();
	}

	@Override
	public BatchLoader<PK> getNextLoader(){
		Range<PK> nextRange = getNextRange();
		return new HBaseEntityPrimaryKeyBatchLoader<EK,PK,D,F>(node, nextRange, config, batchChainCounter + 1);					
	}
}