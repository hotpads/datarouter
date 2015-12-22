package com.hotpads.datarouter.node.type.writebehind;

import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public class WriteBehindMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements MapStorageNode<PK,D>, WriteBehindMapStorageWriterMixin<PK,D,N>{

	public WriteBehindMapStorageNode(Supplier<D> databeanSupplier, Router router, N backingNode) {
		super(databeanSupplier, router, backingNode);
	}

	/**
	 * @deprecated use {@link #WriteBehindMapStorageNode(Supplier, Router, N)}
	 */
	@Deprecated
	public WriteBehindMapStorageNode(Class<D> databeanClass, Router router, N backingNode) {
		super(ReflectionTool.supplier(databeanClass), router, backingNode);
	}

	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		return WriteBehindMapStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper);
	}

}
