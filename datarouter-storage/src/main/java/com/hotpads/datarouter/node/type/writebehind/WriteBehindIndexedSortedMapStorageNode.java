package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindIndexedStorageWriterMixin;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class WriteBehindIndexedSortedMapStorageNode<
	PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>,
	N extends IndexedSortedMapStorage<PK,D>>
extends WriteBehindIndexedMapStorageReaderNode<PK,D,N>
implements IndexedSortedMapStorage<PK,D>,
		WriteBehindMapStorageWriterMixin<PK,D,N>,
		WriteBehindSortedStorageWriterMixin<PK,D,N>{

	protected WriteBehindIndexedStorageWriterMixin<PK,D,N> mixinIndexedWriteOps;

	public WriteBehindIndexedSortedMapStorageNode(Datarouter datarouter, N backingNode) {
		super(datarouter, backingNode);
		mixinIndexedWriteOps = new WriteBehindIndexedStorageWriterMixin<>(this);
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config) {
		mixinIndexedWriteOps.delete(lookup, config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		mixinIndexedWriteOps.deleteUnique(uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		mixinIndexedWriteOps.deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		mixinIndexedWriteOps.deleteByIndex(keys, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		if(WriteBehindMapStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		if(WriteBehindSortedStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		if(writeWrapper.getOp().equals(OP_deleteUnique)){
			backingNode.deleteMultiUnique((Collection<? extends UniqueKey<PK>>)writeWrapper.getObjects(), writeWrapper
					.getConfig());
		}else if(writeWrapper.getOp().equals(OP_indexDelete)){
			Collection<Lookup<PK>> lookups = (Collection<Lookup<PK>>)writeWrapper.getObjects();
			for(Lookup<PK> lookup : lookups){
				backingNode.delete(lookup, writeWrapper.getConfig());
			}
		}else{
			return false;
		}
		return true;
	}

}
