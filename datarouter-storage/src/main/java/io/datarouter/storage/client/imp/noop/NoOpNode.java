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
package io.datarouter.storage.client.imp.noop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;

public class NoOpNode<PK extends PrimaryKey<PK>, D extends Databean<PK,D>> implements IndexedSortedMapStorage<PK,D>{

	@Override
	public boolean exists(PK key, Config config){
		return false;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return List.of();
	}

	@Override
	public D get(PK key, Config config){
		return null;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return List.of();
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
	public void deleteMulti(Collection<PK> keys, Config config){
	}

	@Override
	public void deleteAll(Config config){
	}

	@Override
	public Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		return Scanner.empty();
	}

	@Override
	public Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		return Scanner.empty();
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return null;
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return List.of();
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return List.of();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return List.of();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> void deleteByIndex(Collection<IK> keys, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanRangesIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return Scanner.empty();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanRangesByIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return Scanner.empty();
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanRangesIndexKeys(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		return Scanner.empty();
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
