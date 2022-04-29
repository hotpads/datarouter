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
package io.datarouter.storage.node.adapter.sanitization.mixin;

import java.util.Collection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
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
	static final Logger logger = LoggerFactory.getLogger(MapStorageSanitizationAdapterMixin.class);

	@Override
	default void put(D databean, Config config){
		Objects.requireNonNull(databean);
		Objects.requireNonNull(config);
		PrimaryKeySanitizer.checkForNullPrimaryKeyValues(databean.getKey());
		getBackingNode().put(databean, config);
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		Objects.requireNonNull(databeans);
		Objects.requireNonNull(config);
		if(databeans.isEmpty()){
			return;
		}
		Scanner.of(databeans)
				.map(D::getKey)
				.forEach(PrimaryKeySanitizer::checkForNullPrimaryKeyValues);
		getBackingNode().putMulti(databeans, config);
	}

	@Override
	default void delete(PK key, Config config){
		Objects.requireNonNull(key);
		Objects.requireNonNull(config);
		getBackingNode().delete(key, config);
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		Objects.requireNonNull(keys);
		Objects.requireNonNull(config);
		if(keys.isEmpty()){
			return;
		}
		getBackingNode().deleteMulti(keys, config);
	}

	@Override
	default void deleteAll(Config config){
		Objects.requireNonNull(config);
		getBackingNode().deleteAll(config);
	}

}
