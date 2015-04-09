package com.hotpads.datarouter.client.imp.hbase.batching;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;


public class HBasePrimaryKeyBatchLoader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHBaseBatchLoader<PK,D,F,PK>{
	private static Logger logger = LoggerFactory.getLogger(HBasePrimaryKeyBatchLoader.class);
	
	public HBasePrimaryKeyBatchLoader(final HBaseReaderNode<PK,D,F> node, final List<Field<?>> scatteringPrefix,
			final Range<PK> range, final Config pConfig, Long batchChainCounter){
		super(node, scatteringPrefix, range, pConfig, batchChainCounter);
	}
	
	@Override
	protected boolean isKeysOnly(){
		return true;
	}
	
	@Override
	protected PK parseHBaseResult(Result result){
		return HBaseResultTool.getPrimaryKey(result.getRow(), node.getFieldInfo());
	}
	
	@Override
	protected PK getLastPrimaryKeyFromBatch(){
		return getLast();
	}

	@Override
	public BatchLoader<PK> getNextLoader(){
		Range<PK> nextRange = getNextRange();
		return new HBasePrimaryKeyBatchLoader<PK,D,F>(node, scatteringPrefix, nextRange, config, batchChainCounter + 1);					
	}
}
