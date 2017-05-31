package com.hotpads.datarouter.client.imp.mysql.op.write;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.mysql.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.mysql.util.JdbcTool;
import com.hotpads.datarouter.client.imp.mysql.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseJdbcOp<Long>{

	private final PhysicalNode<PK,D> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Collection<PK> keys;
	private final Config config;

	public JdbcDeleteOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory, Collection<PK> keys,
			Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, shouldAutoCommit(keys));
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.keys = keys;
		this.config = Config.nullSafe(config);
	}

	@Override
	public Long runOnce(){
		Connection connection = getConnection(node.getClientId().getName());
		long numModified = 0;
		for(List<PK> keyBatch : new BatchingIterable<>(keys, config.getIterateBatchSize())){
			String sql = SqlBuilder.deleteMulti(fieldCodecFactory, config, node.getTableName(), keyBatch, node
					.getFieldInfo());
			numModified += JdbcTool.update(connection, sql);
		}
		return numModified;
	}

	private static boolean shouldAutoCommit(Collection<?> keys){
		return DrCollectionTool.size(keys) <= 1;
	}
}
