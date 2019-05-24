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
package io.datarouter.storage.node.adapter.sanitization.mixin;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.sanitization.sanitizer.PrimaryKeySanitizer;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;

public interface MapStorageSanitizationAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D,F>>
extends MapStorage<PK,D>, MapStorageReaderSanitizationAdapterMixin<PK,D,F,N>{

	@Override
	default void put(D databean, Config config){
		if(databean != null){
			PrimaryKeySanitizer.checkForNullPrimaryKeyValues(databean.getKey());
		}
		getBackingNode().put(databean, Config.nullSafe(config));
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		databeans.stream()
				.map(D::getKey)
				.forEach(PrimaryKeySanitizer::checkForNullPrimaryKeyValues);
		getBackingNode().putMulti(databeans, Config.nullSafe(config));
	}

	@Override
	default void delete(PK key, Config config){
		getBackingNode().delete(key, Config.nullSafe(config));
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		getBackingNode().deleteMulti(keys, Config.nullSafe(config));
	}

	@Override
	default void deleteAll(Config config){
		getBackingNode().deleteAll(config);
	}

}
