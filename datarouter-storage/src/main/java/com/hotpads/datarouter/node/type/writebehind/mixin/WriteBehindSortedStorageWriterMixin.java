package com.hotpads.datarouter.node.type.writebehind.mixin;

import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.type.writebehind.WriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface WriteBehindSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageWriter<PK,D>>
extends SortedStorageWriter<PK,D>, WriteBehindNode<PK,D,N>{

	@Override
	default boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		return false;
	}

}
