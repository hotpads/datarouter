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
package io.datarouter.storage.node.type.index;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;

public class ManagedNodesHolder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>{

	private List<ManagedNode<PK,D,?,?,?>> managedNodes;

	public ManagedNodesHolder(){
		this.managedNodes = new ArrayList<>();
	}

	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return managedNodes;
	}

	public <N extends ManagedNode<PK,D,?,?,?>> N registerManagedNode(N node){
		managedNodes.add(node);
		return node;
	}

}
