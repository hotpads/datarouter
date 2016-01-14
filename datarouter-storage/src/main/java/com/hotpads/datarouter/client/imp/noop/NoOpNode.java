package com.hotpads.datarouter.client.imp.noop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class NoOpNode<PK extends PrimaryKey<PK>, D extends Databean<PK, D>> implements IndexedSortedMapStorage<PK, D>{

	@Override
	public boolean exists(PK key, Config config){
		return false;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return Collections.emptyList();
	}

	@Override
	public D get(PK key, Config config){
		return null;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return Collections.emptyList();
	}

	@Override
	public void put(D databean, Config config){

	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){

	}

	@Override
	public void delete(PK key, Config config){

	}

	@Override
	public void delete(Lookup<PK> lookup, Config config){

	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){

	}

	@Override
	public void deleteAll(Config config){

	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return Collections.emptyList();
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		return Collections.emptyList();
	}

	@Override
	public Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return new EmptySortedScannerIterable<>();
	}

	@Override
	public Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return new EmptySortedScannerIterable<>();
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){

	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return null;
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return Collections.emptyList();
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return Collections.emptyList();
	}

	@Override
	public List<D> lookupMulti(Collection<? extends Lookup<PK>> lookup, Config config){
		return Collections.emptyList();
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){

	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){

	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		return Collections.emptyList();
	}

	@Override
	public <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		return Collections.emptyList();
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){

	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		return new EmptySortedScannerIterable<>();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		return new EmptySortedScannerIterable<>();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			N extends ManagedNode<PK,D,IK,IE,IF>>
	N registerManaged(N managedNode){
		return managedNode;
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return new ArrayList<>();
	}
}
