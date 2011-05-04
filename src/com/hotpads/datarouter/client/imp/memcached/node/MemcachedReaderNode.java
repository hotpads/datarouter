package com.hotpads.datarouter.client.imp.memcached.node;

import java.io.ByteArrayInputStream;
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
import com.hotpads.datarouter.client.imp.memcached.DataRouterMemcachedKey;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClient;
import com.hotpads.datarouter.client.imp.memcached.node.task.MemcachedMultiAttemptTask;
import com.hotpads.datarouter.client.imp.memcached.node.task.MemcachedTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
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
		return (MemcachedClient)this.router.getClient(getClientName());
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
		return new MemcachedMultiAttemptTask<D>(new MemcachedTask<D>("get", this, config){
				public D memcachedCall() throws Exception{
					String memcachedKey = new DataRouterMemcachedKey<PK>(name, databeanVersion, key).getVersionedKeyString();
					byte[] bytes = null;

					Future<Object> f = spyClient.asyncGet(memcachedKey);
					try {
						bytes = (byte[])f.get(100, TimeUnit.MILLISECONDS);
					} catch (TimeoutException e) {						
						TraceContext.appendToSpanInfo("memcached timeout");
					} catch (InterruptedException e) {						
					} catch (ExecutionException e) {						
					}
					
					if(ArrayTool.isEmpty(bytes)){ 
						TraceContext.appendToSpanInfo("miss");
						return null; 
					}
//					System.out.println(StringByteTool.fromUtf8Bytes(bytes));
					ByteArrayInputStream is = new ByteArrayInputStream(bytes);
					D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getDatabeanType(), 
							fieldInfo.getFieldByPrefixedName(), is, bytes.length);
					return databean;
				}
			}).call();
	}
	
	
	@Override
	public List<D> getAll(final Config pConfig){
		throw new UnsupportedOperationException();
	}

	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		return new MemcachedMultiAttemptTask<List<D>>(new MemcachedTask<List<D>>("getMulti", this, config){
			public List<D> memcachedCall() throws Exception{
				List<D> databeans = ListTool.createArrayListWithSize(keys);
				Map<String,Object> bytesByStringKey = null;

				Future<Map<String,Object>> f = spyClient.asyncGetBulk(
						DataRouterMemcachedKey.getVersionedKeyStrings(name, databeanVersion, keys));
				try {
					bytesByStringKey = f.get(100, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {										
					TraceContext.appendToSpanInfo("memcached timeout");	
				} catch (ExecutionException e) {					
				} catch (InterruptedException e) {					
				}
				
				if (bytesByStringKey == null)
					return null;
				
				for(Map.Entry<String,Object> entry : bytesByStringKey.entrySet()){
					byte[] bytes = (byte[])entry.getValue();
					if(ArrayTool.isEmpty(bytes)){ return null; }
					ByteArrayInputStream is = new ByteArrayInputStream((byte[])entry.getValue());
					D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(getDatabeanType(), 
							fieldInfo.getFieldByPrefixedName(), is, bytes.length);
					databeans.add(databean);
				}
				TraceContext.appendToSpanInfo("[got "+CollectionTool.size(databeans)+"/"+CollectionTool.size(keys)+"]");
				return databeans;
			}
		}).call();
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		return KeyTool.getKeys(getMulti(keys, pConfig));
	}

	
}
