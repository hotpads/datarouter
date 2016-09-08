package com.hotpads.datarouter.client.imp.redis.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.redis.client.DatarouterRedisKey;
import com.hotpads.datarouter.client.imp.redis.client.RedisClient;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabeanKey;
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
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;

public class RedisReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F> implements MapStorageReader<PK,D>{

	private final Integer databeanVersion;

	/*** constructor *********************************************************/

	public RedisReaderNode(NodeParams<PK,D,F> params){
		super(params);
		this.databeanVersion = Preconditions.checkNotNull(params.getSchemaVersion());
	}

	/** plumbing *************************************************************/

	@Override
	public RedisClient getClient(){
		return (RedisClient)getRouter().getClient(getClientId().getName());
	}

	/** exists ***************************************************************/

	@Override
	public boolean exists(PK key, Config config){
		try{
			startTraceSpan("redis exists");
			return getClient().getJedisClient().exists(buildRedisKey(key));
		}finally{
			finishTraceSpan();
		}
	}

	/** get ******************************************************************/

	@Override
	public D get(final PK key, final Config config){
		if(key == null){
			return null;
		}
		try{
			startTraceSpan("redis get");
			String json = getClient().getJedisClient().get(buildRedisKey(key));
			if(json == null){
				return null;
			}
			return JsonDatabeanTool.databeanFromJson(fieldInfo.getDatabeanSupplier(), fieldInfo.getSampleFielder(),
					json);
		}finally{
			finishTraceSpan();
		}
	}

	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config){
		if(DrCollectionTool.isEmpty(keys)){
			return Collections.emptyList();
		}
		List<String> jsons = null;
		try{
			startTraceSpan("redis get multi");
			jsons = getClient().getJedisClient().mget(buildRedisKeys(keys).toArray(new String[keys.size()]));
		}finally{
			finishTraceSpan();
		}
		List<D> databeans = new ArrayList<>();
		for(String json : jsons){
			if(json == null){
				continue;
			}
			databeans.add(JsonDatabeanTool.databeanFromJson(fieldInfo.getDatabeanSupplier(), fieldInfo
					.getSampleFielder(), json));
		}
		return databeans;
	}

	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config){
		if(DrCollectionTool.isEmpty(keys)){
			return Collections.emptyList();
		}
		return DatabeanTool.getKeys(getMulti(keys, config));
	}

	public Long getTallyCount(RedisDatabeanKey key){
		if(key == null){
			return null;
		}
		try{
			startTraceSpan("redis getTallyCount");
			String tallyCount = getClient().getJedisClient().get(buildRedisKey(key));
			if(tallyCount == null){
				return null;
			}
			return Long.valueOf(tallyCount.trim());
		}finally{
			finishTraceSpan();
		}
	}

	/** serialization ********************************************************/

	protected String buildRedisKey(PrimaryKey<?> pk){
		return new DatarouterRedisKey(getName(), databeanVersion, pk).getVersionedKeyString();
	}

	protected List<String> buildRedisKeys(Collection<? extends PrimaryKey<?>> pks){
		return DatarouterRedisKey.getVersionedKeyStrings(getName(), databeanVersion, pks);
	}

	/** tracing **************************************************************/

	protected void startTraceSpan(String opName){
		TracerTool.startSpan(TracerThreadLocal.get(), getName() + " " + opName);
	}

	protected void finishTraceSpan(){
		TracerTool.finishSpan(TracerThreadLocal.get());
	}
}