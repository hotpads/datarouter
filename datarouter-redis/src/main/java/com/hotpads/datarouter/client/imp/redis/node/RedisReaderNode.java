package com.hotpads.datarouter.client.imp.redis.node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.redis.client.DatarouterRedisKey;
import com.hotpads.datarouter.client.imp.redis.client.RedisClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.profile.tally.TallyKey;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;

public class RedisReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements RedisPhysicalNode<PK,D>, MapStorageReader<PK,D>{

	private static final Logger logger = LoggerFactory.getLogger(RedisReaderNode.class);

	protected final Integer databeanVersion;

	/*** constructors ****************************************************************/

	public RedisReaderNode(NodeParams<PK,D,F> params){
		super(params);
		this.databeanVersion = Preconditions.checkNotNull(params.getSchemaVersion());
	}


	/** plumbing methods **************************************************************/

	@Override
	public RedisClient getClient(){
		return (RedisClient)getRouter().getClient(getClientId().getName());
	}

	/** MapStorageReader methods *************************************************************/

	@Override
	public boolean exists(PK key, Config config) {
		return get(key, config) != null;
	}


	@Override
	public D get(final PK key, final Config paramConfig){
		if(key==null){
			return null;
		}
		startTraceSpan(MapStorageReader.OP_get);
		final Config config = Config.nullSafe(paramConfig);
		byte[] bytes = null;

		try {
			Future<Object> future = getClient().getJedisClient().asyncGet(buildMemcachedKey(key));
			bytes = (byte[])future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
		} catch(TimeoutException e) {
			TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "memcached timeout");
		} catch(InterruptedException | ExecutionException e) {
			logger.error("", e);
		}

		if(bytes == null){
			TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "miss");
			return null;
		}
		TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "hit");
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getFieldInfo().getDatabeanSupplier(),
					fieldInfo.getFieldByPrefixedName(), is, bytes.length);
			return databean;
		} catch (IOException e) {
			logger.error("", e);
			return null;
		}finally{
			finishTraceSpan();
		}
	}


	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config paramConfig){
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}
		startTraceSpan(MapStorageReader.OP_getMulti);
		final Config config = Config.nullSafe(paramConfig);
		List<D> databeans = DrListTool.createArrayListWithSize(keys);
		Map<String,Object> bytesByStringKey = null;


		try {
			//get results asynchronously.  default CacheTimeoutMS set in MapCachingStorage.CACHE_CONFIG
			Future<Map<String,Object>> future = getClient().getJedisClient()
					.asyncGetBulk(buildMemcachedKeys(keys));
			bytesByStringKey = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
		} catch(TimeoutException e) {
			TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "memcached timeout");
		} catch(ExecutionException | InterruptedException e){
			logger.error("", e);
		}

		try{
			if (bytesByStringKey == null){
				return null;
			}

			for(Map.Entry<String,Object> entry : bytesByStringKey.entrySet()){
				byte[] bytes = (byte[])entry.getValue();
				if(DrArrayTool.isEmpty(bytes)){
					return null;
				}
				ByteArrayInputStream is = new ByteArrayInputStream((byte[])entry.getValue());
				try {
					D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getFieldInfo().getDatabeanSupplier(),
							fieldInfo.getFieldByPrefixedName(), is, bytes.length);
					databeans.add(databean);
				} catch (IOException e) {
					logger.error("", e);
				}
			}
			TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "[got " + DrCollectionTool.size(databeans) + "/"
					+ DrCollectionTool.size(keys) + "]");
			return databeans;
		}finally{
			finishTraceSpan();
		}
	}


	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config paramConfig){
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}
		return DatabeanTool.getKeys(getMulti(keys, paramConfig));
	}


	public Long getTallyCount(TallyKey key, final Config paramConfig){
		if(key == null){
			return null;
		}
		Object tallyObject = null;
		try{
			tallyObject = getClient().getJedisClient().asyncGet(buildMemcachedKey(key)).get();
		}catch(Exception exception){
			if(paramConfig.ignoreExceptionOrUse(true)){
				logger.error("memcached error on " + key, exception);
			}else{
				throw new RuntimeException(exception);
			}
		}

		if(!(tallyObject instanceof String)){
			return null;
		}

		return Long.valueOf(((String)tallyObject).trim());
	}

	/** serialization ****************************************************/

	protected String buildMemcachedKey(PrimaryKey<?> pk){
		return new DatarouterRedisKey(getName(), databeanVersion, pk).getVersionedKeyString();
	}

	protected List<String> buildMemcachedKeys(Collection<? extends PrimaryKey<?>> pks){
		return DatarouterRedisKey.getVersionedKeyStrings(getName(), databeanVersion, pks);
	}

	/** tracing ************************************************************/

	protected void startTraceSpan(String opName){
		TracerTool.startSpan(TracerThreadLocal.get(), getTraceName(opName));
	}

	protected void finishTraceSpan(){
		TracerTool.finishSpan(TracerThreadLocal.get());
	}

	protected String getTraceName(String opName){
		return getName() + " " + opName;
	}
}
