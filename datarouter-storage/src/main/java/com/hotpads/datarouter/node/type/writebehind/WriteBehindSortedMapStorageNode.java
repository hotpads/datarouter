package com.hotpads.datarouter.node.type.writebehind;

import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteBehindSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public class WriteBehindSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends WriteBehindSortedMapStorageReaderNode<PK,D,N>
implements SortedMapStorageNode<PK,D>,
		WriteBehindMapStorageWriterMixin<PK,D,N>,
		WriteBehindSortedStorageWriterMixin<PK,D,N>{

	public WriteBehindSortedMapStorageNode(Supplier<D> databeanSupplier, Router router, N backingNode){
		super(databeanSupplier, router, backingNode);
	}

	/**
	 * @deprecated use {@link #WriteBehindSortedMapStorageNode(Supplier,Router,N)}
	 */
	@Deprecated
	public WriteBehindSortedMapStorageNode(Class<D> databeanClass, Router router, N backingNode){
		this(ReflectionTool.supplier(databeanClass), router, backingNode);
	}

	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		return super.handleWriteWrapperInternal(writeWrapper)
				|| WriteBehindMapStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper)
				|| WriteBehindSortedStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper);
	}

}
