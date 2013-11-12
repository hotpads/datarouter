package com.hotpads.datarouter.client.imp.http.node;

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

import com.amazonaws.util.json.JSONObject;
import com.hotpads.datarouter.client.imp.http.HttpClient;
import com.hotpads.datarouter.client.imp.memcached.DataRouterMemcachedKey;
import com.hotpads.datarouter.client.imp.memcached.MemcachedStateException;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Config.ConfigFielder;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public class HttpReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>{
	protected static Logger logger = Logger.getLogger(HttpReaderNode.class);
	
	private ConfigFielder configFielder;
		
	/******************************* constructors ************************************/

	public HttpReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
		this.configFielder = new ConfigFielder();
	}
	
	public HttpReaderNode(Class<D> databeanClass,Class<F> fielderClass,
			DataRouter router, String clientName) {
		super(databeanClass, fielderClass, router, clientName);
		this.configFielder = new ConfigFielder();
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HttpClient getClient(){
		return (HttpClient)this.router.getClient(getClientName());
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
		StringBuilder urlBuilder = getOpUrl("/get/json");
		JSONObject json = new JSONObject();
		json.put("key", JsonDatabeanTool.primaryKeyToJson(key, fieldInfo.getSampleFielder().getKeyFielder()));
		json.put("config", JsonDatabeanTool.databeanToJson(pConfig, configFielder));
		urlBuilder.append("?params=");
		urlBuilder.append(json.toString());
		
		
			
		if(ArrayTool.isEmpty(bytes)){ 
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
				DataRouterMemcachedKey.getVersionedKeyStrings(name, databeanVersion, keys));
			bytesByStringKey = f.get(config.getCacheTimeoutMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {										
			TraceContext.appendToSpanInfo("memcached timeout");	
		} catch (ExecutionException e) {					
		} catch (InterruptedException e) {					
		} catch (MemcachedStateException e) {
			logger.error(ExceptionTool.getStackTraceAsString(e));
		}
		
		if (bytesByStringKey == null)
			return null;
		
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
		return databeans;
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		return KeyTool.getKeys(getMulti(keys, pConfig));
	}

	
	/***************************** private *****************************/
	
	private StringBuilder getNodeUrl(){
		StringBuilder sb = new StringBuilder();
		sb.append(getClient().getUrl().toExternalForm());
		sb.append("/");
		sb.append(router.getName());
		sb.append("/");
		sb.append(getName());
		return sb;
	}
	
	private StringBuilder getOpUrl(String opName){
		StringBuilder sb = getNodeUrl();
		sb.append("/");
		sb.append(opName);
		return sb;
	}
	
}
