package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.java.ReflectionTool;

public class JdbcIndexScanOp
<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,L extends Lookup<PK>>
extends BaseJdbcOp<List<D>>{

	private Range<L> start;
	private JdbcReaderNode<PK, D, F> node;
	private L index;
	private Config config;
	private boolean retreiveAllFields;
	
	public JdbcIndexScanOp(JdbcReaderNode<PK, D, F> node,
			Range<L> start, Class<L> indexClass, Config config, boolean retreiveAllFields){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.start = start;
		this.node = node;
		this.config = config;
		this.index = ReflectionTool.create(indexClass);
		this.retreiveAllFields = retreiveAllFields;
	}
	
	@Override
	public List<D> runOnce(){
		List<Field<?>> selectableFields;
		if(retreiveAllFields){
			Set<Field<?>> selectableFieldSet = SetTool.create(node.getFieldInfo().getFields());
			selectableFieldSet.removeAll(node.getFieldInfo().getNonKeyFields());
			selectableFieldSet.addAll(index.getFields());
			selectableFields = ListTool.createArrayList(selectableFieldSet);
		}else{
			selectableFields = node.getFields();
		}
		String sql = SqlBuilder.getInRange(
				config,
				node.getTableName(),
				selectableFields,
				start.getStart(),
				start.getStartInclusive(),
				start.getEnd(),
				start.getEndInclusive(),
				index.getFields());
		Connection connection = getConnection(node.getClientName());
		List<D> result = JdbcTool.selectDatabeansAndAllowNulls(connection, selectableFields, node.getDatabeanType(), sql);
		return result;
	}

}
