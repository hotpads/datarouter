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
package io.datarouter.client.hbase.util;

import io.datarouter.client.hbase.node.nonentity.HBaseReaderNode;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityReaderNode;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;

public class HBaseClientTool{

	// this currently gets ignored on an empty table since there are no regions to split
	public static byte[][] getSplitPoints(PhysicalNode<?,?,?> node){
		EntityFieldInfo<?,?> entityFieldInfo = getEntityFieldInfo(node);
		EntityPartitioner<?> partitioner = entityFieldInfo.getEntityPartitioner();
		// remember to skip the first partition
		int numSplitPoints = partitioner.getNumPartitions() - 1;
		byte[][] splitPoints = new byte[numSplitPoints][];
		for(int i = 1; i < partitioner.getAllPrefixes().size(); ++i){
			splitPoints[i - 1] = partitioner.getPrefix(i);
		}
		return splitPoints;
	}

	public static EntityFieldInfo<?,?> getEntityFieldInfo(Node<?,?,?> node){
		PhysicalNode<?,?,?> physicalNode = NodeTool.extractSinglePhysicalNode(node);
		if(physicalNode instanceof HBaseSubEntityReaderNode){
			HBaseSubEntityReaderNode<?,?,?,?,?> subEntityNode = (HBaseSubEntityReaderNode<?,?,?,?,?>)physicalNode;
			return subEntityNode.getEntityFieldInfo();
		}
		HBaseReaderNode<?,?,?,?,?> nonEntityNode = (HBaseReaderNode<?,?,?,?,?>)physicalNode;
		return nonEntityNode.getEntityFieldInfo();
	}

}
