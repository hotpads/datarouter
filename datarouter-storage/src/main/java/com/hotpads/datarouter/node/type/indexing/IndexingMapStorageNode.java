package com.hotpads.datarouter.node.type.indexing;

import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.indexing.base.BaseIndexingNode;
import com.hotpads.datarouter.node.type.indexing.mixin.IndexingMapStorageMixin;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class IndexingMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D>>
extends BaseIndexingNode<PK,D,F,N>
implements MapStorageNode<PK,D>, IndexingMapStorageMixin<PK,D,N>{

	public IndexingMapStorageNode(N mainNode) {
		super(mainNode);//mainNode must have explicit Fielder
	}

}
