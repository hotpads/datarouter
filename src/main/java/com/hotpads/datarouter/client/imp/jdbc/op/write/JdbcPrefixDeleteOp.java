package com.hotpads.datarouter.client.imp.jdbc.op.write;

import org.hibernate.Session;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.ListTool;

public class JdbcPrefixDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<Long>{
		
	private JdbcNode<PK,D,F> node;
	private String opName;
	private PK prefix;
	private boolean wildcardLastField;
	private Config config;
	
	public JdbcPrefixDeleteOp(JdbcNode<PK,D,F> node, String opName, PK prefix, boolean wildcardLastField,
			Config config){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.prefix = prefix;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		ClientType clientType = node.getFieldInfo().getFieldAware() ? ClientType.jdbc : ClientType.hibernate;
		DRCounters.incSuffixClientNode(clientType, opName, node.getClientName(), node.getName());
		try{
			TraceContext.startSpan(node.getName()+" "+opName);
			String sql = SqlBuilder.deleteWithPrefixes(config, node.getTableName(), ListTool.wrap(prefix),
					wildcardLastField);
			long numModified = JdbcTool.update(getConnection(node.getClientName()), sql.toString());
			return numModified;
		}finally{
			TraceContext.finishSpan();
		}
	}
	
}
