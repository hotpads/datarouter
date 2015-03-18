package com.hotpads.datarouter.client.imp.jdbc.op.write;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;

public class JdbcPrefixDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<Long>{
		
	private final PhysicalNode<PK,D> node;
	private final PK prefix;
	private final boolean wildcardLastField;
	private final Config config;
	
	public JdbcPrefixDeleteOp(PhysicalNode<PK,D> node, PK prefix, boolean wildcardLastField,
			Config config){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.prefix = prefix;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		String sql = SqlBuilder.deleteWithPrefixes(config, node.getTableName(), DrListTool.wrap(prefix),
				wildcardLastField);
		long numModified = JdbcTool.update(getConnection(node.getClientName()), sql.toString());
		return numModified;
	}
	
}
