package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class JdbcLookupUniqueOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<D>>{
		
	private JdbcReaderNode<PK,D,F> node;
	private String opName;
	private Collection<? extends UniqueKey<PK>> uniqueKeys;
	private Config config;
	
	public JdbcLookupUniqueOp(JdbcReaderNode<PK,D,F> node, String opName, 
			Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.uniqueKeys = uniqueKeys;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		if(CollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			List<? extends UniqueKey<PK>> sortedKeys = ListTool.createArrayList(uniqueKeys);
			Collections.sort(sortedKeys);
			String sql = SqlBuilder.getMulti(config, node.getTableName(), node.getFieldInfo().getFields(), 
					uniqueKeys);
			List<D> result = JdbcTool.selectDatabeans(getConnection(node.getClientName()), node.getFieldInfo(), sql);
			return result;
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
