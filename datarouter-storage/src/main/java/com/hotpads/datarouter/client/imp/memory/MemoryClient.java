package com.hotpads.datarouter.client.imp.memory;

import java.util.NavigableSet;
import java.util.TreeSet;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;

public class MemoryClient extends BaseClient{

	protected NavigableSet<Node<?,?>> nodes = new TreeSet<>();

	public MemoryClient(String name, ClientAvailabilitySettings clientAvailabilitySettings){
		super(name, clientAvailabilitySettings);
	}

	public void registerMemoryNode(Node<?,?> node){
		this.nodes.add(node);
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
