package com.hotpads.datarouter.client.imp.memory;

import java.util.NavigableSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.DClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.util.core.SetTool;

public class MemoryClient extends BaseClient{
	protected static Logger logger = Logger.getLogger(MemoryClient.class);
	
	private final DClientType type = new MemoryClientType();

	protected String name;
	protected NavigableSet<Node<?,?>> nodes = SetTool.createTreeSet();
	
	
	public MemoryClient(String name){
		this.name = name;
	}

	
	public void registerNode(Node<?,?> node){
		this.nodes.add(node);
	}
	
	@Override
	public String getName(){
		return name;
	}

	@Override
	public DClientType getType(){
		return type;
	}
	
	public NavigableSet<Node<?,?>> getNodes(){
		return nodes;
	}

}
