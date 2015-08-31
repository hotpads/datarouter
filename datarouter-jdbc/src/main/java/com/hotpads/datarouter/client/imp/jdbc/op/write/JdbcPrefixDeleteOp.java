package com.hotpads.datarouter.client.imp.jdbc.op.write;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;

public class JdbcPrefixDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends BaseJdbcOp<Long>{
		
	private final PhysicalNode<PK,D> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final PK prefix;
	private final boolean wildcardLastField;
	private final Config config;
	
	public JdbcPrefixDeleteOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory, PK prefix,
			boolean wildcardLastField, Config config){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.prefix = prefix;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		String sql = SqlBuilder.deleteWithPrefixes(fieldCodecFactory, config, node.getTableName(), DrListTool.wrap(
				prefix), wildcardLastField);
		long numModified = JdbcTool.update(getConnection(node.getClientId().getName()), sql.toString());
		return numModified;
	}
	
}
