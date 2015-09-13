package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;

public class JdbcGetPrimaryKeyRangeOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<PK>>{
		
	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Range<PK> range;
	private final Config config;
	
	public JdbcGetPrimaryKeyRangeOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory,
			Range<PK> range, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.range = range;
		this.config = config;
	}
	
	@Override
	public List<PK> runOnce(){
		Client client = node.getClient();
		String opName = SortedStorageReader.OP_getKeysInRange;
		DRCounters.incClientNodeCustom(client.getType(), opName, client.getName(), node.getName());
		
		List<Field<?>> fieldsToSelect = node.getFieldInfo().getPrimaryKeyFields();
		String sql = SqlBuilder.getInRange(fieldCodecFactory, config, node.getTableName(), fieldsToSelect, range, 
				node.getFieldInfo().getPrimaryKeyFields());
		List<PK> result = JdbcTool.selectPrimaryKeys(fieldCodecFactory, getConnection(node.getClientId().getName()),
				node.getFieldInfo(), sql);
		
		DRCounters.incClientNodeCustom(client.getType(), opName + " rows", client.getName(), node.getName(), 
				DrCollectionTool.size(result));
		return result;
	}
	
}
