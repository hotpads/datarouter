package com.hotpads.datarouter.client.imp.redis.node;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.profile.tally.TallyKey;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;

import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class RedisNode<PK extends PrimaryKey<PK>, D extends Databean<PK,D>, F extends DatabeanFielder<PK,D>>
extends RedisReaderNode<PK,D,F> implements PhysicalMapStorageNode<PK,D>{

	private static final Logger logger = LoggerFactory.getLogger(RedisNode.class);

	private static final Boolean DEFAULT_IGNORE_EXCEPTION = true;

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

	/** MapStorageWriter methods *********************************************/


	/** put *********************************************/

	@Override
	public void put(final D databean, final Config config) {
		if(databean == null){
			return;
		}
		putMulti(DrListTool.wrap(databean), config);
	}

	@Override
	public void putMulti(final Collection<D> databeans, final Config paramConfig) {
		if(DrCollectionTool.isEmpty(databeans)){
			return;
		}
		startTraceSpan(MapStorageWriter.OP_putMulti);
		for(D databean : databeans){
			if(! fieldInfo.getFieldAware()){
				throw new IllegalArgumentException("databeans must be field aware");
			}
			String key = buildRedisKey(databean.getKey());

			if(key.length() > MAX_REDIS_KEY_SIZE){
				String json = JsonDatabeanTool.fieldsToJson(databean.getKey().getFields()).toString();
				logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key:" + json);
				return;
			}
			// TODO clean up
			Integer expiration = getExpirationSeconds(paramConfig);
			if(expiration == 0){
				getClient().getJedisClient().set(key, JsonDatabeanTool.databeanToJsonString(databean, null));
			} else {
				getClient().getJedisClient().set(key, JsonDatabeanTool.databeanToJsonString(databean, null),
						"NX", "EX", expiration);
			}
		}
		TracerTool.appendToSpanInfo(TracerThreadLocal.get(), DrCollectionTool.size(databeans) + "");
		finishTraceSpan();
	}

	/** delete *********************************************/


	// TODO redis supports this and can be added
	@Override
	public void deleteAll(final Config paramConfig){
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(PK key, Config paramConfig){
		if(key == null){
			return;
		}
		try{
			startTraceSpan(MapStorageWriter.OP_delete);
			getClient().getJedisClient().del(buildRedisKey(key));
		}catch(Exception exception){
			if(paramConfig.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("redis error on " + key, exception);
			}else{
				throw exception;
			}
		}finally{
			finishTraceSpan();
		}
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config paramConfig){
		for(PK pk : DrIterableTool.nullSafe(keys)){
			delete(pk, paramConfig);
		}
	}

	/** increment *********************************************/


	public void increment(TallyKey tallyKey, int delta, Config paramConfig){
		if(tallyKey == null){
			return;
		}
		try{
			TracerTool.startSpan(TracerThreadLocal.get(), "redis increment");
			String key = buildRedisKey(tallyKey);

			if(paramConfig == null){
				getClient().getJedisClient().incrBy(key, delta);
				return;
			}

			Transaction transaction = getClient().getJedisClient().multi();
			transaction.incrBy(key, delta);
			transaction.expire(key, getExpirationSeconds(paramConfig));
			transaction.exec();
			return;

		}catch(Exception exception){
			if(paramConfig.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("redis error on " + tallyKey, exception);
			}else{
				throw exception;
			}
		} finally {
			finishTraceSpan();
		}
	}

	public Long incrementAndGetCount(TallyKey tallyKey, int delta, Config paramConfig){
		if(tallyKey == null){
			return null;
		}
		try{
			TracerTool.startSpan(TracerThreadLocal.get(), "redis increment and get count");
			String key = buildRedisKey(tallyKey);

			if(paramConfig == null){
				return getClient().getJedisClient().incrBy(key, delta).longValue();
			}

			Transaction transaction = getClient().getJedisClient().multi();
			Response<Long> response = transaction.incrBy(key, delta);
			transaction.expire(key, getExpirationSeconds(paramConfig));
			transaction.exec();
			return response.get();

		}catch(Exception exception){
			if(paramConfig.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("redis error on " + tallyKey, exception);
				return null;
			}
			throw exception;
		} finally {
			finishTraceSpan();
		}
	}

	/** private methods ******************************************************/

	private static int getExpirationSeconds(Config config){
		if(config == null){
			return 0; // Infinite time
		}
		Long timeoutLong = config.getTtlMs() == null
				? Long.MAX_VALUE
				: config.getTtlMs() / 1000;
		Integer expiration = timeoutLong > new Long(Integer.MAX_VALUE)
				? Integer.MAX_VALUE
				: timeoutLong.intValue();
		return expiration;
	}
}