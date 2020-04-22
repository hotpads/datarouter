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
package io.datarouter.virtualnode.caching;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import io.datarouter.util.collection.CollectionTool;

public class MapCachingMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageWriterNode<PK,D,F>>
implements MapStorageWriter<PK,D>{

	protected final BaseMapCachingNode<PK,D,F,N> target;
	protected final boolean cacheWrites;

	public MapCachingMapStorageWriterMixin(BaseMapCachingNode<PK,D,F,N> target, boolean cacheWrites){
		this.target = target;
		this.cacheWrites = cacheWrites;
	}

	@Override
	public void delete(PK key, Config config){
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			target.getCachingNode().delete(key, MapCachingMapStorageReaderNode.getEffectiveCachingNodeConfig(config));
			target.updateLastContact();
		}
		target.getBackingNode().delete(key, config);
	}

	@Override
	public void deleteAll(Config config){
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			target.getCachingNode().deleteAll(MapCachingMapStorageReaderNode.getEffectiveCachingNodeConfig(config));
			target.updateLastContact();
		}
		target.getBackingNode().deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			target.getCachingNode().deleteMulti(keys, MapCachingMapStorageReaderNode.getEffectiveCachingNodeConfig(
					config));
			target.updateLastContact();
		}
		target.getBackingNode().deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config){
		if(databean == null || databean.getKey() == null){
			return;
		}
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			Config effectiveCachingNodeConfig = MapCachingMapStorageReaderNode.getEffectiveCachingNodeConfig(config);
			if(cacheWrites){
				target.getCachingNode().put(databean, effectiveCachingNodeConfig);
			}else{//TODO check config for ignoring caching
				target.getCachingNode().delete(databean.getKey(), effectiveCachingNodeConfig);
			}
			target.updateLastContact();
		}
		target.getBackingNode().put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(CollectionTool.isEmpty(databeans)){
			return;
		}
		if(BaseMapCachingNode.useCache(config)){
			target.updateLastAttemptedContact();
			Config effectiveCachingNodeConfig = MapCachingMapStorageReaderNode.getEffectiveCachingNodeConfig(config);
			if(cacheWrites){
				target.getCachingNode().putMulti(databeans, effectiveCachingNodeConfig);
			}else{//TODO check config for ignoring caching
				Scanner.of(databeans)
						.map(Databean::getKey)
						.flush(keys -> target.getCachingNode().deleteMulti(keys, effectiveCachingNodeConfig));
			}
			target.updateLastContact();
		}
		target.getBackingNode().putMulti(databeans, config);
	}

}
