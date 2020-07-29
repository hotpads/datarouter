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
package io.datarouter.virtualnode.replication;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.replication.mixin.ReplicationMapStorageMixin;
import io.datarouter.virtualnode.replication.mixin.ReplicationSortedStorageMixin;

public class ReplicationSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D,F>>
extends BaseReplicationNode<PK,D,F,N>
implements ReplicationSortedStorageMixin<PK,D,F,N>,
		ReplicationMapStorageMixin<PK,D,F,N>,
		SortedMapStorageNode<PK,D,F>{

	public ReplicationSortedMapStorageNode(N primary, Collection<N> replicas){
		super(primary, replicas);
	}

}
