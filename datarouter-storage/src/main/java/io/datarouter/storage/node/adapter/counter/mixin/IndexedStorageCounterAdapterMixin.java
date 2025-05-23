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
package io.datarouter.storage.node.adapter.counter.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.counter.CounterAdapter;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.op.raw.IndexedStorage.IndexedStorageNode;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
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
		String indexName = uniqueKey.getClass().getSimpleName();
		countIndexUsage(indexName);
		getCounter().count(opName);
		getCounter().count(opName + " " + indexName);
		D result = getBackingNode().lookupUnique(uniqueKey, config);
		String hitOrMiss = result != null ? "hit" : "miss";
		getCounter().count(opName + " " + hitOrMiss);
		getCounter().count(opName + " " + indexName + " " + hitOrMiss);
		return result;
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(uniqueKeys == null || uniqueKeys.isEmpty()){
			return new ArrayList<>();
		}
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		getCounter().count(opName);
		uniqueKeys.forEach(uk -> {
			getCounter().count(opName + " " + uk.getClass().getSimpleName());
			countIndexUsage(uk.getClass().getSimpleName());
		});
		List<D> results = getBackingNode().lookupMultiUnique(uniqueKeys, config);
		int numAttempts = uniqueKeys.size();
		int numHits = results.size();
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
	List<IE> getMultiFromIndex(
			Collection<IK> indexKeys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getFromIndex;
		String indexPkName = getIndexPrimaryKeyClassName(indexEntryFieldInfo);
		String indexName = indexEntryFieldInfo.getIndexName();
		countIndexUsage(indexPkName);
		getCounter().count(opName);
		getCounter().count(opName + " " + indexName);
		getCounter().count(opName + " indexKeys", indexKeys.size());
		getCounter().count(opName + " " + indexName + " indexKeys", indexKeys.size());
		List<IE> results = getBackingNode().getMultiFromIndex(indexKeys, config, indexEntryFieldInfo);
		int numRows = results.size();
		int misses = Math.max(indexKeys.size() - numRows, 0);
		getCounter().count(opName + " hits", numRows);
		getCounter().count(opName + " " + indexName + " hits", numRows);
		getCounter().count(opName + " misses", misses);
		getCounter().count(opName + " " + indexName + " misses", misses);
		return results;
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(Collection<IK> indexKeys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getByIndex;
		String indexPkName = getIndexPrimaryKeyClassName(indexEntryFieldInfo);
		String indexName = indexEntryFieldInfo.getIndexName();
		getCounter().count(opName);
		countIndexUsage(indexPkName);
		getCounter().count(opName + " " + indexName);
		getCounter().count(opName + " indexKeys", indexKeys.size());
		getCounter().count(opName + " " + indexName + " indexKeys", indexKeys.size());
		List<D> results = getBackingNode().getMultiByIndex(indexKeys, config, indexEntryFieldInfo);
		int numRows = results.size();
		int misses = Math.max(indexKeys.size() - numRows, 0);
		getCounter().count(opName + " hits", numRows);
		getCounter().count(opName + " " + indexName + " hits", numRows);
		getCounter().count(opName + " misses", misses);
		getCounter().count(opName + " " + indexName + " misses", misses);
		return results;
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Scanner<IE> scanRangesIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		String opName = IndexedStorageReader.OP_scanIndex;
		String indexPkName = getIndexPrimaryKeyClassName(indexEntryFieldInfo);
		String indexName = indexEntryFieldInfo.getIndexName();
		getCounter().count(opName);
		countIndexUsage(indexPkName);
		getCounter().count(opName + " " + indexName);
		getCounter().count(opName + " ranges", ranges.size());
		getCounter().count(opName + " " + indexName + " ranges", ranges.size());
		return getBackingNode().scanRangesIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Scanner<D> scanRangesByIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		String opName = IndexedStorageReader.OP_scanByIndex;
		String indexPkName = getIndexPrimaryKeyClassName(indexEntryFieldInfo);
		String indexName = indexEntryFieldInfo.getIndexName();
		getCounter().count(opName);
		countIndexUsage(indexPkName);
		getCounter().count(opName + " " + indexName);
		getCounter().count(opName + " ranges", ranges.size());
		getCounter().count(opName + " " + indexName + " ranges", ranges.size());
		return getBackingNode().scanRangesByIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Scanner<IK> scanRangesIndexKeys(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		String opName = IndexedStorageReader.OP_scanIndexKeys;
		String indexPkName = getIndexPrimaryKeyClassName(indexEntryFieldInfo);
		String indexName = indexEntryFieldInfo.getIndexName();
		getCounter().count(opName);
		countIndexUsage(indexPkName);
		getCounter().count(opName + " " + indexName);
		getCounter().count(opName + " ranges", ranges.size());
		getCounter().count(opName + " " + indexName + " ranges", ranges.size());
		return getBackingNode().scanRangesIndexKeys(indexEntryFieldInfo, ranges, config);
	}

	//Writer

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		getCounter().count(IndexedStorageWriter.OP_deleteUnique);
		getCounter().count(IndexedStorageWriter.OP_deleteUnique + " " + uniqueKey.getClass().getSimpleName());
		countIndexUsage(uniqueKey.getClass().getSimpleName());
		getBackingNode().deleteUnique(uniqueKey, config);
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		getCounter().count(opName);
		uniqueKeys.forEach(uk -> {
			getCounter().count(opName + " " + uk.getClass().getSimpleName());
			countIndexUsage(uk.getClass().getSimpleName());
		});
		getCounter().count(opName + " uniqueKeys", uniqueKeys.size());
		getBackingNode().deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	void deleteByIndex(
			Collection<IK> indexKeys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = OP_deleteByIndex;
		String indexPkName = getIndexPrimaryKeyClassName(indexEntryFieldInfo);
		String indexName = indexEntryFieldInfo.getIndexName();
		getCounter().count(opName);
		countIndexUsage(indexPkName);
		getCounter().count(opName + " " + indexName);
		getCounter().count(opName + " indexKeys", indexKeys.size());
		getCounter().count(opName + " " + indexName + " indexKeys", indexKeys.size());
		getBackingNode().deleteByIndex(indexKeys, config, indexEntryFieldInfo);
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

	private <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	String getIndexPrimaryKeyClassName(IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return indexEntryFieldInfo.getPrimaryKeySupplier().get().getClass().getSimpleName();
	}

	private void countIndexUsage(String indexName){
		Metrics.count("Usage index " + indexName);
	}
}
