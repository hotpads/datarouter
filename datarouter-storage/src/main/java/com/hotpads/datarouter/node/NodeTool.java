package com.hotpads.datarouter.node;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.util.core.DrIterableTool;

public class NodeTool{

	public static List<Node<?,?>> getNodeAndDescendants(Node<?,?> parent){
		List<Node<?,?>> nodes = new ArrayList<>();
		addNodeAndDescendants(nodes, parent);
		return nodes;
	}
	
	public static void addNodeAndDescendants(List<Node<?,?>> nodes, Node<?,?> parent){
		nodes.add(parent);
		List<? extends Node<?,?>> children = parent.getChildNodes();
		for(Node<?,?> child : DrIterableTool.nullSafe(children)){
			addNodeAndDescendants(nodes, child);
		}
	}
}
