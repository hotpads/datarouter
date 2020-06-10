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
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
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

	public static final String OP_exists = "exists";
	public static final String OP_get = "get";
	public static final String OP_getMulti = "getMulti";
	public static final String OP_getKeys = "getKeys";


	boolean exists(PK key, Config config);

	default boolean exists(PK key){
		return exists(key, new Config());
	}

	List<PK> getKeys(Collection<PK> keys, Config config);

	default List<PK> getKeys(Collection<PK> keys){
		return getKeys(keys, new Config());
	}

	default Scanner<PK> scanMultiKeys(Collection<PK> keys, Config config){
		return Scanner.of(getKeys(keys, config));
	}

	default Scanner<PK> scanMultiKeys(Collection<PK> keys){
		return Scanner.of(getKeys(keys));
	}

	D get(PK key, Config config);

	default D get(PK key){
		return get(key, new Config());
	}

	List<D> getMulti(Collection<PK> keys, Config config);

	default List<D> getMulti(Collection<PK> keys){
		return getMulti(keys, new Config());
	}

	default Scanner<D> scanMulti(Collection<PK> keys, Config config){
		return Scanner.of(getMulti(keys, config));
	}

	default Scanner<D> scanMulti(Collection<PK> keys){
		return Scanner.of(getMulti(keys));
	}

	default Optional<D> find(PK key, Config config){
		return Optional.ofNullable(get(key, config));
	}

	default Optional<D> find(PK key){
		return find(key, new Config());
	}

	/*---------------------------- sub-interfaces ---------------------------*/

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
