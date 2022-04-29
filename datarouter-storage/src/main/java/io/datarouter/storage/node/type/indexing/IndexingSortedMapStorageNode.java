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
package io.datarouter.storage.node.type.indexing;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.type.indexing.base.BaseIndexingNode;
import io.datarouter.storage.node.type.indexing.mixin.IndexingMapStorageMixin;
import io.datarouter.storage.node.type.indexing.mixin.IndexingSortedStorageMixin;

public class IndexingSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D,F>>
extends BaseIndexingNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D,F>,
		IndexingMapStorageMixin<PK,D,F,N>,
		IndexingSortedStorageMixin<PK,D,F,N>{

	public IndexingSortedMapStorageNode(N mainNode){
		super(mainNode);// mainNode must have explicit Fielder
	}

}
