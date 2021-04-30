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
package io.datarouter.opencensus.adapter.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.opencensus.adapter.OpencensusAdapter;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.op.raw.IndexedStorage.IndexedStorageNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;
import io.opencensus.trace.Span;

public interface IndexedStorageOpencensusAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageNode<PK,D,F>>
extends IndexedStorage<PK,D>, OpencensusAdapter{

	N getBackingNode();

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		Optional<Span> span = startSpan();
		try{
			getBackingNode().deleteUnique(uniqueKey, config);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		Optional<Span> span = startSpan();
		try{
			getBackingNode().deleteMultiUnique(uniqueKeys, config);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>> void deleteByIndex(Collection<IK> keys, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		Optional<Span> span = startSpan();
		try{
			getBackingNode().deleteByIndex(keys, config, indexEntryFieldInfo);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		Optional<Span> span = startSpan();
		try{
			return getBackingNode().lookupUnique(uniqueKey, config);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		Optional<Span> span = startSpan();
		try{
			return getBackingNode().lookupMultiUnique(uniqueKeys, config);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		Optional<Span> span = startSpan();
		try{
			return getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		Optional<Span> span = startSpan();
		try{
			return getBackingNode().getMultiByIndex(keys, config, indexEntryFieldInfo);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanRangesIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		Optional<Span> span = startSpan();
		try{
			return getBackingNode().scanRangesIndex(indexEntryFieldInfo, ranges, config);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanRangesByIndex(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		Optional<Span> span = startSpan();
		try{
			return getBackingNode().scanRangesByIndex(indexEntryFieldInfo, ranges, config);
		}finally{
			span.ifPresent(Span::end);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanRangesIndexKeys(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges,
			Config config){
		Optional<Span> span = startSpan();
		try{
			return getBackingNode().scanRangesIndexKeys(indexEntryFieldInfo, ranges, config);
		}finally{
			span.ifPresent(Span::end);
		}
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
