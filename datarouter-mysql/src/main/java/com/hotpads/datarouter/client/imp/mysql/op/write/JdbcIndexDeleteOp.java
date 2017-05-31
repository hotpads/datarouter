package com.hotpads.datarouter.client.imp.mysql.op.write;

import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.mysql.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.mysql.util.JdbcTool;
import com.hotpads.datarouter.client.imp.mysql.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;

public class JdbcIndexDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseJdbcOp<Long>{

	private final PhysicalNode<PK,D> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Lookup<PK> lookup;
	private final Config config;

	public JdbcIndexDeleteOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory, Lookup<PK> lookup,
			Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.lookup = lookup;
		this.config = config;
	}

	@Override
	public Long runOnce(){
		String sql = SqlBuilder.deleteMulti(fieldCodecFactory, config, node.getTableName(), DrListTool.wrap(lookup),
				node.getFieldInfo());
		long numModified = JdbcTool.update(getConnection(node.getClientId().getName()), sql.toString());
		return numModified;
	}

}
