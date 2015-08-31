package com.hotpads.datarouter.client.imp.jdbc.op.read.index;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.BaseField.FieldColumnNameComparator;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.java.ReflectionTool;

public class JdbcIndexScanOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		PKLookup extends BaseLookup<PK>>
extends BaseJdbcOp<List<PKLookup>>{

	private final JdbcReaderNode<PK, D, F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Range<PKLookup> start;
	private final Class<PKLookup> indexClass;
	private final Config config;
	
	public JdbcIndexScanOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory, Range<PKLookup> start,
			Class<PKLookup> indexClass, Config config){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.start = start;
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.config = config;
		this.indexClass = indexClass;
	}
	
	@Override
	public List<PKLookup> runOnce(){
		PKLookup index = ReflectionTool.create(indexClass, indexClass.getCanonicalName() 
				+ " must have a no-arg constructor");
		
		Set<Field<?>> selectableFieldSet = new TreeSet<>(new FieldColumnNameComparator());
		selectableFieldSet.addAll(node.getFieldInfo().getPrefixedPrimaryKeyFields());
		selectableFieldSet.addAll(index.getFields());
		
		@SuppressWarnings("serial")
		FieldSet<?> fullStart = new BaseLookup<PK>(){

			@Override
			public List<Field<?>> getFields(){
				List<Field<?>> fields = new ArrayList<>();
				fields.addAll(start.getStart().getFields());
				fields.addAll(start.getStart().getPrimaryKey().getFields());
				return fields;
			}

		};
		
		List<Field<?>> selectableFields = DrListTool.createArrayList(selectableFieldSet);
		String sql = SqlBuilder.getInRange(fieldCodecFactory, config, node.getTableName(), selectableFields, fullStart,
				start.getStartInclusive(), start.getEnd(), start.getEndInclusive(), index.getFields());
		Connection connection = getConnection(node.getClientId().getName());
		List<PKLookup> result = JdbcTool.selectLookups(fieldCodecFactory, connection, selectableFields, indexClass,
				sql, node.getFieldInfo().getPrimaryKeyClass());
		return result;
	}

}
