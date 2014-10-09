package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
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
		
	private BasePhysicalNode<PK,D,F> node;
	private String opName;
	private Collection<PK> keys;
	private Config config;
	
	public JdbcGetOp(BasePhysicalNode<PK,D,F> node, String opName, Collection<PK> keys, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.keys = keys;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		int batchSize = JdbcNode.DEFAULT_ITERATE_BATCH_SIZE;
		if(config!=null && config.getIterateBatchSize()!=null){
			batchSize = config.getIterateBatchSize();
		}
		List<? extends Key<PK>> sortedKeys = ListTool.createArrayList(keys);
		Collections.sort(sortedKeys);//should prob remove
		int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
		List<D> result = ListTool.createArrayList(keys.size());
		Connection connection = getConnection(node.getClientName());
		for(int batchNum=0; batchNum < numBatches; ++batchNum){
			List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
			String sql = SqlBuilder.getMulti(config, node.getTableName(), node.getFieldInfo().getFields(), keyBatch);
			List<D> batch = JdbcTool.selectDatabeans(connection, node.getFieldInfo(), sql);
			DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
			DRCounters.incSuffixClientNode(node.getClient().getType(), opName+" rows", node.getClientName(), node.getName(), 
					CollectionTool.size(batch));//count the number of hits (arbitrary decision)
			if(CollectionTool.notEmpty(batch)){
				Collections.sort(batch);//should prob remove
				result.addAll(batch);
			}
		}
		TraceContext.appendToSpanInfo("[got "+CollectionTool.size(result)+"/"+CollectionTool.size(keys)+"]");
		return result;
	}
	
}
