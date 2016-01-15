package com.hotpads.datarouter.node.type.partitioned.base;

import java.util.LinkedList;
import java.util.List;

public class PartitionedNodeTool {

	public static List<Integer> getNodeIndexes(int numNodes){
		List<Integer> indexes = new LinkedList<>();
		for(int i=0; i < numNodes; ++i){
			indexes.add(i);
		}
		return indexes;
	}
	
}
