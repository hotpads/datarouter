package com.hotpads.datarouter.client.imp.jdbc.op.read.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcGetByIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>>
extends BaseJdbcOp<List<D>>{

	private final PhysicalNode<PK, D> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Collection<IK> entryKeys;
	private final boolean wildcardLastField;
	private final Config config;

	public JdbcGetByIndexOp(PhysicalNode<PK,D> node, JdbcFieldCodecFactory fieldCodecFactory, Collection<IK> entryKeys,
			boolean wildcardLastField, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.entryKeys = entryKeys;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}

	@Override
	public List<D> runOnce(){
		List<D> result = new ArrayList<>();
		//TODO test if right info?
		for(List<IK> batch : new BatchingIterable<>(entryKeys, JdbcReaderNode.DEFAULT_ITERATE_BATCH_SIZE)){
			String sql = SqlBuilder.getWithPrefixes(fieldCodecFactory, config, node.getTableName(), node.getFieldInfo()
					.getFields(), batch, wildcardLastField, null, node.getFieldInfo());
			result.addAll(JdbcTool.selectDatabeans(fieldCodecFactory, getConnection(node.getClientId().getName()), node
					.getFieldInfo(), sql));
		}
		return result;
	}

}
