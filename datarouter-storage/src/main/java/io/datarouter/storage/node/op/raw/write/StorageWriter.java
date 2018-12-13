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
import java.util.stream.Stream;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.util.StreamTool;

/**
 * Methods for writing to any storage system.
 */
public interface StorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	public static final String OP_put = "put";
	public static final String OP_putMulti = "putMulti";

	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);

	default void putStream(Stream<D> databeans, Config config){
		int iterateBatchSize = Config.nullSafe(config).getIterateBatchSize();
		StreamTool.batch(databeans, iterateBatchSize)
				.forEach(batch -> putMulti(batch, config));
	}

	public interface StorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends Node<PK,D,F>,StorageWriter<PK,D>{
	}

}
