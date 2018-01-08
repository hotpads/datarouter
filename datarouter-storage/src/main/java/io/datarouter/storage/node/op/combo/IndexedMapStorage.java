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
import io.datarouter.storage.node.op.combo.reader.IndexedMapStorageReader;
import io.datarouter.storage.node.op.combo.writer.IndexedMapStorageWriter;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.op.raw.MapStorage;

public interface IndexedMapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorage<PK,D>, IndexedStorage<PK,D>,
		IndexedMapStorageReader<PK,D>, IndexedMapStorageWriter<PK,D>{
	public interface IndexedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends IndexedMapStorage<PK,D>,
			MapStorageNode<PK,D,F>, IndexedStorageNode<PK,D,F>,
			IndexedMapStorageReaderNode<PK,D,F>, IndexedMapStorageWriterNode<PK,D,F>{
	}
	public interface PhysicalIndexedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends IndexedMapStorageNode<PK,D,F>,
			PhysicalMapStorageNode<PK,D,F>, PhysicalIndexedStorageNode<PK,D,F>,
			PhysicalIndexedMapStorageReaderNode<PK,D,F>, PhysicalIndexedMapStorageWriterNode<PK,D,F>{
	}
}
