package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;

public class JdbcGetFirstKeyOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<PK>{
		
	private JdbcReaderNode<PK,D,F> node;
	private String opName;
	private Config config;
	
	public JdbcGetFirstKeyOp(JdbcReaderNode<PK,D,F> node, String opName, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.config = config;
	}
	
	@Override
	public PK runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		Config nullSafeConfig = Config.nullSafe(config);
		nullSafeConfig.setLimit(1);
		String sql = SqlBuilder.getAll(config, node.getTableName(), node.getFieldInfo().getPrimaryKeyFields(), null, 
				node.getFieldInfo().getPrimaryKeyFields());
		List<PK> result = JdbcTool.selectPrimaryKeys(getConnection(node.getClientName()), node.getFieldInfo(), sql);
		return CollectionTool.getFirst(result);
	}
	
}
