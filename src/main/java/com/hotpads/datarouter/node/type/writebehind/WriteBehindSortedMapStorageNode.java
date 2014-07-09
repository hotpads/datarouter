package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindSortedStorageWriterMixin;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindSortedStorageWriterMixin.DeleteRangeWithPrefixWraper;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends WriteBehindSortedMapStorageReaderNode<PK,D,N>
implements SortedMapStorageNode<PK,D>{

	protected WriteBehindMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	protected WriteBehindSortedStorageWriterMixin<PK,D,N> mixinSortedWriteOps;
	
	public WriteBehindSortedMapStorageNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
		this.mixinMapWriteOps = new WriteBehindMapStorageWriterMixin<PK,D,N>(this);
		this.mixinSortedWriteOps = new WriteBehindSortedStorageWriterMixin<PK,D,N>(this);
	}
	
	
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
	

	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(super.handleWriteWrapperInternal(writeWrapper)){ return true; }

		if(writeWrapper.getOp().equals(OP_put)){
			backingNode.putMulti((Collection<D>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_delete)){
			backingNode.deleteMulti((Collection<PK>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_deleteAll)){
			backingNode.deleteAll(writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_deleteRangeWithPrefix)){
			Collection<DeleteRangeWithPrefixWraper<PK>> deleteRangeWithPrefixWrapers = (Collection<DeleteRangeWithPrefixWraper<PK>>)writeWrapper
					.getObjects();
			for(DeleteRangeWithPrefixWraper<PK> deleteRangeWithPrefixWraper : deleteRangeWithPrefixWrapers){
				backingNode.deleteRangeWithPrefix(deleteRangeWithPrefixWraper.getPrefix(), deleteRangeWithPrefixWraper
						.isWildcardLastField(), writeWrapper.getConfig());
			}
		}else{
			return false;
		}
		return true;
	}

}
