package com.hotpads.datarouter.node.type.partitioned.base;

import java.util.List;

import com.hotpads.datarouter.util.core.DrListTool;

public class PartitionedNodeTool {

	public static List<Integer> getNodeIndexes(int numNodes){
		List<Integer> indexes = DrListTool.createLinkedList();
		for(int i=0; i < numNodes; ++i){
			indexes.add(i);
		}
		return indexes;
	}
	
}
