package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class JdbcLookupUniqueOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<D>>{
		
	private final JdbcReaderNode<PK,D,F> node;
	private final String opName;
	private final Collection<? extends UniqueKey<PK>> uniqueKeys;
	private final Config config;
	
	public JdbcLookupUniqueOp(JdbcReaderNode<PK,D,F> node, String opName, 
			Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.uniqueKeys = uniqueKeys;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		if(DrCollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		List<? extends UniqueKey<PK>> sortedKeys = DrListTool.createArrayList(uniqueKeys);
		Collections.sort(sortedKeys);
		String sql = SqlBuilder.getMulti(config, node.getTableName(), node.getFieldInfo().getFields(), 
				uniqueKeys);
		List<D> result = JdbcTool.selectDatabeans(getConnection(node.getClientName()), node.getFieldInfo(), sql);
		return result;
	}
	
}
