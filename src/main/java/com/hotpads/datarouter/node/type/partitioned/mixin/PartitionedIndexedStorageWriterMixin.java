package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.PhysicalIndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;

public class PartitionedIndexedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedStorageWriterNode<PK,D>>
implements IndexedStorageWriter<PK,D>{
	
	protected BasePartitionedNode<PK,D,F,N> target;
	
	public PartitionedIndexedStorageWriterMixin(BasePartitionedNode<PK,D,F,N> target){
		this.target = target;
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config){
		for(N node : IterableTool.nullSafe(target.getPhysicalNodes())){
			node.delete(lookup, config);
		}
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		for(N node : IterableTool.nullSafe(target.getPhysicalNodes())){
			node.deleteMultiUnique(uniqueKeys, config);
		}
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		for(N node : IterableTool.nullSafe(target.getPhysicalNodes())){
			node.deleteUnique(uniqueKey, config);
		}
	}
	
	
}
