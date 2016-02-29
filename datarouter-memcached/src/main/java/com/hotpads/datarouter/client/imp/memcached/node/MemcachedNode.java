package com.hotpads.datarouter.client.imp.memcached.node;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.memcached.client.MemcachedStateException;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;

public class MemcachedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends MemcachedReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedNode.class);

	protected static final int MEGABYTE = 1024 * 1024;

	public MemcachedNode(NodeParams<PK,D,F> params){
		super(params);
	}

	@Override
	public Node<PK,D> getMaster() {
		return this;
	}


	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){
			return;
		}
		putMulti(DrListTool.wrap(databean), config);
	}


	//TODO does spy client not do batched puts?
	@Override
	public void putMulti(final Collection<D> databeans, final Config paramConfig) {
		if(DrCollectionTool.isEmpty(databeans)){
			return;
		}
		try{
			startTraceSpan(MapStorageWriter.OP_putMulti);
			final Config config = Config.nullSafe(paramConfig);
			for(D databean : databeans){
				if( ! fieldInfo.getFieldAware()){
					throw new IllegalArgumentException("databeans must be field aware");
				}
				//TODO put only the nonKeyFields in the byte[] and figure out the keyFields from the key string
				//  could big big savings for small or key-only databeans
				byte[] bytes = DatabeanTool.getBytes(databean, fieldInfo.getSampleFielder());
				String key = buildMemcachedKey(databean.getKey());
				//memcachedClient uses an integer for cache timeout
				Long timeoutLong = config.getTtlMs() == null
						? Long.MAX_VALUE
						: config.getTtlMs() / 1000;
				Integer expiration = timeoutLong > new Long(Integer.MAX_VALUE)
						? Integer.MAX_VALUE
						: timeoutLong.intValue();
				if (bytes.length > 2 * MEGABYTE) {
					//memcached max size is 1mb for a compressed object, so don't PUT things that won't compress well
					String json = JsonDatabeanTool.fieldsToJson(databean.getKey().getFields()).toString();
					logger.error("memcached object too big for memcached!" + databean.getDatabeanName() + ", key:"
							+ json);
					return;
				}
				try {
					this.getClient().getSpyClient().set(key, expiration, bytes);
				} catch (MemcachedStateException e) {
					logger.error("memached error on " + key,e);
				}
			}
			TracerTool.appendToSpanInfo(TracerThreadLocal.get(), DrCollectionTool.size(databeans)+"");
		}finally{
			finishTraceSpan();
		}
	}


	@Override
	public void deleteAll(final Config paramConfig){
		throw new UnsupportedOperationException();
	}


	@Override
	public void delete(PK key, Config paramConfig){
		if(key == null){
			return;
		}
		try{
			startTraceSpan(MapStorageWriter.OP_delete);
			try {
				getClient().getSpyClient().delete(buildMemcachedKey(key));
			} catch (MemcachedStateException e) {
				logger.error("", e);
			}
		}finally{
			finishTraceSpan();
		}
	}


	@Override
	public void deleteMulti(final Collection<PK> keys, final Config paramConfig){
		for(PK pk : DrIterableTool.nullSafe(keys)){
			delete(pk, paramConfig);
		}
	}

}
