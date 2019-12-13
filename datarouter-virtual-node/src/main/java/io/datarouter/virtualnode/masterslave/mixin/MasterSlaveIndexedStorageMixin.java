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
package io.datarouter.virtualnode.masterslave.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.op.raw.IndexedStorage.IndexedStorageNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.masterslave.MasterSlaveNode;

public interface MasterSlaveIndexedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageNode<PK,D,F>>
extends MasterSlaveNode<PK,D,F,N>, IndexedStorage<PK,D>{

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return chooseSlaveOrElseMaster(config).lookupUnique(uniqueKey, config);
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return chooseSlaveOrElseMaster(config).lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return chooseSlaveOrElseMaster(config).getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return chooseSlaveOrElseMaster(config).getMultiByIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanMultiIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return chooseSlaveOrElseMaster(config).scanMultiIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanMultiByIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return chooseSlaveOrElseMaster(config).scanMultiByIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanMultiIndexKeys(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return chooseSlaveOrElseMaster(config).scanMultiIndexKeys(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		getChildNodes().forEach(node -> node.registerManaged(managedNode));
		return managedNode;
	}

	@Override
	default List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return getMaster().getManagedNodes();
	}

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		getMaster().deleteUnique(uniqueKey, config);
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		getMaster().deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		getMaster().deleteByIndex(keys, config);
	}

}
