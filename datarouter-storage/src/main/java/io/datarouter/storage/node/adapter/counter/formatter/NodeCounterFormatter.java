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
package io.datarouter.storage.node.adapter.counter.formatter;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.DatarouterCounters;

public class NodeCounterFormatter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>{


	private final N node;
	private final PhysicalNode<PK,D,F> physicalNode;//may be null


	/******************* construct *************************/

	public NodeCounterFormatter(N node){
		this.node = node;
		if(node instanceof PhysicalNode){
			this.physicalNode = (PhysicalNode<PK,D,F>)node;
		}else{
			this.physicalNode = null;
		}
	}


	/********************** methods *****************************/

	public void count(String key){
		count(key, 1);
	}

	public void count(String key, long delta){
		if(physicalNode == null){
			DatarouterCounters.incNode(key, node.getName(), delta);
		}else{
			DatarouterCounters.incFromCounterAdapter(physicalNode, key, delta);
		}
	}

}
