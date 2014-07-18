package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import com.hotpads.util.core.profile.PhaseTimer;

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
	
	private static PhaseTimer timer = new PhaseTimer();
	private static int c = 0;
	
	@Override
	public List<D> runOnce(){
		timer.sum("potato");
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		timer.sum("DRCounters");
		int batchSize = JdbcNode.DEFAULT_ITERATE_BATCH_SIZE;
		if(config!=null && config.getIterateBatchSize()!=null){
			batchSize = config.getIterateBatchSize();
		}
		List<? extends Key<PK>> sortedKeys = ListTool.createArrayList(keys);
		timer.sum("config");
		Collections.sort(sortedKeys);//should prob remove
		timer.sum("sort");
		int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
		timer.sum("numBatches");
		List<D> result = ListTool.createArrayList(keys.size());
		timer.sum("createArrayList");
		Connection connection = getConnection(node.getClientName());
		timer.sum("connection");
		for(int batchNum=0; batchNum < numBatches; ++batchNum){
			List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
			timer.sum("getBatch");
			String sql = SqlBuilder.getMulti(config, node.getTableName(), node.getFieldInfo().getFields(), keyBatch);
			timer.sum("sql");
			List<D> batch = JdbcTool.selectDatabeans(connection, node.getFieldInfo(), sql);
			timer.sum("selectDatabeans");
			DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
			DRCounters.incSuffixClientNode(node.getClient().getType(), opName+" rows", node.getClientName(), node.getName(), 
					CollectionTool.size(batch));//count the number of hits (arbitrary decision)
			timer.sum("counters");
			if(CollectionTool.notEmpty(batch)){
				Collections.sort(batch);//should prob remove
				result.addAll(batch);
			}
			timer.sum("sort and addAll");
		}
		TraceContext.appendToSpanInfo("[got "+CollectionTool.size(result)+"/"+CollectionTool.size(keys)+"]");
		if(c++%500 == 0){
			//System.out.println(timer);
		}
		return result;
	}
	
}
