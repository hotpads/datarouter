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
package io.datarouter.storage.node.adapter.availability.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.setting.impl.DatarouterClientAvailabilitySettings.AvailabilitySettingNode;
import io.datarouter.storage.exception.UnavailableException;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;

public interface PhysicalMapStorageAvailabilityAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalMapStorageNode<PK,D,F>>
extends MapStorage<PK,D>{

	N getBackingNode();
	AvailabilitySettingNode getAvailability();
	UnavailableException makeUnavailableException();

	@Override
	default boolean exists(PK key, Config config){
		if(getAvailability().read.get()){
			return getBackingNode().exists(key, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config){
		if(getAvailability().read.get()){
			return getBackingNode().getKeys(keys, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default D get(PK key, Config config){
		if(getAvailability().read.get()){
			return getBackingNode().get(key, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config){
		if(getAvailability().read.get()){
			return getBackingNode().getMulti(keys, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default void delete(PK key, Config config){
		if(getAvailability().write.get()){
			getBackingNode().delete(key, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		if(getAvailability().write.get()){
			getBackingNode().deleteMulti(keys, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteAll(Config config){
		if(getAvailability().write.get()){
			getBackingNode().deleteAll(config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void put(D databean, Config config){
		if(getAvailability().write.get()){
			getBackingNode().put(databean, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		if(getAvailability().write.get()){
			getBackingNode().putMulti(databeans, config);
			return;
		}
		throw makeUnavailableException();
	}

}
