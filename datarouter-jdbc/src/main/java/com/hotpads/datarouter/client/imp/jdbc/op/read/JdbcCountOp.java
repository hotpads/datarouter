package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;

public class JdbcCountOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<Long>{
		
	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Lookup<PK> lookup;
	private final Config config;
	
	public JdbcCountOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory, Lookup<PK> lookup,
			Config config){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.lookup = lookup;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		Connection connection = getConnection(node.getClientId().getName());
		String sql = SqlBuilder.getCount(fieldCodecFactory, config, node.getTableName(), node.getFieldInfo()
				.getFields(), DrListTool.wrap(lookup));
		return JdbcTool.count(connection, sql);
	}
	
}
