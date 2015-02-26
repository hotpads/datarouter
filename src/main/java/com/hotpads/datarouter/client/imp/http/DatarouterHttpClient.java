package com.hotpads.datarouter.client.imp.http;

import java.util.NavigableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.util.core.DrSetTool;

public class DatarouterHttpClient extends BaseClient{
	protected static Logger logger = LoggerFactory.getLogger(DatarouterHttpClient.class);
	
	private String name;
	private String url;
	private ApacheHttpClient apacheHttpClient;

	private NavigableSet<Node<?,?>> nodes = DrSetTool.createTreeSet();
	
	
	public DatarouterHttpClient(String name, String url){
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
