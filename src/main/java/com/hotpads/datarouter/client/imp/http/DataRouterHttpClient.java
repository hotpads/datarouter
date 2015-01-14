package com.hotpads.datarouter.client.imp.http;

import java.util.NavigableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.util.core.SetTool;

public class DataRouterHttpClient extends BaseClient{
	protected static Logger logger = LoggerFactory.getLogger(DataRouterHttpClient.class);
	
	private String name;
	private String url;
	private ApacheHttpClient apacheHttpClient;

	private NavigableSet<Node<?,?>> nodes = SetTool.createTreeSet();
	
	
	public DataRouterHttpClient(String name, String url){
		this.name = name;
		this.url = url;
		this.apacheHttpClient = new ApacheHttpClient(url);
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
		return HttpClientType.INSTANCE;
	}
	
	@Override
	public void shutdown(){
	}
	
	public NavigableSet<Node<?,?>> getNodes(){
		return nodes;
	}


	public ApacheHttpClient getApacheHttpClient(){
		return apacheHttpClient;
	}


	public String getUrl(){
		return url;
	}
	
	
	
	

}
