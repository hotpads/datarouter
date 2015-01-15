package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.ListTool;

public class JdbcCountOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<Long>{
		
	private JdbcReaderNode<PK,D,F> node;
	private String opName;
	private Lookup<PK> lookup;
	private Config config;
	
	public JdbcCountOp(JdbcReaderNode<PK,D,F> node, String opName, Lookup<PK> lookup, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.lookup = lookup;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		Connection connection = getConnection(node.getClientName());
		String sql = SqlBuilder.getCount(config, node.getTableName(), node.getFieldInfo().getFields(), 
				ListTool.wrap(lookup));
		return JdbcTool.count(connection, sql);
	}
	
}
