package com.hotpads.datarouter.node.base.partitioned;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class PartitionedNodeTool {

	public static List<Integer> getNodeIndexes(int numNodes){
		List<Integer> indexes = ListTool.createLinkedList();
		for(int i=0; i < numNodes; ++i){
			indexes.add(i);
		}
		return indexes;
	}
	
}
