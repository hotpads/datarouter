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
import java.util.Map;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public interface TallyStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends MapStorageReader<PK,D>{

	public static final String OP_findTallyCount = "findTallyCount";
	public static final String OP_getMultiTallyCount = "getMultiTallyCount";

	Optional<Long> findTallyCount(String key, Config config);

	default Optional<Long> findTallyCount(String key){
		return findTallyCount(key, new Config());
	}

	Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config);

	default Map<String,Long> getMultiTallyCount(Collection<String> keys){
		return getMultiTallyCount(keys, new Config());
	}

	public interface TallyStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends Node<PK,D,F>, MapStorageReader<PK,D>{
	}

	public interface PhysicalTallyStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalNode<PK,D,F>, MapStorageReaderNode<PK,D,F>{
	}

}
