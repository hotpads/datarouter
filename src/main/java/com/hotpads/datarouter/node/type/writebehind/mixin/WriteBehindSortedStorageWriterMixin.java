package com.hotpads.datarouter.node.type.writebehind.mixin;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;

public class WriteBehindSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{

	private BaseWriteBehindNode<PK,D,N> node;

	public WriteBehindSortedStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node){
		this.node = node;
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		node.getQueue().offer(
				new WriteWrapper<DeleteRangeWithPrefixWraper<PK>>(OP_deleteRangeWithPrefix, DrListTool
						.wrap(new DeleteRangeWithPrefixWraper<PK>(prefix, wildcardLastField)), config));
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
