package com.hotpads.datarouter.client.imp.http;

import java.util.NavigableSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.util.core.SetTool;

public class DataRouterHttpClient extends BaseClient{
	protected static Logger logger = Logger.getLogger(DataRouterHttpClient.class);
	
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
