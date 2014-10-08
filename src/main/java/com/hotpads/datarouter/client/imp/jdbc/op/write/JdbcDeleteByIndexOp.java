package com.hotpads.datarouter.client.imp.jdbc.op.write;

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
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcDeleteByIndexOp<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>>
		extends BaseJdbcOp<Long>{

	private final PhysicalNode<PK, D> node;
	private final Config config;
	private final Collection<IK> entryKeys;
	private final String opName;

	public JdbcDeleteByIndexOp(PhysicalNode<PK, D> node, Collection<IK> entryKeys, Config config, String opName){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, shouldAutoCommit(entryKeys));
		this.node = node;
		this.entryKeys = entryKeys;
		this.config = config;
		this.opName = opName;
	}
	
	@Override
	public Long runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		long numModified = 0;
		for(List<IK> batch : new BatchingIterable<IK>(entryKeys, JdbcNode.DEFAULT_ITERATE_BATCH_SIZE)){
			String sql = SqlBuilder.deleteMulti(config, node.getTableName(), batch);
			numModified += JdbcTool.update(getConnection(node.getClientName()), sql.toString());
		}
		
		return numModified;
	}
	
	
	private static <IK extends PrimaryKey<IK>> boolean shouldAutoCommit(Collection<IK> keys){
		return CollectionTool.size(keys) <= 1;
	}

}
