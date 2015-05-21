package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class JdbcGetFirstOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<D>{
		
	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory jdbcFieldCodecFactory;
	private final Config config;
	
	public JdbcGetFirstOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory jdbcFieldCodecFactory, Config pConfig) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.jdbcFieldCodecFactory = jdbcFieldCodecFactory;
		this.node = node;
		this.config = Config.nullSafe(pConfig);
	}
	
	@Override
	public D runOnce(){
		config.setLimit(1);
		String sql = SqlBuilder.getAll(config, node.getTableName(), node.getFieldInfo().getFields(), null, 
				node.getFieldInfo().getPrimaryKeyFields());
		List<D> result = JdbcTool.selectDatabeans(jdbcFieldCodecFactory, getConnection(node.getClientName()), node
				.getFieldInfo(), sql);
		return DrCollectionTool.getFirst(result);
	}
	
}
