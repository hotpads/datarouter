package com.hotpads.datarouter.client.imp.redis.node;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabeanKey;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class RedisNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends RedisReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D>{

	private static final Logger logger = LoggerFactory.getLogger(RedisNode.class);

	private static final int MAX_REDIS_KEY_SIZE = 1024 * 64;

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

		String key = buildRedisKey(databean.getKey());

		if(key.length() > MAX_REDIS_KEY_SIZE){
			String jsonKey = JsonDatabeanTool.fieldsToJson(databean.getKey().getFields()).toString();
			logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key: " + jsonKey);
			return;
		}

		try{
			startTraceSpan("redis put");
			Long ttl = getTtlMs(config);

			String jsonBean = JsonDatabeanTool.databeanToJsonString(databean, fieldInfo.getSampleFielder());

			if(ttl == null){
				getClient().getJedisClient().set(key, jsonBean);
			} else{
				// XX - Only set they key if it already exists
				// PX - Milliseconds
				getClient().getJedisClient().set(key, jsonBean, "XX", "PX", ttl);
			}
		} finally {
			finishTraceSpan();
		}
	}

	@Override
	public void putMulti(final Collection<D> databeans, final Config config) {
		if(DrCollectionTool.isEmpty(databeans)){
			return;
		}
		try{
			startTraceSpan("redis put multi");
			for(D databean : databeans){
				if(databean == null){
					return;
				}

				String key = buildRedisKey(databean.getKey());

				if(key.length() > MAX_REDIS_KEY_SIZE){
					String jsonKey = JsonDatabeanTool.fieldsToJson(databean.getKey().getFields()).toString();
					logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key: " + jsonKey);
				}
				Long ttl = getTtlMs(config);

				String jsonBean = JsonDatabeanTool.databeanToJsonString(databean, fieldInfo.getSampleFielder());

				if(ttl == null){
					getClient().getJedisClient().set(key, jsonBean);
				} else{
					// XX - Only set they key if it already exists
					// PX - Milliseconds
					getClient().getJedisClient().set(key, jsonBean, "XX", "PX", ttl);
				}
			}
		} finally {
			finishTraceSpan();
		}
	}

	/** delete ***************************************************************/

	@Override
	public void deleteAll(final Config config){
		try{
			startTraceSpan("redis delete all");
			getClient().getJedisClient().flushAll();
		} finally{
			finishTraceSpan();
		}
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
		if(DrCollectionTool.isEmpty(keys)){
			return;
		}
		try{
			startTraceSpan("redis delete multi");
			getClient().getJedisClient().del((String[])buildRedisKeys(keys).toArray());
		} finally{
			finishTraceSpan();
		}
	}

	/** increment ************************************************************/

	public void increment(RedisDatabeanKey redisKey, int delta, Config config){
		if(redisKey == null){
			return;
		}
		try{
			startTraceSpan("redis increment");
			String key = buildRedisKey(redisKey);

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

	public Long incrementAndGetCount(RedisDatabeanKey redisKey, int delta, Config config){
		if(redisKey == null){
			return null;
		}
		try{
			startTraceSpan("redis increment and get count");
			String key = buildRedisKey(redisKey);

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