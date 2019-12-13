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
package io.datarouter.virtualnode.masterslave.mixin;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.SortedStorage;
import io.datarouter.storage.node.op.raw.SortedStorage.SortedStorageNode;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.masterslave.MasterSlaveNode;

public interface MasterSlaveSortedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageNode<PK,D,F>>
extends MasterSlaveNode<PK,D,F,N>, SortedStorage<PK,D>{

	@Override
	default Scanner<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return chooseSlaveOrElseMaster(config).scanKeysMulti(ranges, config);
	}

	@Override
	default Scanner<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return chooseSlaveOrElseMaster(config).scanMulti(ranges, config);
	}

}
