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
package io.datarouter.virtualnode.masterslave;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.virtualnode.masterslave.mixin.MasterSlaveIndexedStorageMixin;
import io.datarouter.virtualnode.masterslave.mixin.MasterSlaveMapStorageMixin;
import io.datarouter.virtualnode.masterslave.mixin.MasterSlaveSortedStorageMixin;

public class MasterSlaveIndexedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D,F>>
extends BaseMasterSlaveNode<PK,D,F,N>
implements MasterSlaveIndexedStorageMixin<PK,D,F,N>,
		MasterSlaveMapStorageMixin<PK,D,F,N>,
		MasterSlaveSortedStorageMixin<PK,D,F,N>,
		IndexedSortedMapStorageNode<PK,D,F>{

	public MasterSlaveIndexedSortedMapStorageNode(N master, Collection<N> slaves){
		super(master, slaves);
	}

}
