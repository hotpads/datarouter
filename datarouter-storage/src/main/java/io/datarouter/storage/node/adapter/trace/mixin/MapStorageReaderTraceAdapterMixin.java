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
package io.datarouter.storage.node.adapter.trace.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.instrumentation.trace.TraceSpanFinisher;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.trace.TraceAdapter;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader.MapStorageReaderNode;

public interface MapStorageReaderTraceAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D,F>>
extends MapStorageReader<PK,D>, TraceAdapter<PK,D,F,N>{

	@Override
	public default boolean exists(PK key, Config config){
		try(TraceSpanFinisher finisher = startSpanForOp(OP_exists)){
			boolean result = getBackingNode().exists(key, config);
			TracerTool.appendToSpanInfo(result ? "hit" : "miss");
			return result;
		}
	}

	@Override
	public default D get(PK key, Config config){
		try(TraceSpanFinisher finisher = startSpanForOp(OP_get)){
			D result = getBackingNode().get(key, config);
			TracerTool.appendToSpanInfo(result != null ? "hit" : "miss");
			return result;
		}
	}

	@Override
	public default List<D> getMulti(Collection<PK> keys, Config config){
		try(TraceSpanFinisher finisher = startSpanForOp(OP_getMulti)){
			List<D> results = getBackingNode().getMulti(keys, config);
			TracerTool.appendToSpanInfo(String.format("got %d/%d", results.size(), keys.size()));
			return results;
		}
	}

	@Override
	public default List<PK> getKeys(Collection<PK> keys, Config config){
		try(TraceSpanFinisher finisher = startSpanForOp(OP_getKeys)){
			List<PK> results = getBackingNode().getKeys(keys, config);
			TracerTool.appendToSpanInfo(String.format("got %d/%d", results.size(), keys.size()));
			return results;
		}
	}

}
