package com.hotpads.datarouter.client.imp.jdbc.op.write;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;

public class JdbcDeleteAllOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<Long>{
		
	private PhysicalNode<PK,D> node;
	private String opName;
	private Config config;
	
	public JdbcDeleteAllOp(PhysicalNode<PK,D> node, String opName, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		DRCounters.incClientNodeCustom(node.getClient().getType(), opName, node.getClientName(), node.getName());
		String sql = SqlBuilder.deleteAll(config, node.getTableName());
		long numModified = JdbcTool.update(getConnection(node.getClientName()), sql.toString());
		return numModified;
	}
	

}
