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
package io.datarouter.storage.node.adapter.availability.mixin;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.exception.UnavailableException;
import io.datarouter.storage.node.op.raw.SortedStorage;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.util.tuple.Range;

public interface PhysicalSortedStorageAvailabilityAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSortedStorageNode<PK,D,F>>
extends SortedStorage<PK,D>{

	N getBackingNode();
	UnavailableException makeUnavailableException();

	@Override
	default Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		if(getBackingNode().getClient().getAvailability().read.getValue()){
			return getBackingNode().scanKeysMulti(ranges, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default Iterable<PK> scanKeys(Range<PK> range, Config config){
		if(getBackingNode().getClient().getAvailability().read.getValue()){
			return getBackingNode().scanKeys(range, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		if(getBackingNode().getClient().getAvailability().read.getValue()){
			return getBackingNode().scanMulti(ranges, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default Iterable<D> scan(Range<PK> range, Config config){
		if(getBackingNode().getClient().getAvailability().read.getValue()){
			return getBackingNode().scan(range, config);
		}
		throw makeUnavailableException();
	}

}
