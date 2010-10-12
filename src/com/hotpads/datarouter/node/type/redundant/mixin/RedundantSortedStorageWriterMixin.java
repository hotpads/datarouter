package com.hotpads.datarouter.node.type.redundant.mixin;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.redundant.BaseRedundantNode;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends SortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{

	protected BaseRedundantNode<PK,D,N> target;
	
	public RedundantSortedStorageWriterMixin(BaseRedundantNode<PK,D,N> target){
		this.target = target;
	}
	
	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		for(N n : target.getWriteNodes()){
			n.deleteRangeWithPrefix(prefix, wildcardLastField, config);
		}
	}
}
