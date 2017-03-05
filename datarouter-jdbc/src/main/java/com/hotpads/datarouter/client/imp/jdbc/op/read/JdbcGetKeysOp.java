package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcGetKeysOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseJdbcOp<List<PK>>{

	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final String opName;
	private final Collection<PK> keys;
	private final Config config;

	public JdbcGetKeysOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory, String opName,
			Collection<PK> keys, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.opName = opName;
		this.keys = keys;
		this.config = config;
	}

	@Override
	public List<PK> runOnce(){
		int batchSize = JdbcReaderNode.DEFAULT_ITERATE_BATCH_SIZE;
		if(config != null && config.getIterateBatchSize() != null){
			batchSize = config.getIterateBatchSize();
		}
		List<? extends Key<PK>> sortedKeys = new ArrayList<>(keys);
		Collections.sort(sortedKeys);
		List<PK> result = new ArrayList<>(keys.size());
		Connection connection = getConnection(node.getClientId().getName());
		for(List<? extends Key<PK>> keyBatch : new BatchingIterable<>(sortedKeys, batchSize)){
			String sql = SqlBuilder.getMulti(fieldCodecFactory, config, node.getTableName(), node.getFieldInfo()
					.getPrimaryKeyFields(), keyBatch, node.getFieldInfo());
			List<PK> batch = JdbcTool.selectPrimaryKeys(fieldCodecFactory, connection, node.getFieldInfo(), sql);
			DRCounters.incClientNodeCustom(node.getClient().getType(), opName + " selects", node.getClientId()
					.getName(), node.getName());
			if(DrCollectionTool.notEmpty(batch)){
				Collections.sort(batch);//should prob remove
				result.addAll(batch);
			}
		}
		TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "[got " + DrCollectionTool.size(result) + "/"
				+ DrCollectionTool.size(keys) + "]");
		return result;
	}
}
