package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcLookupOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseJdbcOp<List<D>>{
		
	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Collection<? extends Lookup<PK>> lookups;
	private final boolean wildcardLastField;
	private final Config config;
	
	public JdbcLookupOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory,
			Collection<? extends Lookup<PK>> lookups, boolean wildcardLastField, Config config){
		super(node.getDatarouterContext(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.lookups = lookups;
		this.wildcardLastField = wildcardLastField;
		this.config = Config.nullSafe(config);
	}
	
	@Override
	public List<D> runOnce(){
		if(DrCollectionTool.isEmpty(lookups)){
			return new LinkedList<>();
		}
		Integer batchSize = config.getLimit();
		int configuredBatchSize = config.getIterateBatchSizeOverrideNull(JdbcNode.DEFAULT_ITERATE_BATCH_SIZE);
		if (batchSize == null || batchSize > configuredBatchSize){
			batchSize = configuredBatchSize;
		}
		//TODO undefined behavior on trailing nulls
		List<D> result = new ArrayList<>();
		for (List<? extends Lookup<PK>> batch : new BatchingIterable<>(lookups, batchSize)){
			String sql = SqlBuilder.getWithPrefixes(fieldCodecFactory, config, node.getTableName(), node.getFieldInfo()
					.getFields(), batch, wildcardLastField, node.getFieldInfo().getPrimaryKeyFields());
			result.addAll(JdbcTool.selectDatabeans(fieldCodecFactory, getConnection(node.getClientName()), node
					.getFieldInfo(), sql));
			if(config.getLimit() != null && result.size() >= config.getLimit()){
				break;
			}
		}
		if(config.getLimit() != null && result.size() > config.getLimit()){
			return new ArrayList<>(result.subList(0, config.getLimit()));
		}
		return result;
	}
	
}
