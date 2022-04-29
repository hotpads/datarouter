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
package io.datarouter.storage.node.op.combo.writer;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;

public interface IndexedMapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorageWriter<PK,D>, IndexedStorageWriter<PK,D>{

	public interface IndexedMapStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends MapStorageWriterNode<PK,D,F>, IndexedStorageWriterNode<PK,D,F>{
	}

	public interface PhysicalIndexedMapStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalMapStorageWriterNode<PK,D,F>, PhysicalIndexedStorageWriterNode<PK,D,F>{
	}

}
