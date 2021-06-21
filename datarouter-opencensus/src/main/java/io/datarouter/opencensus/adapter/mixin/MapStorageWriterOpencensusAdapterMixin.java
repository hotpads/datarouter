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
package io.datarouter.opencensus.adapter.mixin;

import java.util.Collection;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.opencensus.adapter.OpencensusAdapter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;
import io.opencensus.common.Scope;

public interface MapStorageWriterOpencensusAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D,F>>
extends MapStorageWriter<PK,D>, OpencensusAdapter{

	N getBackingNode();

	@Override
	public default void put(D databean, Config config){
		Optional<Scope> span = startSpan();
		try{
			getBackingNode().put(databean, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config){
		Optional<Scope> span = startSpan();
		try{
			getBackingNode().putMulti(databeans, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	public default void delete(PK key, Config config){
		Optional<Scope> span = startSpan();
		try{
			getBackingNode().delete(key, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config){
		Optional<Scope> span = startSpan();
		try{
			getBackingNode().deleteMulti(keys, config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

	@Override
	public default void deleteAll(Config config){
		Optional<Scope> span = startSpan();
		try{
			getBackingNode().deleteAll(config);
		}finally{
			span.ifPresent(Scope::close);
		}
	}

}
