package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.collections.Range;

public class JdbcGetPrimaryKeyRangeOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<PK>>{
		
	private JdbcReaderNode<PK,D,F> node;
	private String opName;
	private Range<PK> range;
	private Config config;
	
	public JdbcGetPrimaryKeyRangeOp(JdbcReaderNode<PK,D,F> node, String opName, Range<PK> range, Config config) {
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.opName = opName;
		this.range = range;
		this.config = config;
	}
	
	@Override
	public List<PK> runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName, node.getClientName(), node.getName());
		List<Field<?>> fieldsToSelect = node.getFieldInfo().getPrimaryKeyFields();
		String sql = SqlBuilder.getInRange(config, node.getTableName(), fieldsToSelect, range, 
				node.getFieldInfo().getPrimaryKeyFields());
		List<PK> result = JdbcTool.selectPrimaryKeys(getConnection(node.getClientName()), node.getFieldInfo(), sql);
		DRCounters.incSuffixClientNode(node.getClient().getType(), opName+" rows", node.getClientName(), node.getName(), 
				CollectionTool.size(result));
		return result;
	}
	
}