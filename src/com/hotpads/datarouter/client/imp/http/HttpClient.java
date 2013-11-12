package com.hotpads.datarouter.client.imp.http;

import java.net.URL;
import java.util.NavigableSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.util.core.SetTool;

public class HttpClient extends BaseClient{
	protected static Logger logger = Logger.getLogger(HttpClient.class);

	protected String name;
	protected URL url;
	protected NavigableSet<Node<?,?>> nodes = SetTool.createTreeSet();
	
	
	public HttpClient(String name, URL url){
		this.name = name;
		this.url = url;
	}

	
	public void registerNode(Node<?,?> node){
		this.nodes.add(node);
	}
	
	@Override
	public String getName(){
		return name;
	}

	@Override
	public ClientType getType(){
		return ClientType.http;
	}
	
	public NavigableSet<Node<?,?>> getNodes(){
		return nodes;
	}

}
