package com.hotpads.datarouter.client.imp.memory;

import java.util.NavigableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.util.core.DrSetTool;

public class MemoryClient extends BaseClient{
	protected static Logger logger = LoggerFactory.getLogger(MemoryClient.class);

	protected String name;
	protected NavigableSet<Node<?,?>> nodes = DrSetTool.createTreeSet();
	
	
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
	
	@Override
	public void shutdown(){
		//no-op
	}
	
	public NavigableSet<Node<?,?>> getNodes(){
		return nodes;
	}

}
