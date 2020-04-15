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
package io.datarouter.storage.node.adapter.trace.physical;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.trace.TallyStorageWriterTraceAdapter;
import io.datarouter.storage.node.adapter.trace.mixin.MapStorageTraceAdapterMixin;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class PhysicalTallyStorageTraceAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalTallyStorageNode<PK,D,F>>
extends TallyStorageWriterTraceAdapter<PK,D,F,N>
implements PhysicalTallyStorageNode<PK,D,F>,
		PhysicalAdapterMixin<PK,D,F,N>,
		MapStorageTraceAdapterMixin<PK,D,F,N>{

	public PhysicalTallyStorageTraceAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public PhysicalDatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		try(var $ = startSpanForOp(OP_incrementAndGetCount)){
			return getBackingNode().incrementAndGetCount(key, delta, config);
		}
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		try(var $ = startSpanForOp(OP_findTallyCount)){
			return getBackingNode().findTallyCount(key, config);
		}
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		try(var $ = startSpanForOp(OP_getMultiTallyCount)){
			return getBackingNode().getMultiTallyCount(keys, config);
		}
	}

}
