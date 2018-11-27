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
package io.datarouter.storage.node.adapter.counter.physical;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.entity.PhysicalSubEntitySortedMapStorageNode;

public class PhysicalSubEntitySortedMapStorageCounterAdapter<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>>
extends PhysicalSortedMapStorageCounterAdapter<PK,D,F,N>
implements PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>{

	public PhysicalSubEntitySortedMapStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public String getEntityNodePrefix(){
		return backingNode.getEntityNodePrefix();
	}

}
