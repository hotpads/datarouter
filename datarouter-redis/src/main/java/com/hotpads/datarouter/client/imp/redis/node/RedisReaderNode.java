package com.hotpads.datarouter.client.imp.redis.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.redis.client.DatarouterRedisKey;
import com.hotpads.datarouter.client.imp.redis.client.RedisClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
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

//	private static final Logger logger = LoggerFactory.getLogger(RedisReaderNode.class);

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
	public boolean exists(PK key, Config config){
		// TODO figure out what to do with the config
		return getClient().getJedisClient().exists(buildRedisKey(key));
	}


	@Override
	public D get(final PK key, final Config paramConfig){
		if(key == null){
			return null;
		}

		String json = getClient().getJedisClient().get(buildRedisKey(key));
		return JsonDatabeanTool.databeanFromJson(fieldInfo.getDatabeanSupplier(), fieldInfo.getSampleFielder(), json);
	}


	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config paramConfig){
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}
		List <D> databeans = DrListTool.createArrayListWithSize(keys);
		for(PK key : keys){
			databeans.add(this.get(key, paramConfig));
		}

		/*
		List <String> jsonDatabeans = getClient().getJedisClient()
				.mget(buildRedisKeys(keys).toArray(new String[keys.size()]));

		List <D> databeans = DrListTool.createArrayListWithSize(keys);
		for(String databeanString : jsonDatabeans){
			databeans.add(JsonDatabeanTool.databeanFromJson(fieldInfo.getDatabeanSupplier(),
					fieldInfo.getSampleFielder(), databeanString));
		}
		*/
		return databeans;
	}

	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config paramConfig){
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}
		return DatabeanTool.getKeys(getMulti(keys, paramConfig));
	}

//
//	public Long getTallyCount(TallyKey key, final Config paramConfig){
//		if(key == null){
//			return null;
//		}
//
//		String json = getClient().getJedisClient().get(buildRedisKey(key));
//		JsonDatabeanTool.databeanFromJson(fieldInfo.getDatabeanSupplier(), fieldInfo.getSampleFielder(),
//				json);
//
//		Object tallyObject = null;
//		try{
//			tallyObject = getClient().getJedisClient().get(buildRedisKey(key));
//		}catch(Exception exception){
//			if(paramConfig.ignoreExceptionOrUse(true)){
//				logger.error("redis error on " + key, exception);
//			}else{
//				throw new RuntimeException(exception);
//			}
//		}
//
//		return Long.valueOf(((String)tallyObject).trim());
//	}

	/** serialization ****************************************************/

	protected String buildRedisKey(PrimaryKey<?> pk){
		return new DatarouterRedisKey(getName(), databeanVersion, pk).getVersionedKeyString();
	}

	protected List<String> buildRedisKeys(Collection<? extends PrimaryKey<?>> pks){
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
