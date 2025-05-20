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
package io.datarouter.storage.node.adapter.trace.mixin;

import java.util.Collection;

import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;

public interface MapStorageTraceAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D,F>>
extends MapStorage<PK,D>, MapStorageReaderTraceAdapterMixin<PK,D,F,N>{

	//Writer

	@Override
	public default void put(D databean, Config config){
		try(var _ = startSpanForOp(OP_put)){
			getBackingNode().put(databean, config);
		}
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config){
		try(var _ = startSpanForOp(OP_putMulti)){
			getBackingNode().putMulti(databeans, config);
			TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder().databeans(databeans.size()));
		}
	}

	@Override
	public default void delete(PK key, Config config){
		try(var _ = startSpanForOp(OP_delete)){
			getBackingNode().delete(key, config);
		}
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config){
		try(var _ = startSpanForOp(OP_deleteMulti)){
			getBackingNode().deleteMulti(keys, config);
			TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder().keys(keys.size()));
		}
	}

	@Override
	public default void deleteAll(Config config){
		try(var _ = startSpanForOp(OP_deleteAll)){
			getBackingNode().deleteAll(config);
		}
	}

}
