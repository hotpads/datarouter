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
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.KeyTool;
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
	protected static Logger logger = LoggerFactory.getLogger(MemcachedReaderNode.class);
	
	protected Integer databeanVersion;
	
	/******************************* constructors ************************************/

	public MemcachedReaderNode(NodeParams<PK,D,F> params){
		super(params);
		this.databeanVersion = Preconditions.checkNotNull(params.getSchemaVersion());
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public MemcachedClient getClient(){
		return (MemcachedClient)getRouter().getClient(getClientName());
	}
	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(PK key, Config config) {
		return get(key, config) != null;
	}

	
	@Override
	public D get(final PK key, final Config pConfig){
		if(key==null){ return null; }
		final Config config = Config.nullSafe(pConfig);
			String memcachedKey = new DatarouterMemcachedKey<PK>(getName(), databeanVersion, key).getVersionedKeyString();
			byte[] bytes = null;
			
			try {
				Future<Object> f = this.getClient().getSpyClient().asyncGet(memcachedKey);
				bytes = (byte[])f.get(config.getCacheTimeoutMs(), TimeUnit.MILLISECONDS); //get result asynchronously.  default CacheTimeoutMS set in MapCachingStorage.CACHE_CONFIG
			} catch (TimeoutException e) {						
				TraceContext.appendToSpanInfo("memcached timeout");
			} catch (InterruptedException e) {						
			} catch (ExecutionException e) {						
			} catch (MemcachedStateException e) {
				logger.error("", e);
			}
			
			if(DrArrayTool.isEmpty(bytes)){ 
				TraceContext.appendToSpanInfo("miss");
				return null; 
			}
//					System.out.println(StringByteTool.fromUtf8Bytes(bytes));
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			try {
				D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getDatabeanType(), 
						fieldInfo.getFieldByPrefixedName(), is, bytes.length);
				return databean;
			} catch (IOException e) {
				logger.error("", e);
				return null;
			}
	}

	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){
		if(DrCollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		List<D> databeans = DrListTool.createArrayListWithSize(keys);
		Map<String,Object> bytesByStringKey = null;

		
		try {
			Future<Map<String,Object>> f = this.getClient().getSpyClient().asyncGetBulk( //get results asynchronously.  default CacheTimeoutMS set in MapCachingStorage.CACHE_CONFIG
				DatarouterMemcachedKey.getVersionedKeyStrings(getName(), databeanVersion, keys));
			bytesByStringKey = f.get(config.getCacheTimeoutMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {										
			TraceContext.appendToSpanInfo("memcached timeout");	
		} catch (ExecutionException e) {					
		} catch (InterruptedException e) {					
		} catch (MemcachedStateException e) {
			logger.error("", e);
		}
		
		if (bytesByStringKey == null){
			return null;
		}
		
		for(Map.Entry<String,Object> entry : bytesByStringKey.entrySet()){
			byte[] bytes = (byte[])entry.getValue();
			if(DrArrayTool.isEmpty(bytes)){ return null; }
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
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(DrCollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		return KeyTool.getKeys(getMulti(keys, pConfig));
	}

	
}