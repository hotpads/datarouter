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
package io.datarouter.storage.node.adapter.sanitization;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.sanitization.sanitizer.ScanSanitizer;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class PhysicalIndexedSortedMapStorageSanitizationAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedSortedMapStorageNode<PK,D,F>>
extends BaseSanitizationAdapter<PK,D,F,N>
implements PhysicalIndexedSortedMapStorageNode<PK,D,F>,
		PhysicalAdapterMixin<PK,D,F,N>{

	public PhysicalIndexedSortedMapStorageSanitizationAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public boolean exists(PK key, Config config){
		return getBackingNode().exists(key, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return getBackingNode().getKeys(keys, config);
	}

	@Override
	public D get(PK key, Config config){
		return getBackingNode().get(key, config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return getBackingNode().getMulti(keys, config);
	}

	@Override
	public void delete(PK key, Config config){
		getBackingNode().delete(key, config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		getBackingNode().deleteMulti(keys, config);
	}

	@Override
	public void deleteAll(Config config){
		getBackingNode().deleteAll(config);
	}

	@Override
	public void put(D databean, Config config){
		getBackingNode().put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		getBackingNode().putMulti(databeans, config);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return getBackingNode().lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return getBackingNode().lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,IE extends IndexEntry<IK,IE,PK,D>,IF extends DatabeanFielder<IK,IE>> List<IE>
			getMultiFromIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,IE extends IndexEntry<IK,IE,PK,D>,IF extends DatabeanFielder<IK,IE>> List<D>
			getMultiByIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return getBackingNode().getMultiByIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,IE extends IndexEntry<IK,IE,PK,D>,IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanRangesIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
					Config config){
		return getBackingNode().scanRangesIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,IE extends IndexEntry<IK,IE,PK,D>,IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanRangesByIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
					Config config){
		return getBackingNode().scanRangesByIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,IE extends IndexEntry<IK,IE,PK,D>,IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanRangesIndexKeys(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
					Config config){
		return getBackingNode().scanRangesIndexKeys(indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
			MN registerManaged(MN managedNode){
		return getBackingNode().registerManaged(managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return getBackingNode().getManagedNodes();
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		getBackingNode().deleteUnique(uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		getBackingNode().deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> void deleteByIndex(Collection<IK> keys, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		getBackingNode().deleteByIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		ScanSanitizer.rejectUnexceptedFullScan(ranges);
		return getBackingNode().scanRanges(ranges, config);
	}

	@Override
	public Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		ScanSanitizer.rejectUnexceptedFullScan(ranges);
		return getBackingNode().scanRangesKeys(ranges, config);
	}

	@Override
	public PhysicalDatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
