package com.hotpads.datarouter.client.imp.mysql.op.write;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.mysql.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.mysql.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.mysql.util.JdbcTool;
import com.hotpads.datarouter.client.imp.mysql.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcDeleteByIndexOp<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>>
		extends BaseJdbcOp<Long>{

	private final PhysicalNode<PK, D> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Config config;
	private final Collection<IK> entryKeys;

	public JdbcDeleteByIndexOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory,
			Collection<IK> entryKeys, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION,
				shouldAutoCommit(entryKeys));
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.entryKeys = entryKeys;
		this.config = config;
	}

	@Override
	public Long runOnce(){
		long numModified = 0;
		for(List<IK> batch : new BatchingIterable<>(entryKeys, JdbcReaderNode.DEFAULT_ITERATE_BATCH_SIZE)){
			String sql = SqlBuilder.deleteMulti(fieldCodecFactory, config, node.getTableName(), batch, node
					.getFieldInfo());
			numModified += JdbcTool.update(getConnection(node.getClientId().getName()), sql.toString());
		}

		return numModified;
	}


	private static <IK extends PrimaryKey<IK>> boolean shouldAutoCommit(Collection<IK> keys){
		return DrCollectionTool.size(keys) <= 1;
	}

}