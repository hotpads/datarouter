/*
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
package io.datarouter.storage.node.adapter.sanitization.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.sanitization.SanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.sanitizer.ScanSanitizer;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.op.raw.IndexedStorage.IndexedStorageNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;

public interface IndexedStorageSanitizationAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageNode<PK,D,F>>
extends IndexedStorage<PK,D>, SanitizationAdapter<PK,D,F,N>{

	/*------------- read -------------*/

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		Objects.requireNonNull(uniqueKey);
		Objects.requireNonNull(config);
		return getBackingNode().lookupUnique(uniqueKey, config);
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		Objects.requireNonNull(uniqueKeys);
		Objects.requireNonNull(config);
		return getBackingNode().lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		Objects.requireNonNull(keys);
		Objects.requireNonNull(config);
		Objects.requireNonNull(indexEntryFieldInfo);
		keys.forEach(Objects::requireNonNull);
		return getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		Objects.requireNonNull(keys);
		Objects.requireNonNull(config);
		Objects.requireNonNull(indexEntryFieldInfo);
		keys.forEach(Objects::requireNonNull);
		return getBackingNode().getMultiByIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanRangesIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		Objects.requireNonNull(indexEntryFieldInfo);
		Objects.requireNonNull(ranges);
		Objects.requireNonNull(config);
		ranges.forEach(Objects::requireNonNull);
		ranges.forEach(ScanSanitizer::rejectUnexpectedFullScan);
		return getBackingNode().scanRangesIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanRangesByIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		Objects.requireNonNull(indexEntryFieldInfo);
		Objects.requireNonNull(ranges);
		Objects.requireNonNull(config);
		ranges.forEach(Objects::requireNonNull);
		ranges.forEach(ScanSanitizer::rejectUnexpectedFullScan);
		return getBackingNode().scanRangesByIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanRangesIndexKeys(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		Objects.requireNonNull(indexEntryFieldInfo);
		Objects.requireNonNull(ranges);
		Objects.requireNonNull(config);
		ranges.forEach(Objects::requireNonNull);
		ranges.forEach(ScanSanitizer::rejectUnexpectedFullScan);
		return getBackingNode().scanRangesIndexKeys(indexEntryFieldInfo, ranges, config);
	}

	/*------------- write -------------*/

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		Objects.requireNonNull(uniqueKey);
		Objects.requireNonNull(config);
		getBackingNode().deleteUnique(uniqueKey, config);
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		Objects.requireNonNull(uniqueKeys);
		Objects.requireNonNull(config);
		uniqueKeys.forEach(Objects::requireNonNull);
		getBackingNode().deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	void deleteByIndex(
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		Objects.requireNonNull(keys);
		Objects.requireNonNull(config);
		Objects.requireNonNull(indexEntryFieldInfo);
		keys.forEach(Objects::requireNonNull);
		getBackingNode().deleteByIndex(keys, config, indexEntryFieldInfo);
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
