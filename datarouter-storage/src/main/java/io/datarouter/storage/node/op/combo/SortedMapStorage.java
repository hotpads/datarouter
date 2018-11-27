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
package io.datarouter.storage.node.op.combo;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.reader.SortedMapStorageReader;
import io.datarouter.storage.node.op.combo.writer.SortedMapStorageWriter;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.SortedStorage;
import io.datarouter.util.iterable.BatchingIterable;

public interface SortedMapStorage<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends MapStorage<PK,D>,
		SortedStorage<PK,D>,
		SortedMapStorageReader<PK,D>,
		SortedMapStorageWriter<PK,D>{

	static final int DELETE_BATCH_SIZE = 100;

	default void deleteWithPrefix(PK prefix, Config config){
		for(List<PK> keys : new BatchingIterable<>(scanKeysWithPrefix(prefix, config), DELETE_BATCH_SIZE)){
			deleteMulti(keys, config);
		}
	}

	default void deleteWithPrefixes(Collection<PK> prefixes, Config config){
		for(List<PK> keys : new BatchingIterable<>(scanKeysWithPrefixes(prefixes, config), DELETE_BATCH_SIZE)){
			deleteMulti(keys, config);
		}
	}

	public interface SortedMapStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends SortedMapStorage<PK,D>,
			MapStorageNode<PK,D,F>,
			SortedStorageNode<PK,D,F>,
			SortedMapStorageReaderNode<PK,D,F>,
			SortedMapStorageWriterNode<PK,D,F>{
	}

	public interface PhysicalSortedMapStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends SortedMapStorageNode<PK,D,F>,
			PhysicalMapStorageNode<PK,D,F>,
			PhysicalSortedStorageNode<PK,D,F>,
			PhysicalSortedMapStorageReaderNode<PK,D,F>,
			PhysicalSortedMapStorageWriterNode<PK,D,F>{
	}

}
