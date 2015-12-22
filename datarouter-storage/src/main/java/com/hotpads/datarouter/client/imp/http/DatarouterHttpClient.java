package com.hotpads.datarouter.client.imp.http;

import java.util.NavigableSet;
import java.util.TreeSet;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.node.Node;

public class DatarouterHttpClient extends BaseClient{

	private String url;
	private ApacheHttpClient apacheHttpClient;

	private NavigableSet<Node<?,?>> nodes = new TreeSet<>();

	public DatarouterHttpClient(String name, String url, ClientAvailabilitySettings clientAvailabilitySettings){
		super(name, clientAvailabilitySettings);
		this.url = url;
		this.apacheHttpClient = new ApacheHttpClient(url);
	}


	public void registerNode(Node<?,?> node){
		this.nodes.add(node);
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
