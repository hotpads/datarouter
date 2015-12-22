package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.BaseCallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class IndexedStorageReaderCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageReaderNode<PK,D>>
implements IndexedStorageReader<PK,D>{

	private BaseCallsiteAdapter<PK,D,F,N> adapterNode;
	private N backingNode;


	public IndexedStorageReaderCallsiteAdapterMixin(BaseCallsiteAdapter<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}


	/***************** IndexedSortedMapStorageReader ************************************/

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		D result = null;
		try{
			result = backingNode.lookupUnique(uniqueKey, config);
			return result;
		}finally{
			int numResults = result == null ? 0 : 1;
			adapterNode.recordCallsite(config, startNs, numResults);
		}
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = backingNode.lookupMultiUnique(uniqueKeys, config);
			return results;
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = backingNode.lookup(lookup, wildcardLastField, config);
			return results;
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	public List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = backingNode.lookupMulti(lookups, config);
			return results;
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getMultiFromIndex(keys, config, indexEntryFieldInfo);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, keys);
		}
	}

	@Override
	public <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getMultiByIndex(keys, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, keys);
		}
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.scanIndex(indexEntryFieldInfo, range, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		config = Config.nullSafe(config).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.scanIndexKeys(indexEntryFieldInfo, range, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
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
