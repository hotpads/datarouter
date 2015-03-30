package com.hotpads.datarouter.client.imp.memcached.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.memcached.client.DatarouterMemcachedKey;
import com.hotpads.datarouter.client.imp.memcached.client.MemcachedStateException;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.trace.TraceContext;

public class MemcachedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends MemcachedReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D>
{
	
	public MemcachedNode(NodeParams<PK,D,F> params){
		super(params);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/
	
	public static final byte[] FAM = HBaseSimpleClientFactory.DEFAULT_FAMILY_QUALIFIER;
	public static final String DUMMY = HBaseSimpleClientFactory.DUMMY_COL_NAME;
	protected static final int MEGABYTE = 1024 * 1024;
	
	
	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){ return; }
		putMulti(DrListTool.wrap(databean), config);
	}

	
	//TODO does spy client not do batched puts?
	@Override
	public void putMulti(final Collection<D> databeans, final Config pConfig) {
		if(DrCollectionTool.isEmpty(databeans)){ return; }
		final Config config = Config.nullSafe(pConfig);
		for(D databean : databeans){
			if( ! fieldInfo.getFieldAware()){ throw new IllegalArgumentException("databeans must be field aware"); }
			//TODO put only the nonKeyFields in the byte[] and figure out the keyFields from the key string
			//  could big big savings for small or key-only databeans
			byte[] bytes = DatabeanTool.getBytes(databean, fieldInfo.getSampleFielder());
			String key = new DatarouterMemcachedKey<PK>(getName(), databeanVersion, databean.getKey()).getVersionedKeyString();
			//memcachedClient uses an integer for cache timeout
			Long timeoutLong = config.getCacheTimeoutMs() == null 
								? Long.MAX_VALUE 
								: config.getCacheTimeoutMs() / 1000;
			Integer expiration = (timeoutLong > new Long(Integer.MAX_VALUE) 
								? Integer.MAX_VALUE 
								: timeoutLong.intValue());
			if (bytes.length > 2 * MEGABYTE) {
				//memcached max size is 1mb for a compressed object, so don't PUT things that won't compress down that well 
				String json = JsonDatabeanTool.fieldsToJson(databean.getKey().getFields()).toString();
				logger.error("memcached object too big for memcached!" + databean.getDatabeanName() + ", key:" + json);
				return;
			}
			try {
				this.getClient().getSpyClient().set(key, expiration, bytes); 
			} catch (MemcachedStateException e) {
				logger.error("memached error on " + key,e);
			}
		}
		TraceContext.appendToSpanInfo(DrCollectionTool.size(databeans)+"");
	}
	
	
	@Override
	public void deleteAll(final Config pConfig) {
		throw new UnsupportedOperationException();
	}

	
	@Override
	public void delete(PK key, Config pConfig) {
		deleteMulti(DrListTool.wrap(key), pConfig);
	}

	
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config pConfig){
		if(DrCollectionTool.isEmpty(keys)){ return; }
		for(PK key : keys){
			try {
				this.getClient().getSpyClient().delete(key.getPersistentString());
			} catch (MemcachedStateException e) {
				logger.error("", e);
			}
		}
		TraceContext.appendToSpanInfo(DrCollectionTool.size(keys)+"");
	}
	
	

}
