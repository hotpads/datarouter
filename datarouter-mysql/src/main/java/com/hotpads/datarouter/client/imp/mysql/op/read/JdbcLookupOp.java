package com.hotpads.datarouter.client.imp.mysql.op.read;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.mysql.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.mysql.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.mysql.util.JdbcTool;
import com.hotpads.datarouter.client.imp.mysql.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.profile.counter.Counters;
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
	private static final Logger logger = LoggerFactory.getLogger(JdbcLookupOp.class);

	private static final int LARGE_LOOKUP_ALERT_THRESHOLD = 1000;
	private static final String ALERT_PREFIX = "JDBC lookup exceeded alert threshold";

	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final Collection<? extends Lookup<PK>> lookups;
	private final boolean wildcardLastField;
	private final Config config;

	public JdbcLookupOp(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory,
			Collection<? extends Lookup<PK>> lookups, boolean wildcardLastField, Config config){
		super(node.getDatarouter(), node.getClientNames(), Config.DEFAULT_ISOLATION, true);
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
		int configuredBatchSize = config.getIterateBatchSize();
		if(batchSize == null || batchSize > configuredBatchSize){
			batchSize = configuredBatchSize;
		}
		//TODO undefined behavior on trailing nulls
		List<D> result = new ArrayList<>();
		for(List<? extends Lookup<PK>> batch : new BatchingIterable<>(lookups, batchSize)){
			//for performance reasons, pass null for orderBy and sort in java if desired
			String sql = SqlBuilder.getWithPrefixes(fieldCodecFactory, config, node.getTableName(), node.getFieldInfo()
					.getFields(), batch, wildcardLastField, null, node.getFieldInfo());
			result.addAll(JdbcTool.selectDatabeans(fieldCodecFactory, getConnection(node.getClientId().getName()), node
					.getFieldInfo(), sql));
			if(config.getLimit() != null && result.size() >= config.getLimit()){
				break;
			}
		}
		if(result.size() > LARGE_LOOKUP_ALERT_THRESHOLD){
			logger.warn(ALERT_PREFIX + " : " + result.size() + ">" + LARGE_LOOKUP_ALERT_THRESHOLD,
					new Exception());
			Counters.inc(ALERT_PREFIX);
			lookups.stream()
					.map(Object::getClass)
					.distinct()
					.map(Class::getSimpleName)
					.map((ALERT_PREFIX + " ")::concat)
					.forEach(Counters::inc);
		}
		if(config.getLimit() != null && result.size() > config.getLimit()){
			return new ArrayList<>(result.subList(0, config.getLimit()));
		}
		return result;
	}

}