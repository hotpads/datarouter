package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;

public class JdbcGetRangeOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<D>>{
		
	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Range<PK> range;
	private final Config config;
	
	public JdbcGetRangeOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory, 
			Range<PK> range, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.range = range;
		this.config = config;
	}
	
	@Override
	public List<D> runOnce(){
		Client client = node.getClient();
		String opName = SortedStorageReader.OP_getRange;
		DRCounters.incClientNodeCustom(client.getType(), opName, client.getName(), node.getName());
		
		List<Field<?>> fieldsToSelect = node.getFieldInfo().getFields();
		String sql = SqlBuilder.getInRange(fieldCodecFactory, config, node.getTableName(), fieldsToSelect, range, 
				node.getFieldInfo().getPrimaryKeyFields());
		List<D> result = JdbcTool.selectDatabeans(fieldCodecFactory, getConnection(node.getClientName()), node
				.getFieldInfo(), sql);
		
		DRCounters.incClientNodeCustom(client.getType(), opName + " rows", client.getName(), node.getName(), 
				DrCollectionTool.size(result));
		return result;
	}
	
}