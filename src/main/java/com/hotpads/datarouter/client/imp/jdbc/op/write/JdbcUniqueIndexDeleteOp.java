package com.hotpads.datarouter.client.imp.jdbc.op.write;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class JdbcUniqueIndexDeleteOp<PK extends PrimaryKey<PK>, D extends Databean<PK, D>> extends BaseJdbcOp<Long>{
		
	private PhysicalNode<PK,D> node;
	private String opName;
	private Collection<? extends UniqueKey<PK>> uniqueKeys;
	private Config config;
	
	public JdbcUniqueIndexDeleteOp(PhysicalNode<PK,D> node, String opName, 
			Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, shouldAutoCommit(uniqueKeys));
		this.node = node;
		this.opName = opName;
		this.uniqueKeys = uniqueKeys;
		this.config = config;
	}
	
	@Override
	public Long runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		String sql = SqlBuilder.deleteMulti(config, node.getTableName(), uniqueKeys);
		long numModified = JdbcTool.update(getConnection(node.getClientName()), sql.toString());
		return numModified;
	}
	
	
	private static boolean shouldAutoCommit(Collection<?> keys){
		return DrCollectionTool.size(keys) <= 1;
	}
}
