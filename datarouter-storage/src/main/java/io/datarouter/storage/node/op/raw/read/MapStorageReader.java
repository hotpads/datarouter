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
package io.datarouter.storage.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.type.physical.PhysicalNode;

/**
 * Methods for reading from simple key/value storage systems, supporting similar methods to a HashMap.
 *
 * There are many possible implementations such as a HashMap, a Guava cache, Ehcache, Memcached, JDBC, Hibernate, HBase,
 * DynamoDB, S3, Google Cloud Storage, etc.
 */
public interface MapStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	public static final String
		OP_exists = "exists",
		OP_get = "get",
		OP_getMulti = "getMulti",
		OP_getKeys = "getKeys";


	boolean exists(PK key, Config config);
	List<PK> getKeys(final Collection<PK> keys, final Config config);

	D get(PK key, Config config);
	List<D> getMulti(Collection<PK> keys, Config config);


	/*************** sub-interfaces ***********************/

	public interface MapStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends Node<PK,D,F>, MapStorageReader<PK,D>{
	}


	public interface PhysicalMapStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalNode<PK,D,F>, MapStorageReaderNode<PK,D,F>{
	}
}
