package com.hotpads.datarouter.client.imp.jdbc.op.read.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcGetByIndexOp<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>>
extends BaseJdbcOp<List<D>>{

	private final PhysicalNode<PK, D> node;
	private final Collection<IK> entryKeys;
	private final boolean wildcardLastField;
	private final Config config;
	private final String opName;

	public JdbcGetByIndexOp(PhysicalNode<PK, D> node, Collection<IK> entryKeys, boolean wildcardLastField,
			String opName, Config config){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.entryKeys = entryKeys;
		this.wildcardLastField = wildcardLastField;
		this.opName = opName;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		List<D> result = new ArrayList<>();
		for(List<IK> batch : new BatchingIterable<>(entryKeys, JdbcNode.DEFAULT_ITERATE_BATCH_SIZE)){
			String sql = SqlBuilder.getWithPrefixes(config, node.getTableName(), node.getFieldInfo().getFields(), batch, 
					wildcardLastField, null);
			result.addAll(JdbcTool.selectDatabeans(getConnection(node.getClientName()), node.getFieldInfo(), sql));
		}
		return result;
	}

}
