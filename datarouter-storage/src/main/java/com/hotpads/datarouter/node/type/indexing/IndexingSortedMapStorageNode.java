package com.hotpads.datarouter.node.type.indexing;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.indexing.base.BaseIndexingNode;
import com.hotpads.datarouter.node.type.indexing.mixin.IndexingMapStorageMixin;
import com.hotpads.datarouter.node.type.indexing.mixin.IndexingSortedStorageMixin;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class IndexingSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends BaseIndexingNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D>,
		IndexingMapStorageMixin<PK,D,N>,
		IndexingSortedStorageMixin<PK,D,N>{

	public IndexingSortedMapStorageNode(N mainNode) {
		super(mainNode);//mainNode must have explicit Fielder
	}

}
