package com.hotpads.datarouter.client.imp.jdbc.op.write;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class JdbcDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends BaseJdbcOp<Long>{
		
	private final PhysicalNode<PK,D> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Collection<PK> keys;
	private final Config config;
	
	public JdbcDeleteOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory, Collection<PK> keys, 
			Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, shouldAutoCommit(keys));
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.keys = keys;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		String sql = SqlBuilder.deleteMulti(fieldCodecFactory, config, node.getTableName(), keys);
		long numModified = JdbcTool.update(getConnection(node.getClientId().getName()), sql.toString());
		return numModified;
	}
	
	
	private static boolean shouldAutoCommit(Collection<?> keys){
		return DrCollectionTool.size(keys) <= 1;
	}
}
