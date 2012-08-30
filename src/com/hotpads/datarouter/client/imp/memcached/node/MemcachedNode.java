package com.hotpads.datarouter.client.imp.memcached.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.memcached.DataRouterMemcachedKey;
import com.hotpads.datarouter.client.imp.memcached.node.task.MemcachedMultiAttemptTask;
import com.hotpads.datarouter.client.imp.memcached.node.task.MemcachedTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class MemcachedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends MemcachedReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D>
{
	
	public MemcachedNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName, int databeanVersion) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName, databeanVersion);
	}
	
	public MemcachedNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, int databeanVersion) {
		super(databeanClass, fielderClass, router, clientName, databeanVersion);
	}
	
	public MemcachedNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			Class<F> fielderClass, DataRouter router, String clientName, int databeanVersion){
		super(baseDatabeanClass, databeanClass, fielderClass, router, clientName, databeanVersion);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/
	
	public static final byte[] FAM = HBaseSimpleClientFactory.DEFAULT_FAMILY_QUALIFIER;
	public static final String DUMMY = HBaseSimpleClientFactory.DUMMY_COL_NAME;
	
	
	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){ return; }
		putMulti(ListTool.wrap(databean), config);
	}

	
	@Override
	public void putMulti(final Collection<D> databeans, final Config pConfig) {
		if(CollectionTool.isEmpty(databeans)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new MemcachedMultiAttemptTask<Void>(new MemcachedTask<Void>("putMulti", this, config){
			public Void memcachedCall() throws Exception{
				for(D databean : databeans){
					if( ! databean.isFieldAware()){ throw new IllegalArgumentException("databeans must be field aware"); }
					//TODO put only the nonKeyFields in the byte[] and figure out the keyFields from the key string
					//  could big big savings for small or key-only databeans
					byte[] bytes = DatabeanTool.getBytes(databean);
					String key = new DataRouterMemcachedKey<PK>(name, databeanVersion, databean.getKey()).getVersionedKeyString();
					spyClient.set(key, Integer.MAX_VALUE, bytes);
				}
				TraceContext.appendToSpanInfo(CollectionTool.size(databeans)+"");
				return null;
			}
		}).call();
	}
	
	
	@Override
	public void deleteAll(final Config pConfig) {
		throw new UnsupportedOperationException();
	}

	
	@Override
	public void delete(PK key, Config pConfig) {
		deleteMulti(ListTool.wrap(key), pConfig);
	}

	
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config pConfig){
		if(CollectionTool.isEmpty(keys)){ return; }
		final Config config = Config.nullSafe(pConfig);
		new MemcachedMultiAttemptTask<Void>(new MemcachedTask<Void>("deleteMulti", this, config){
			public Void memcachedCall() throws Exception{
				for(PK key : keys){
					spyClient.delete(key.getPersistentString());
				}
				TraceContext.appendToSpanInfo(CollectionTool.size(keys)+"");
				return null;
			}
		}).call();
	}
	
	

}
