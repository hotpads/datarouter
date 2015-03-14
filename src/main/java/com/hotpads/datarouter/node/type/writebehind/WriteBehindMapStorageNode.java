package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements MapStorageNode<PK,D>{

	private WriteBehindMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	
	public WriteBehindMapStorageNode(Class<D> databeanClass, Datarouter router,
			N backingNode) {
		super(databeanClass, router, backingNode);
		this.mixinMapWriteOps = new WriteBehindMapStorageWriterMixin<>(this);
	}
	
	
	/***************************** MapStorageWriter ****************************/

	@Override
	public void delete(final PK key, final Config config) {
		mixinMapWriteOps.delete(key, config);
	}

	
	@Override
	public void deleteAll(final Config config) {
		mixinMapWriteOps.deleteAll(config);
	}

	
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config) {
		mixinMapWriteOps.deleteMulti(keys, config);
	}

	
	@Override
	public void put(final D databean, final Config config) {
		mixinMapWriteOps.put(databean, config);
	}

	
	@Override
	public void putMulti(final Collection<D> databeans, final Config config) {
		mixinMapWriteOps.putMulti(databeans, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		if(writeWrapper.getOp().equals(OP_put)){
			backingNode.putMulti((Collection<D>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_delete)){
			backingNode.deleteMulti((Collection<PK>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_deleteAll)){
			backingNode.deleteAll(writeWrapper.getConfig());
		}else{
			return false;
		}
		return true;
	}

}
