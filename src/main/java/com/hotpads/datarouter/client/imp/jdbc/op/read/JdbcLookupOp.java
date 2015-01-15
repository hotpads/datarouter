package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcLookupOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<D>>{
		
	private JdbcReaderNode<PK,D,F> node;
	private String opName;
	private Collection<? extends Lookup<PK>> lookups;
	private boolean wildcardLastField;
	private Config config;
	
	public JdbcLookupOp(JdbcReaderNode<PK,D,F> node, String opName, 
			Collection<? extends Lookup<PK>> lookups, boolean wildcardLastField, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.lookups = lookups;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		if(CollectionTool.isEmpty(lookups)){ return new LinkedList<D>(); }
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		//TODO undefined behavior on trailing nulls
		List<D> result = ListTool.create();
		for(List<? extends Lookup<PK>> batch : new BatchingIterable<>(lookups, JdbcNode.DEFAULT_ITERATE_BATCH_SIZE)){
			String sql = SqlBuilder.getWithPrefixes(config, node.getTableName(), node.getFieldInfo().getFields(), batch, 
					wildcardLastField, node.getFieldInfo().getPrimaryKeyFields());
			result.addAll(JdbcTool.selectDatabeans(getConnection(node.getClientName()), node.getFieldInfo(), sql));
		}
		return result;
	}
	
}
