package com.hotpads.datarouter.client.imp.memory;

import java.util.NavigableSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.util.core.SetTool;

public class MemoryClient extends BaseClient{
	protected static Logger logger = Logger.getLogger(MemoryClient.class);

	protected String name;
	protected NavigableSet<Node<?,?>> nodes = SetTool.createTreeSet();
	
	
	public MemoryClient(String name){
		this.name = name;
	}

	
	public void registerMemoryNode(Node<?,?> node){
		this.nodes.add(node);
	}
	
	@Override
	public String getName(){
		return name;
	}

	@Override
	public ClientType getType(){
		return MemoryClientType.INSTANCE;
	}
	
	public NavigableSet<Node<?,?>> getNodes(){
		return nodes;
	}

}
