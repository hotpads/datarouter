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

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.memcached.DataRouterMemcachedKey;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClient;
import com.hotpads.datarouter.client.imp.memcached.MemcachedStateException;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public class MemcachedReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MemcachedPhysicalNode<PK,D>,
		MapStorageReader<PK,D>{
	protected static Logger logger = Logger.getLogger(MemcachedReaderNode.class);
	
	protected Integer databeanVersion;
	
	/******************************* constructors ************************************/

	public MemcachedReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName, int databeanVersion) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
		this.databeanVersion = Preconditions.checkNotNull(databeanVersion);
	}
	
	public MemcachedReaderNode(Class<D> databeanClass,Class<F> fielderClass,
			DataRouter router, String clientName, int databeanVersion) {
		super(databeanClass, fielderClass, router, clientName);
		this.databeanVersion = Preconditions.checkNotNull(databeanVersion);
	}

	public MemcachedReaderNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			Class<F> fielderClass, DataRouter router, String clientName, int databeanVersion){
		super(baseDatabeanClass, databeanClass, fielderClass, router, clientName);
		this.databeanVersion = Preconditions.checkNotNull(databeanVersion);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public MemcachedClient getClient(){
		return (MemcachedClient)getRouter().getClient(getClientName());
	}
	
	@Override
	public void clearThreadSpecificState(){
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
			String memcachedKey = new DataRouterMemcachedKey<PK>(getName(), databeanVersion, key).getVersionedKeyString();
			byte[] bytes = null;
			
			try {
				Future<Object> f = this.getClient().getSpyClient().asyncGet(memcachedKey);
				bytes = (byte[])f.get(config.getCacheTimeoutMs(), TimeUnit.MILLISECONDS); //get result asynchronously.  default CacheTimeoutMS set in MapCachingStorage.CACHE_CONFIG
			} catch (TimeoutException e) {						
				TraceContext.appendToSpanInfo("memcached timeout");
			} catch (InterruptedException e) {						
			} catch (ExecutionException e) {						
			} catch (MemcachedStateException e) {
				logger.error(ExceptionTool.getStackTraceAsString(e));
			}
			
			String opName = "get";
			DRCounters.incSuffixClientNode(ClientType.memcached, opName, getClientName(), getName());
			
			if(ArrayTool.isEmpty(bytes)){ 
				TraceContext.appendToSpanInfo("miss");
				DRCounters.incSuffixClientNode(ClientType.memcached, opName+" miss", getClientName(), getName());
				return null; 
			}
//					System.out.println(StringByteTool.fromUtf8Bytes(bytes));
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			try {
				D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getDatabeanType(), 
						fieldInfo.getFieldByPrefixedName(), is, bytes.length);
				DRCounters.incSuffixClientNode(ClientType.memcached, opName+" hit", getClientName(), getName());
				return databean;
			} catch (IOException e) {
				logger.error(ExceptionTool.getStackTraceAsString(e));
				return null;
			}
	}
	
	
	@Override
	public List<D> getAll(final Config pConfig){
		throw new UnsupportedOperationException();
	}

	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		List<D> databeans = ListTool.createArrayListWithSize(keys);
		Map<String,Object> bytesByStringKey = null;

		
		try {
			Future<Map<String,Object>> f = this.getClient().getSpyClient().asyncGetBulk( //get results asynchronously.  default CacheTimeoutMS set in MapCachingStorage.CACHE_CONFIG
				DataRouterMemcachedKey.getVersionedKeyStrings(getName(), databeanVersion, keys));
			bytesByStringKey = f.get(config.getCacheTimeoutMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {										
			TraceContext.appendToSpanInfo("memcached timeout");	
		} catch (ExecutionException e) {					
		} catch (InterruptedException e) {					
		} catch (MemcachedStateException e) {
			logger.error(ExceptionTool.getStackTraceAsString(e));
		}
		
		if (bytesByStringKey == null){
			return null;
		}
		
		for(Map.Entry<String,Object> entry : bytesByStringKey.entrySet()){
			byte[] bytes = (byte[])entry.getValue();
			if(ArrayTool.isEmpty(bytes)){ return null; }
			ByteArrayInputStream is = new ByteArrayInputStream((byte[])entry.getValue());
			try {
				D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getDatabeanType(), 
						fieldInfo.getFieldByPrefixedName(), is, bytes.length);
				databeans.add(databean);
			} catch (IOException e) {
				logger.error(ExceptionTool.getStackTraceAsString(e));
			}
		}
		TraceContext.appendToSpanInfo("[got "+CollectionTool.size(databeans)+"/"+CollectionTool.size(keys)+"]");

		String opName = "getMulti";
		DRCounters.incSuffixClientNode(ClientType.memcached, opName, getClientName(), getName());
		int requested = keys.size();
		int hit = databeans.size();
		int miss = requested - hit;
		DRCounters.incSuffixClientNode(ClientType.memcached, opName+" requested", getClientName(), getName(), requested);
		DRCounters.incSuffixClientNode(ClientType.memcached, opName+" hit", getClientName(), getName(), hit);
		DRCounters.incSuffixClientNode(ClientType.memcached, opName+" miss", getClientName(), getName(), miss);
				
		return databeans;
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		return KeyTool.getKeys(getMulti(keys, pConfig));
	}

	
}