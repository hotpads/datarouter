package com.hotpads.datarouter.node.type.writebehind;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorage<PK,D>>
extends WriteBehindSortedMapStorageReaderNode<PK,D,N>
implements SortedMapStorage<PK,D>,
		WriteBehindMapStorageWriterMixin<PK,D,N>,
		WriteBehindSortedStorageWriterMixin<PK,D,N>{

	public WriteBehindSortedMapStorageNode(Datarouter datarouter, N backingNode){
		super(datarouter, backingNode);
	}

	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		return super.handleWriteWrapperInternal(writeWrapper)
				|| WriteBehindMapStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper)
				|| WriteBehindSortedStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper);
	}

}
