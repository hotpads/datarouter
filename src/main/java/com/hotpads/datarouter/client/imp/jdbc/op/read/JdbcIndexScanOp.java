package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.BaseField.FieldColumnNameComparator;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;

public class JdbcIndexScanOp
<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,PKLookup extends Lookup<PK>>
extends BaseJdbcOp<List<PKLookup>>{

	private Range<PKLookup> start;
	private JdbcReaderNode<PK, D, F> node;
	private Class<PKLookup> indexClass;
	private Config config;
	private String traceName;
	
	public JdbcIndexScanOp(JdbcReaderNode<PK, D, F> node, Range<PKLookup> start, Class<PKLookup> indexClass, Config config,
			String traceName){
		super(node.getDataRouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.start = start;
		this.node = node;
		this.config = config;
		this.indexClass = indexClass;
		this.traceName = traceName;
	}
	
	@Override
	public List<PKLookup> runOnce(){
		DRCounters.incSuffixClientNode(node.getClient().getType(), traceName, node.getClientName(), node.getName());
		
		PKLookup index;
		try{
			index = indexClass.newInstance();
		}catch (InstantiationException | IllegalAccessException e){
			throw new RuntimeException(indexClass.getCanonicalName() + " must have a no-arg constructor", e);
		}
		
		Set<Field<?>> selectableFieldSet = new TreeSet<Field<?>>(new FieldColumnNameComparator());
		selectableFieldSet.addAll(node.getFieldInfo().getPrefixedPrimaryKeyFields());
		selectableFieldSet.addAll(index.getFields());
		
		List<Field<?>> selectableFields = ListTool.createArrayList(selectableFieldSet);
		String sql = SqlBuilder.getInRange(config, node.getTableName(), selectableFields, start.getStart(),
				start.getStartInclusive(), start.getEnd(), start.getEndInclusive(), index.getFields());
		Connection connection = getConnection(node.getClientName());
		List<PKLookup> result = JdbcTool.selectLookups(connection, selectableFields, indexClass, sql, node.getFieldInfo().getPrimaryKeyClass());
		return result;
	}

}
