/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.counter.CounterAdapter;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.op.raw.IndexedStorage.IndexedStorageNode;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.tuple.Range;

public interface IndexedStorageCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageNode<PK,D,F>>
extends IndexedStorage<PK,D>, CounterAdapter<PK,D,F,N>{

	//Reader

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageReader.OP_lookupUnique;
		getCounter().count(opName);
		D result = getBackingNode().lookupUnique(uniqueKey, config);
		String hitOrMiss = result != null ? "hit" : "miss";
		getCounter().count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		getCounter().count(opName);
		List<D> results = getBackingNode().lookupMultiUnique(uniqueKeys, config);
		int numAttempts = CollectionTool.size(uniqueKeys);
		int numHits = CollectionTool.size(results);
		int numMisses = numAttempts - numHits;
		getCounter().count(opName + " attempts", numAttempts);
		getCounter().count(opName + " hits", numHits);
		getCounter().count(opName + " misses", numMisses);
		return results;
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getFromIndex;
		getCounter().count(opName);
		List<IE> results = getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
		int numRows = CollectionTool.size(results);
		getCounter().count(opName + " rows", numRows);
		if(numRows == 0){
			getCounter().count(opName + " misses");
		}
		return results;
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getByIndex;
		getCounter().count(opName);
		List<D> results = getBackingNode().getMultiByIndex(keys, config, indexEntryFieldInfo);
		int numRows = CollectionTool.size(results);
		getCounter().count(opName + " rows", numRows);
		if(numRows == 0){
			getCounter().count(opName + " misses");
		}
		return results;
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanMultiIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		String opName = IndexedStorageReader.OP_scanIndex;
		getCounter().count(opName);
		return getBackingNode().scanMultiIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<D> scanMultiByIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		String opName = IndexedStorageReader.OP_scanByIndex;
		getCounter().count(opName);
		return getBackingNode().scanMultiByIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanMultiIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		String opName = IndexedStorageReader.OP_scanIndexKeys;
		getCounter().count(opName);
		return getBackingNode().scanMultiIndexKeys(indexEntryFieldInfo, ranges, config);
	}

	//Writer

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageWriter.OP_deleteUnique;
		getCounter().count(opName);
		getBackingNode().deleteUnique(uniqueKey, config);
	}

	@Override
	public default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		getCounter().count(opName);
		getBackingNode().deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		getCounter().count(OP_deleteByIndex);
		getBackingNode().deleteByIndex(keys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return getBackingNode().registerManaged(managedNode);
	}


	@Override
	default List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return getBackingNode().getManagedNodes();
	}

}
