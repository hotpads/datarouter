package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcGetByIndexOp<PK extends PrimaryKey<PK>,
							D extends Databean<PK,D>,
							IK extends PrimaryKey<IK>>
		extends BaseJdbcOp<List<D>>{

	private final PhysicalNode<PK, D> node;
	private final Collection<IK> entryKeys;
	private final boolean wildcardLastField;
	private final Config config;

	public JdbcGetByIndexOp(PhysicalNode<PK, D> node, Collection<IK> entryKeys, boolean wildcardLastField, Config config){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.entryKeys = entryKeys;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		List<D> result = new LinkedList<>();
		for(List<IK> batch : new BatchingIterable<>(entryKeys, JdbcNode.DEFAULT_ITERATE_BATCH_SIZE)){
			String sql = SqlBuilder.getWithPrefixes(config, node.getTableName(), node.getFieldInfo().getFields(), batch, 
					wildcardLastField, node.getFieldInfo().getPrimaryKeyFields());
			result.addAll(JdbcTool.selectDatabeans(getConnection(node.getClientName()), node.getFieldInfo(), sql));
		}
		return result;
	}

}
