package com.hotpads.datarouter.client.imp.jdbc.op.write;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class JdbcDeleteAllOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseJdbcOp<Long>{

	private final PhysicalNode<PK,D> node;
	private final Config config;

	public JdbcDeleteAllOp(PhysicalNode<PK,D> node, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.config = config;
	}

	@Override
	public Long runOnce(){
		String sql = SqlBuilder.deleteAll(config, node.getTableName());
		long numModified = JdbcTool.update(getConnection(node.getClientId().getName()), sql.toString());
		return numModified;
	}


}
