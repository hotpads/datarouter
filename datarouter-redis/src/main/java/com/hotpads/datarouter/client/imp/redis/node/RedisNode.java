package com.hotpads.datarouter.client.imp.redis.node;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.redis.test.databean.RedisTestDatabeanKey;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;

public class RedisNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends RedisReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D>{

	private static final Logger logger = LoggerFactory.getLogger(RedisNode.class);

	private static final int MEGABYTE = 1024 * 1024;
	private static final int MAX_REDIS_KEY_SIZE = MEGABYTE * 512;

	/** constructor **********************************************************/

	public RedisNode(NodeParams<PK,D,F> params){
		super(params);
	}

	@Override
	public Node<PK,D> getMaster() {
		return this;
	}

	/** put ******************************************************************/

	@Override
	public void put(final D databean, final Config config) {
		if(databean == null){
			return;
		}

		if(! fieldInfo.getFieldAware()){
			throw new IllegalArgumentException("databeans must be field aware");
		}
		String key = buildRedisKey(databean.getKey());

		if(key.length() > MAX_REDIS_KEY_SIZE){
			String jsonKey = JsonDatabeanTool.fieldsToJson(databean.getKey().getFields()).toString();
			logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key: " + jsonKey);
			return;
		}

		Long ttl = getTtlMs(config);

		String jsonBean;
		try{
			startTraceSpan("redis put");
			jsonBean = JsonDatabeanTool.databeanToJsonString(databean, fieldInfo.getSampleFielder());
		} finally{
			finishTraceSpan();
		}

		if(ttl == null){
			getClient().getJedisClient().set(key, jsonBean);
		} else{
			getClient().getJedisClient().set(key, jsonBean, "XX", "PX", ttl);
		}
	}

	@Override
	public void putMulti(final Collection<D> databeans, final Config config) {
		if(DrCollectionTool.isEmpty(databeans)){
			return;
		}
		for(D databean : databeans){
			put(databean, config);
		}
	}

	/** delete ***************************************************************/

	// TODO redis supports this and can be added
	@Override
	public void deleteAll(final Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(PK key, Config config){
		if(key == null){
			return;
		}
		try{
			startTraceSpan("redis delete");
			getClient().getJedisClient().del(buildRedisKey(key));
		} finally{
			finishTraceSpan();
		}
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		for(PK pk : DrIterableTool.nullSafe(keys)){
			delete(pk, config);
		}
	}

	/** increment ************************************************************/

	public void increment(RedisTestDatabeanKey tallyKey, int delta, Config config){
		if(tallyKey == null){
			return;
		}
		try{
			startTraceSpan("redis increment");
			String key = buildRedisKey(tallyKey);

			Long ttl = getTtlMs(config);
			if(ttl == null){
				getClient().getJedisClient().incrBy(key, delta);
				return;
			}

			getClient().getJedisClient().incrBy(key, delta);
			getClient().getJedisClient().pexpire(key, ttl);
			return;
		} finally{
			finishTraceSpan();
		}
	}

	public Long incrementAndGetCount(RedisTestDatabeanKey tallyKey, int delta, Config config){
		if(tallyKey == null){
			return null;
		}
		try{
			startTraceSpan("redis increment and get count");
			String key = buildRedisKey(tallyKey);

			Long expiration = getTtlMs(config);
			if(expiration == null){
				return getClient().getJedisClient().incrBy(key, delta).longValue();
			}

			Long response = getClient().getJedisClient().incrBy(key, delta);
			getClient().getJedisClient().pexpire(key, expiration);
			return response.longValue();
		} finally{
			finishTraceSpan();
		}
	}

	/** private **************************************************************/

	private Long getTtlMs(Config config){
		if(config == null){
			return null;
		}
		Long ttl = config.getTtlMs() == null
				? Long.MAX_VALUE
				: config.getTtlMs();
		return ttl;
	}
}