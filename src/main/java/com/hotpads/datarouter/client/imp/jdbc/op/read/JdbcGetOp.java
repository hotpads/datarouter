package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class JdbcGetOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<D>>{
		
	private com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode<PK,D,F> node;
	private String opName;
	private Collection<PK> keys;
	private Config config;
	
	public JdbcGetOp(JdbcReaderNode<PK,D,F> node, String opName, Collection<PK> keys, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.keys = keys;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.jdbc;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			int batchSize = JdbcNode.DEFAULT_ITERATE_BATCH_SIZE;
			if(config!=null && config.getIterateBatchSize()!=null){
				batchSize = config.getIterateBatchSize();
			}
			List<? extends Key<PK>> sortedKeys = ListTool.createArrayList(keys);
			Collections.sort(sortedKeys);
			int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
			List<D> all = ListTool.createArrayList(keys.size());
			for(int batchNum=0; batchNum < numBatches; ++batchNum){
				List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
				List<D> batch;
				String sql = SqlBuilder.getMulti(config, node.getTableName(), node.getFieldInfo().getFields(), keyBatch);
				batch = JdbcTool.selectDatabeans(getConnection(node.getClientName()), node.getFieldInfo(), sql);
				DRCounters.incSuffixClientNode(ClientType.jdbc, opName, node.getClientName(), node.getName());
				DRCounters.incSuffixClientNode(ClientType.jdbc, opName+" rows", node.getClientName(), node.getName(), 
						CollectionTool.size(keys));
				Collections.sort(batch);//can sort here because batches were already sorted
				ListTool.nullSafeArrayAddAll(all, batch);
			}
			TraceContext.appendToSpanInfo("[got "+CollectionTool.size(all)+"/"+CollectionTool.size(keys)+"]");
			return all;
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
