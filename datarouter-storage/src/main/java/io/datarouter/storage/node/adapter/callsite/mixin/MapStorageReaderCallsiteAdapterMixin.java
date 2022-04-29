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
package io.datarouter.storage.node.adapter.callsite.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.callsite.CallsiteAdapter;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import io.datarouter.util.lang.LineOfCode;

public interface MapStorageReaderCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D,F>>
extends MapStorageReader<PK,D>, CallsiteAdapter{

	N getBackingNode();

	@Override
	default boolean exists(PK key, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		boolean result = false;
		try{
			result = getBackingNode().exists(key, config);
			return result;
		}finally{
			int numResults = result ? 1 : 0;
			recordCallsite(lineOfCode, startNs, numResults);
		}
	}

	@Override
	default D get(PK key, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		D result = null;
		try{
			result = getBackingNode().get(key, config);
			return result;
		}finally{
			int numResults = result == null ? 0 : 1;
			recordCallsite(lineOfCode, startNs, numResults);
		}
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = getBackingNode().getMulti(keys, config);
			return results;
		}finally{
			recordCollectionCallsite(lineOfCode, startNs, results);
		}
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config){
		LineOfCode lineOfCode = getCallsite();
		long startNs = System.nanoTime();
		List<PK> results = null;
		try{
			results = getBackingNode().getKeys(keys, config);
			return results;
		}finally{
			recordCollectionCallsite(lineOfCode, startNs, results);
		}
	}

}
