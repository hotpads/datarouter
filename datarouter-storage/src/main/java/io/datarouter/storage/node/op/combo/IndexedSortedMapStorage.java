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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.combo.reader.IndexedSortedMapStorageReader;
import io.datarouter.storage.node.op.combo.writer.IndexedSortedMapStorageWriter;

public interface IndexedSortedMapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends IndexedMapStorage<PK,D>,
		SortedMapStorage<PK,D>,
		IndexedSortedMapStorageReader<PK,D>,
		IndexedSortedMapStorageWriter<PK,D>{

	public interface IndexedSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends IndexedSortedMapStorage<PK,D>,
			IndexedMapStorageNode<PK,D,F>,
			SortedMapStorageNode<PK,D,F>,
			IndexedSortedMapStorageReaderNode<PK,D,F>,
			IndexedSortedMapStorageWriterNode<PK,D,F>{
	}
	public interface PhysicalIndexedSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends IndexedSortedMapStorageNode<PK,D,F>,
			PhysicalIndexedMapStorageNode<PK,D,F>,
			PhysicalSortedMapStorageNode<PK,D,F>,
			PhysicalIndexedSortedMapStorageReaderNode<PK,D,F>,
			PhysicalIndexedSortedMapStorageWriterNode<PK,D,F>{
	}
}
