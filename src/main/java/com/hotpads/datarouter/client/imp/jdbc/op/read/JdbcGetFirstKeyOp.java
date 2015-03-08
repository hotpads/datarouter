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
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class JdbcGetFirstKeyOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<PK>{
		
	private final JdbcReaderNode<PK,D,F> node;
	private final String opName;
	private final Config config;
	
	public JdbcGetFirstKeyOp(JdbcReaderNode<PK,D,F> node, String opName, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.config = config;
	}
	
	@Override
	public PK runOnce(){
		Config limitedConfig = Config.nullSafe(config);
		limitedConfig.setLimit(1);
		String sql = SqlBuilder.getAll(limitedConfig, node.getTableName(), node.getFieldInfo().getPrimaryKeyFields(),
				null, node.getFieldInfo().getPrimaryKeyFields());
		List<PK> result = JdbcTool.selectPrimaryKeys(getConnection(node.getClientName()), node.getFieldInfo(), sql);
		return DrCollectionTool.getFirst(result);
	}
	
}
