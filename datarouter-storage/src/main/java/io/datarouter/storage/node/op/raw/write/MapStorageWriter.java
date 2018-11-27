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
package io.datarouter.storage.node.op.raw.write;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.type.physical.PhysicalNode;

/**
 * Methods for deleting from simple key/value storage systems, supporting similar methods to a HashMap.
 *
 * See MapStorageReader for implementation notes.
 */
public interface MapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends StorageWriter<PK,D>{

	public static final String OP_put = "put";
	public static final String OP_putMulti = "putMulti";
	public static final String OP_delete = "delete";
	public static final String OP_deleteMulti = "deleteMulti";
	public static final String OP_deleteAll = "deleteAll";


	void delete(PK key, Config config);
	void deleteMulti(Collection<PK> keys, Config config);
	void deleteAll(Config config);


	/*---------------------------- sub-interfaces ---------------------------*/

	public interface MapStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends StorageWriterNode<PK,D,F>, MapStorageWriter<PK,D>{
	}


	public interface PhysicalMapStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalNode<PK,D,F>, MapStorageWriterNode<PK,D,F>{
	}

}
