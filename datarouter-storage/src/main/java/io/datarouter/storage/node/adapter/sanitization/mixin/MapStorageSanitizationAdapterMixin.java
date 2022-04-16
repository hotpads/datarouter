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
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	static final Logger logger = LoggerFactory.getLogger(MapStorageSanitizationAdapterMixin.class);

	static final AtomicBoolean NULL_PUT_DATABEAN = new AtomicBoolean(false);
	static final AtomicBoolean NULL_PUT_CONFIG = new AtomicBoolean(false);

	static final AtomicBoolean NULL_PUT_MULTI_DATABEANS = new AtomicBoolean(false);
	static final AtomicBoolean NULL_PUT_MULTI_CONFIG = new AtomicBoolean(false);

	static final AtomicBoolean NULL_DELETE_PK = new AtomicBoolean(false);
	static final AtomicBoolean NULL_DELETE_CONFIG = new AtomicBoolean(false);

	static final AtomicBoolean NULL_DELETE_MULTI_PKS = new AtomicBoolean(false);
	static final AtomicBoolean NULL_DELETE_MULTI_CONFIG = new AtomicBoolean(false);

	static final AtomicBoolean NULL_DELETE_ALL_CONFIG = new AtomicBoolean(false);

	@Override
	default void put(D databean, Config config){
		if(databean == null && !NULL_PUT_DATABEAN.getAndSet(true)){
			logger.warn("NULL_PUT_DATABEAN", new Exception());
		}
		if(config == null && !NULL_PUT_CONFIG.getAndSet(true)){
			logger.warn("NULL_PUT_CONFIG", new Exception());
		}
		if(databean != null){
			PrimaryKeySanitizer.checkForNullPrimaryKeyValues(databean.getKey());
		}
		getBackingNode().put(databean, config);
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		if(databeans == null && !NULL_PUT_MULTI_DATABEANS.getAndSet(true)){
			logger.warn("NULL_PUT_MULTI_DATABEANS", new Exception());
		}
		if(config == null && !NULL_PUT_MULTI_CONFIG.getAndSet(true)){
			logger.warn("NULL_PUT_MULTI_CONFIG", new Exception());
		}
		databeans.stream()
				.map(D::getKey)
				.forEach(PrimaryKeySanitizer::checkForNullPrimaryKeyValues);
		getBackingNode().putMulti(databeans, config);
	}

	@Override
	default void delete(PK key, Config config){
		if(key == null && !NULL_DELETE_PK.getAndSet(true)){
			logger.warn("NULL_DELETE_PK", new Exception());
		}
		if(config == null && !NULL_DELETE_CONFIG.getAndSet(true)){
			logger.warn("NULL_DELETE_CONFIG", new Exception());
		}
		getBackingNode().delete(key, config);
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		if(keys == null && !NULL_DELETE_MULTI_PKS.getAndSet(true)){
			logger.warn("NULL_DELETE_MULTI_PKS", new Exception());
		}
		if(config == null && !NULL_DELETE_MULTI_CONFIG.getAndSet(true)){
			logger.warn("NULL_DELETE_MULTI_CONFIG", new Exception());
		}
		getBackingNode().deleteMulti(keys, config);
	}

	@Override
	default void deleteAll(Config config){
		if(config == null && !NULL_DELETE_ALL_CONFIG.getAndSet(true)){
			logger.warn("NULL_DELETE_ALL_CONFIG", new Exception());
		}
		getBackingNode().deleteAll(config);
	}

}
