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
package io.datarouter.virtualnode.redundant;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.base.BaseRedundantNode;
import io.datarouter.virtualnode.redundant.mixin.RedundantMapStorageMixin;
import io.datarouter.virtualnode.redundant.mixin.RedundantSortedStorageMixin;

public class RedundantSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D,F>>
extends BaseRedundantNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D,F>,
		RedundantMapStorageMixin<PK,D,F,N>,
		RedundantSortedStorageMixin<PK,D,F,N>{

	/**
	 * @param nodes reads go to first node while writes go to all nodes
	 */
	private RedundantSortedMapStorageNode(List<N> nodes){
		super(nodes, nodes.getFirst());
	}

	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends SortedMapStorageNode<PK,D,F>>
	SortedMapStorageNode<PK,D,F> makeIfMulti(List<N> nodes){
		if(nodes.size() == 1){
			return nodes.getFirst();
		}
		return new RedundantSortedMapStorageNode<>(nodes);
	}

	@SafeVarargs
	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends SortedMapStorageNode<PK,D,F>>
	SortedMapStorageNode<PK,D,F> makeIfMulti(N... nodes){
		return makeIfMulti(List.of(nodes));
	}

}
