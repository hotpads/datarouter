package com.hotpads.datarouter.node;

import java.util.List;

import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class NodeTool{

	public static List<Node<?,?>> getNodeAndDescendants(Node<?,?> parent){
		List<Node<?,?>> nodes = ListTool.createArrayList();
		addNodeAndDescendants(nodes, parent);
		return nodes;
	}
	
	public static void addNodeAndDescendants(List<Node<?,?>> nodes, Node<?,?> parent){
		nodes.add(parent);
		List<? extends Node<?,?>> children = parent.getChildNodes();
		for(Node<?,?> child : IterableTool.nullSafe(children)){
			addNodeAndDescendants(nodes, child);
		}
	}
}