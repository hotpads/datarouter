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
package io.datarouter.storage.node.op.combo.reader;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;

public interface SortedMapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorageReader<PK,D>, SortedStorageReader<PK,D>{

	public interface SortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends MapStorageReaderNode<PK,D,F>, SortedStorageReaderNode<PK,D,F>, SortedMapStorageReader<PK,D>{
	}

	public interface PhysicalSortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalMapStorageReaderNode<PK,D,F>, PhysicalSortedStorageReaderNode<PK,D,F>,
			SortedMapStorageReaderNode<PK,D,F>{
	}
}
