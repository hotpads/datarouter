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
package io.datarouter.storage.node.adapter.availability.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.setting.impl.DatarouterClientAvailabilitySettings.AvailabilitySettingNode;
import io.datarouter.storage.exception.UnavailableException;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.op.raw.IndexedStorage.PhysicalIndexedStorageNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public interface PhysicalIndexedStorageAvailabilityAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedStorageNode<PK,D,F>>
extends IndexedStorage<PK,D>{

	N getBackingNode();
	AvailabilitySettingNode getAvailability();
	UnavailableException makeUnavailableException();

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		if(getAvailability().read.get()){
			return getBackingNode().lookupUnique(uniqueKey, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(getAvailability().read.get()){
			return getBackingNode().lookupMultiUnique(uniqueKeys, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		if(getAvailability().read.get()){
			return getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		if(getAvailability().read.get()){
			return getBackingNode().getMultiByIndex(keys, config, indexEntryFieldInfo);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanMultiIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		if(getAvailability().read.get()){
			return getBackingNode().scanMultiIndex(indexEntryFieldInfo, ranges, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<D> scanMultiByIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		if(getAvailability().read.get()){
			return getBackingNode().scanMultiByIndex(indexEntryFieldInfo, ranges, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanMultiIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		if(getAvailability().read.get()){
			return getBackingNode().scanMultiIndexKeys(indexEntryFieldInfo, ranges, config);
		}
		throw makeUnavailableException();
	}

	//Writer

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		if(getAvailability().write.get()){
			getBackingNode().deleteUnique(uniqueKey, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(getAvailability().write.get()){
			getBackingNode().deleteMultiUnique(uniqueKeys, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		if(getAvailability().write.get()){
			getBackingNode().deleteByIndex(keys, config);
			return;
		}
		throw makeUnavailableException();
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
