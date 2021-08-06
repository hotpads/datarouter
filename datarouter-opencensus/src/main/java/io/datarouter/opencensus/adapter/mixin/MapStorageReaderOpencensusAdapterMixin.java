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
package io.datarouter.opencensus.adapter.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.opencensus.adapter.OpencensusAdapter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import io.opencensus.common.Scope;

public interface MapStorageReaderOpencensusAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D,F>>
extends MapStorageReader<PK,D>, OpencensusAdapter{

	N getBackingNode();

	@Override
	default boolean exists(PK key, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().exists(key, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	default D get(PK key, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().get(key, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().getMulti(keys, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config){
		Optional<Scope> span = startSpan();
		try{
			return getBackingNode().getKeys(keys, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

}
