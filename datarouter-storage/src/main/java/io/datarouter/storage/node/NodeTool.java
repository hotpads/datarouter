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
package io.datarouter.storage.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.adapter.BaseAdapter;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public class NodeTool{

	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> extractSinglePhysicalNode(Node<PK,D,F> node){
		Iterator<? extends PhysicalNode<PK,D,F>> physicalNodes = node.getPhysicalNodes().iterator();
		PhysicalNode<PK,D,F> physicalNode = physicalNodes.next();
		if(physicalNodes.hasNext()){
			throw new RuntimeException(node + " has multiple physical nodes, you need to pick one");
		}
		return physicalNode;
	}

	public static List<Node<?,?,?>> getNodeAndDescendants(Node<?,?,?> parent){
		List<Node<?,?,?>> nodes = new ArrayList<>();
		addNodeAndDescendants(nodes, parent);
		return nodes;
	}

	public static void addNodeAndDescendants(List<Node<?,?,?>> nodes, Node<?,?,?> parent){
		nodes.add(parent);
		List<? extends Node<?,?,?>> children = parent.getChildNodes();
		for(Node<?,?,?> child : children){
			addNodeAndDescendants(nodes, child);
		}
	}

	public static Node<?,?,?> getUnderlyingNode(Node<?,?,?> node){
		while(node instanceof BaseAdapter){
			node = ((BaseAdapter<?,?,?,?>)node).getUnderlyingNode();
		}
		return node;
	}

}
