package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.BaseCallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public class IndexedStorageWriterCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageWriterNode<PK,D>>
implements IndexedStorageWriter<PK,D>{
	
	private BaseCallsiteAdapter<PK,D,F,N> adapterNode;
	private N backingNode;

	
	public IndexedStorageWriterCallsiteAdapterMixin(BaseCallsiteAdapter<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}


	/***************** IndexedSortedMapStorage ************************************/

	@Override
	public void delete(Lookup<PK> lookup, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.delete(lookup, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.deleteUnique(uniqueKey, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.deleteMultiUnique(uniqueKeys, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, uniqueKeys);
		}
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.deleteByIndex(keys, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, keys);
		}
	}


	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return backingNode.registerManaged(managedNode);
	}


	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return backingNode.getManagedNodes();
	}
}
