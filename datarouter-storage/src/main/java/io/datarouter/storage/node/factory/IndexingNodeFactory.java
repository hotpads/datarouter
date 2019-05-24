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
package io.datarouter.storage.node.factory;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.multi.MultiIndexEntry;
import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.NoTxnManagedUniqueIndexNode;
import io.datarouter.storage.client.imp.TxnManagedUniqueIndexNode;
import io.datarouter.storage.node.op.combo.IndexedMapStorage;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.node.op.raw.index.IndexListener;
import io.datarouter.storage.node.type.index.IndexMapStorageWriterListener;
import io.datarouter.storage.node.type.index.ManagedUniqueIndexNode;
import io.datarouter.storage.node.type.index.ManualMultiIndexNode;
import io.datarouter.storage.node.type.index.ManualUniqueIndexNode;
import io.datarouter.storage.node.type.indexing.IndexingMapStorageNode;
import io.datarouter.storage.node.type.indexing.IndexingSortedMapStorageNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.lang.ReflectionTool;

public class IndexingNodeFactory{

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	IndexingMapStorageNode<PK,D,F,MapStorageNode<PK,D,F>> newMap(MapStorageNode<PK,D,F> mainNode){
		return new IndexingMapStorageNode<>(mainNode);
	}

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>,
			N extends SortedMapStorageNode<PK,D,F>>
	IndexingSortedMapStorageNode<PK, D, F, N> newSortedMap(N mainNode){
		return new IndexingSortedMapStorageNode<>(mainNode);
	}

	/*----------------------------- listener --------------------------------*/

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends UniqueIndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			IN extends SortedMapStorageNode<IK,IE,IF>>
	IndexListener<PK,D> newUniqueListener(Supplier<IE> indexEntrySupplier, IN indexNode){
		return new IndexMapStorageWriterListener<>(indexEntrySupplier, indexNode);
	}

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends MultiIndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			IN extends SortedMapStorageNode<IK,IE,IF>>
	IndexListener<PK,D> newMultiListener(Supplier<IE> indexEntrySupplier, IN indexNode){
		return new IndexMapStorageWriterListener<>(indexEntrySupplier, indexNode);
	}

	/*--------------------------- indexing node -----------------------------*/

	/*--------------------------- manual indexes ----------------------------*/

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IF extends DatabeanFielder<IK,IE>,
			IE extends UniqueIndexEntry<IK,IE,PK,D>>
	ManualUniqueIndexNode<PK,D,IK,IE> newManualUnique(MapStorage<PK,D> mainNode,
			SortedMapStorageNode<IK,IE,IF> indexNode){
		return new ManualUniqueIndexNode<>(mainNode, indexNode);
	}

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends MultiIndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	ManualMultiIndexNode<PK,D,IK,IE> newManualMulti(MapStorage<PK,D> mainNode,
			SortedMapStorageNode<IK,IE,IF> indexNode){
		return new ManualMultiIndexNode<>(mainNode, indexNode);
	}

	/*-------------------------- managed indexes ----------------------------*/

	/**
	 * WARNING: make sure the index fielder you pass in has the same character set and collation options as the backing
	 * node's fielder or risk having incorrect, performance-hurting introducers in SQL
	 */
	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends UniqueIndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	ManagedUniqueIndexNode<PK,D,IK,IE,IF> newManagedUnique(IndexedMapStorage<PK, D> backingNode,
			Supplier<IF> indexFielderSupplier, Supplier<IE> indexEntrySupplier, boolean manageTxn, String indexName){
		IndexEntryFieldInfo<IK,IE,IF> fieldInfo = new IndexEntryFieldInfo<>(indexName, indexEntrySupplier,
				indexFielderSupplier);
		if(manageTxn){
			return new TxnManagedUniqueIndexNode<>(backingNode, fieldInfo, indexName);
		}
		return new NoTxnManagedUniqueIndexNode<>(backingNode, fieldInfo, indexName);
	}

	/**
	 * WARNING: make sure the index fielder you pass in has the same character set and collation options as the backing
	 * node's fielder or risk having incorrect, performance-hurting introducers in SQL
	 */
	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends UniqueIndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	ManagedUniqueIndexNode<PK,D,IK,IE,IF> newManagedUnique(IndexedMapStorage<PK, D> backingNode,
			Class<IF> indexFielder, Class<IE> indexEntryClass, boolean manageTxn){
		return newManagedUnique(backingNode, ReflectionTool.supplier(indexFielder), ReflectionTool.supplier(
				indexEntryClass), manageTxn, indexEntryClass.getSimpleName());
	}

}
