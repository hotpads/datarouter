package com.hotpads.handler.util.node;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.ListTool;

public class NodeWrapper{
	public Node<?,?> node;
	public int levelsNested = 0;
	boolean sorted = false;

	public NodeWrapper(Node<?,?> node, int levelsNested){
		this.node = node;
		this.levelsNested = levelsNested;
		this.sorted = node instanceof SortedStorageWriter<?,?>;
	}

	public static List<NodeWrapper> getNodeWrappers(Datarouter router){
		List<NodeWrapper> outs = ListTool.createArrayList();
		Collection<Node<?,?>> topLevelNodes = router.getContext().getNodes().getTopLevelNodesByRouterName().get(
				router.getName());
//		SortedSet<Node> topLevelNodes = router.getNodes();
		for(Node<?,?> node : topLevelNodes){
			addNodeAndChildren(node, outs, 0);
		}
		return outs;
	}

	public static void addNodeAndChildren(Node<?,?> parentNode, List<NodeWrapper> wrappers, int indent){
		wrappers.add(new NodeWrapper(parentNode, indent));
		List<? extends Node<?,?>> childNodes = parentNode.getChildNodes();
		if(CollectionTool.isEmpty(childNodes)){ return; }
		for(Node<?,?> node : childNodes){
			addNodeAndChildren(node, wrappers, indent + 1);
		}
	}

	public Node<?,?> getNode(){
		return node;
	}
	
	public String getClassName(){
		return node.getClass().getName();
	}
	
	public String getClassSimpleName(){
		return node.getClass().getSimpleName();
	}
	
	public HBaseNode<?,?,?> getHBaseNode(){
		if(! getIsHBaseNode()){ return null; }
		return (HBaseNode<?,?,?>)node;
	}
	
	public boolean getIsHBaseNode(){
		return node instanceof HBaseNode;
	}
	
	public boolean getIsHibernateReaderNode(){
		return node instanceof HibernateReaderNode;
	}
	
	public String getHBaseTableName(){
		if(! getIsHBaseNode()){ return null; }
		return getHBaseNode().getTableName();
	}

	public int getLevelsNested(){
		return levelsNested;
	}

	public String getIndentHtml(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < levelsNested; ++i){
			sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		return sb.toString();
	}

	public boolean isSorted(){
		return sorted;
	}

}