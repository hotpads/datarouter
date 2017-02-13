package com.hotpads.datarouter.node.type.writebehind;

import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorage<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements MapStorage<PK,D>, WriteBehindMapStorageWriterMixin<PK,D,N>{

	public WriteBehindMapStorageNode(Datarouter datarouter, N backingNode){
		super(datarouter, backingNode);
	}

	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		return super.handleWriteWrapperInternal(writeWrapper) || WriteBehindMapStorageWriterMixin.super
				.handleWriteWrapperInternal(writeWrapper);
	}

}
