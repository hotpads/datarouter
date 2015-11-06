package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JdbcGetWithPrefixesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseJdbcOp<List<D>>{

	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Collection<PK> prefixes;
	private final boolean wildcardLastField;
	private final Config config;
	private int count;
	private Connection connection;
	private List<D> result = new LinkedList<>();
	private static final int MAX_PREFIXES = 400;

	public JdbcGetWithPrefixesOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory,
			Collection<PK> prefixes, boolean wildcardLastField, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.prefixes = prefixes;
		this.wildcardLastField = wildcardLastField;
		this.config = config;
		this.count = DrCollectionTool.sizeNullSafe(prefixes);
	}

	@Override
	public List<D> runOnce(){
		if(count == 0) {
			return result;
		}
		connection = getConnection(node.getClientId().getName());
		if(count < MAX_PREFIXES) {
			return runBatch(prefixes);
		}
		for(List<PK> batch : new BatchingIterable<>(prefixes, MAX_PREFIXES)){
			result.addAll(runBatch(batch));
		}
		return result;
	}

	private List<D> runBatch(Collection<PK> batch) {
		String sql = SqlBuilder.getWithPrefixes(fieldCodecFactory, config, node.getTableName(), node.getFieldInfo()
				.getFields(), batch, wildcardLastField, node.getFieldInfo().getPrimaryKeyFields());
		return JdbcTool.selectDatabeans(fieldCodecFactory, connection, node.getFieldInfo(), sql);
	}
}
