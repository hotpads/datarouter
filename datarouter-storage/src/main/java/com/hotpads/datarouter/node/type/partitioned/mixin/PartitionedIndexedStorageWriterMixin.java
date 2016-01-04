package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.PhysicalIndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrIterableTool;

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
		for(N node : DrIterableTool.nullSafe(target.getPhysicalNodes())){
			node.delete(lookup, config);
		}
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		for(N node : DrIterableTool.nullSafe(target.getPhysicalNodes())){
			node.deleteMultiUnique(uniqueKeys, config);
		}
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		for(N node : DrIterableTool.nullSafe(target.getPhysicalNodes())){
			node.deleteUnique(uniqueKey, config);
		}
	}
	
	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		for(N node : DrIterableTool.nullSafe(target.getPhysicalNodes())){
			node.deleteByIndex(keys, config);
		}
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		for(N node : DrIterableTool.nullSafe(target.getPhysicalNodes())){
			node.registerManaged(managedNode);
		}
		return managedNode;
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		for(N node : DrIterableTool.nullSafe(target.getPhysicalNodes())){
			return node.getManagedNodes();
		}
		return new ArrayList<>();
	}
}
