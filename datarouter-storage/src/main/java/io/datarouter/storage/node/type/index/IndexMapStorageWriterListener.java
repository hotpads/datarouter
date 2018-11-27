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
package io.datarouter.storage.node.type.index;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.index.KeyIndexEntry;
import io.datarouter.model.index.unique.UniqueKeyIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.op.raw.index.IndexListener;
import io.datarouter.storage.node.type.index.base.BaseIndexNode;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;

/**
 * This assumes that only PK fields are changed... It has no way of detecting changes in non primary key fields.
 * Also fine for cases where you never delete or modify records.
 */
public class IndexMapStorageWriterListener<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>,
		IN extends SortedMapStorageNode<IK,IE,IF>>
extends BaseIndexNode<PK,D,IK,IE,IF,IN>
implements IndexListener<PK,D>{

	public IndexMapStorageWriterListener(Supplier<IE> indexEntrySupplier, IN indexNode){
		super(indexEntrySupplier, indexNode);//indexNode must have explicit Fielder
	}

	//TODO is passing the configs straight through the right thing to do?

	/*------------------------------ writing --------------------------------*/

	@Override
	public void onDelete(PK key, Config config){
		if(key == null){
			throw new IllegalArgumentException("invalid null key");
		}
		IE indexEntry = createIndexEntry();
		if(indexEntry instanceof KeyIndexEntry){
			((KeyIndexEntry<IK,IE,PK,D>)indexEntry).fromPrimaryKey(key);
			indexNode.delete(indexEntry.getKey(), config);
		}else{
			throw new IllegalArgumentException("Unable to find index from PK, please call "
					+ "deleteDatabean method instead");
		}
	}

	@Override
	public void onDeleteDatabean(D databean, Config config){
		IE sampleIndexEntry = createIndexEntry();
		List<IE> indexEntriesFromSingleDatabean = sampleIndexEntry.createFromDatabean(databean);
		indexNode.deleteMulti(DatabeanTool.getKeys(indexEntriesFromSingleDatabean), config);
	}

	@Override
	public void onDeleteAll(Config config){
		indexNode.deleteAll(config);
	}

	@Override
	public void onDeleteMulti(Collection<PK> keys, Config config){
		if(CollectionTool.nullSafe(keys).contains(null)){
			throw new IllegalArgumentException("invalid null key");
		}
		List<IE> indexEntries = getIndexEntriesFromPrimaryKeys(keys);
		indexNode.deleteMulti(DatabeanTool.getKeys(indexEntries), config);
	}

	@Override
	public void onDeleteMultiDatabeans(Collection<D> databeans, Config config){
		List<IE> indexEntries = getIndexEntriesFromDatabeans(databeans);
		indexNode.deleteMulti(DatabeanTool.getKeys(indexEntries), config);
	}

	@Override
	public void onPut(D databean, Config config){
		if(databean == null){
			throw new IllegalArgumentException("invalid null databean");
		}
		IE sampleIndexEntry = createIndexEntry();
		List<IE> indexEntries = sampleIndexEntry.createFromDatabean(databean);
		indexNode.putMulti(indexEntries, config);
	}

	@Override
	public void onPutMulti(Collection<D> databeans, Config config){
		if(CollectionTool.nullSafe(databeans).contains(null)){
			throw new IllegalArgumentException("invalid null databean");
		}
		List<IE> indexEntries = getIndexEntriesFromDatabeans(databeans);
		indexNode.putMulti(indexEntries, config);
	}

	/*------------------------------ helper ---------------------------------*/

	private List<IE> getIndexEntriesFromPrimaryKeys(Collection<PK> primaryKeys){
		List<IE> indexEntries = ListTool.createArrayListWithSize(primaryKeys);
		for(PK key : IterableTool.nullSafe(primaryKeys)){
			IE indexEntry = createIndexEntry();
			if(indexEntry instanceof UniqueKeyIndexEntry){
				((UniqueKeyIndexEntry<IK,IE,PK,D>)indexEntry).fromPrimaryKey(key);
				indexEntries.add(indexEntry);
			}else{
				throw new IllegalArgumentException("Unable to find index from PK, please call "
						+ "deleteMultiDatabeans method instead");
			}
		}
		return indexEntries;
	}

	private List<IE> getIndexEntriesFromDatabeans(Collection<D> databeans){
		IE sampleIndexEntry = createIndexEntry();
		List<IE> indexEntries = ListTool.createArrayListWithSize(databeans);
		for(D databean : IterableTool.nullSafe(databeans)){
			List<IE> indexEntriesFromSingleDatabean = sampleIndexEntry.createFromDatabean(databean);
			indexEntries.addAll(CollectionTool.nullSafe(indexEntriesFromSingleDatabean));
		}
		return indexEntries;
	}

}
