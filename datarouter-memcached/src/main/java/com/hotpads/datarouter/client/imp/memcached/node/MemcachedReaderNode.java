package com.hotpads.datarouter.client.imp.memcached.node;

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
import com.hotpads.datarouter.client.imp.memcached.client.DatarouterMemcachedKey;
import com.hotpads.datarouter.client.imp.memcached.client.MemcachedClient;
import com.hotpads.datarouter.client.imp.memcached.client.MemcachedStateException;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.trace.TraceContext;

public class MemcachedReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MemcachedPhysicalNode<PK,D>,
		MapStorageReader<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedReaderNode.class);
	
	protected final Integer databeanVersion;
	
	/******************************* constructors ************************************/

	public MemcachedReaderNode(NodeParams<PK,D,F> params){
		super(params);
		this.databeanVersion = Preconditions.checkNotNull(params.getSchemaVersion());
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public MemcachedClient getClient(){
		return (MemcachedClient)getRouter().getClient(getClientId().getName());
	}
	
	/************************************ MapStorageReader methods ****************************/
	
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
			Future<Object> future = getClient().getSpyClient().asyncGet(buildMemcachedKey(key));
			//get result asynchronously.  default CacheTimeoutMS set in MapCachingStorage.CACHE_CONFIG
			bytes = (byte[])future.get(config.getCacheTimeoutMs(), TimeUnit.MILLISECONDS); 
		} catch(TimeoutException e) {						
			TraceContext.appendToSpanInfo("memcached timeout");
		} catch(InterruptedException | ExecutionException | MemcachedStateException e) {
			logger.error("", e);
		}

		try {
			if(DrArrayTool.isEmpty(bytes)){ 
				TraceContext.appendToSpanInfo("miss");
				return null; 
			}
			TraceContext.appendToSpanInfo("hit");
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getDatabeanType(), 
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
			return new LinkedList<D>();
		}
		startTraceSpan(MapStorageReader.OP_getMulti);
		final Config config = Config.nullSafe(paramConfig);
		List<D> databeans = DrListTool.createArrayListWithSize(keys);
		Map<String,Object> bytesByStringKey = null;

		
		try {
			//get results asynchronously.  default CacheTimeoutMS set in MapCachingStorage.CACHE_CONFIG
			Future<Map<String,Object>> future = getClient().getSpyClient().asyncGetBulk(buildMemcachedKeys(keys));
			bytesByStringKey = future.get(config.getCacheTimeoutMs(), TimeUnit.MILLISECONDS);
		} catch(TimeoutException e) {										
			TraceContext.appendToSpanInfo("memcached timeout");	
		} catch(ExecutionException | InterruptedException | MemcachedStateException e){
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
					D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getDatabeanType(), 
							fieldInfo.getFieldByPrefixedName(), is, bytes.length);
					databeans.add(databean);
				} catch (IOException e) {
					logger.error("", e);
				}
			}
			TraceContext.appendToSpanInfo("[got "+DrCollectionTool.size(databeans)+"/"+DrCollectionTool.size(keys)+"]");
			return databeans;
		}finally{
			finishTraceSpan();
		}
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config paramConfig){
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<PK>();
		}
		return DatabeanTool.getKeys(getMulti(keys, paramConfig));
	}
	
	
	/******************** serialization *******************/
	
	protected String buildMemcachedKey(PK pk){
		return new DatarouterMemcachedKey<PK>(getName(), databeanVersion, pk).getVersionedKeyString();
	}
	
	protected List<String> buildMemcachedKeys(Collection<PK> pks){
		return DatarouterMemcachedKey.getVersionedKeyStrings(getName(), databeanVersion, pks);
	}
	
	/******************* tracing ***************************/
	
	protected void startTraceSpan(String opName){
		TraceContext.startSpan(getTraceName(opName));
	}

	protected void finishTraceSpan(){
		TraceContext.finishSpan();
	}

	protected String getTraceName(String opName){
		return getName() + " " + opName;
	}
}
