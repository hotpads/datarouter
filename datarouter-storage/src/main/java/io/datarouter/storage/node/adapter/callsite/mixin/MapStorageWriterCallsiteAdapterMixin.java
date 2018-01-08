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
package io.datarouter.storage.node.adapter.callsite.mixin;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.callsite.CallsiteAdapter;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;

public interface MapStorageWriterCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D,F>>
extends MapStorageWriter<PK,D>, CallsiteAdapter{

	N getBackingNode();

	@Override
	public default void put(D databean, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().put(databean, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().putMulti(databeans, nullSafeConfig);
		}finally{
			recordCollectionCallsite(nullSafeConfig, startNs, databeans);
		}
	}

	@Override
	public default void delete(PK key, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().delete(key, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().deleteMulti(keys, nullSafeConfig);
		}finally{
			recordCollectionCallsite(nullSafeConfig, startNs, keys);
		}
	}

	@Override
	public default void deleteAll(Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().deleteAll(nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 0);
		}
	}

}
