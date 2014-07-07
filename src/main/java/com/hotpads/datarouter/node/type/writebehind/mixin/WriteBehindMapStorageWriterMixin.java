package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;

public class WriteBehindMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
implements MapStorageWriter<PK,D>{

	protected BaseWriteBehindNode<PK,D,N> node;


	public WriteBehindMapStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node){
		this.node = node;
	}

	@Override
	public void delete(PK key, Config config){
		node.getQueue().offer(new WriteWrapper<PK>(OP_delete, ListTool.wrap(key), config));
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		node.getQueue().offer(new WriteWrapper<PK>(OP_delete, keys, config));
	}

	@Override
	public void deleteAll(Config config){
		node.getQueue().offer(new WriteWrapper<Object>(OP_deleteAll, null, config));
	}

	@Override
	public void put(D databean, Config config) {
		node.getQueue().offer(new WriteWrapper<D>(OP_put, ListTool.wrap(databean), config));
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		node.getQueue().offer(new WriteWrapper<D>(OP_put, databeans, config));
	}

	public void writeMulti(final Collection<D> flushBatch, final Config config){
		N a = node.getBackingNode();
		a.putMulti(flushBatch, config);
	}

}