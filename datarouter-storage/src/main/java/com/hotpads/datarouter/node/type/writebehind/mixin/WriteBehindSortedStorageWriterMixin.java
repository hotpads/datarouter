package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Arrays;
import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.node.type.writebehind.WriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface WriteBehindSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageWriterNode<PK,D>>
extends SortedStorageWriter<PK,D>, WriteBehindNode<PK,D,N>{

	@Override
	public default void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		getQueue().offer(new WriteWrapper<>(OP_deleteRangeWithPrefix, Arrays.asList(new DeleteRangeWithPrefixWraper<>(
				prefix, wildcardLastField)), config));
	}

	@Override
	default boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(writeWrapper.getOp().equals(OP_deleteRangeWithPrefix)){
			@SuppressWarnings("unchecked")
			Collection<DeleteRangeWithPrefixWraper<PK>> deleteRangeWithPrefixWrapers =
					(Collection<DeleteRangeWithPrefixWraper<PK>>)writeWrapper.getObjects();
			for(DeleteRangeWithPrefixWraper<PK> deleteRangeWithPrefixWraper : deleteRangeWithPrefixWrapers){
				getBackingNode().deleteRangeWithPrefix(deleteRangeWithPrefixWraper.getPrefix(),
						deleteRangeWithPrefixWraper.isWildcardLastField(), writeWrapper.getConfig());
			}
			return true;
		}
		return false;
	}

	public static class DeleteRangeWithPrefixWraper<PK extends PrimaryKey<PK>> {

		private PK prefix;
		private boolean wildcardLastField;

		public DeleteRangeWithPrefixWraper(PK prefix, boolean wildcardLastField){
			this.prefix = prefix;
			this.wildcardLastField = wildcardLastField;
		}

		public PK getPrefix(){
			return prefix;
		}

		public boolean isWildcardLastField(){
			return wildcardLastField;
		}

	}
}
