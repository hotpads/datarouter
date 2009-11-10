package com.hotpads.datarouter.routing;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.app.App;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public abstract class DataRouter {
	
	/********************************* fields **********************************/
	
	protected Clients clients;
	
	@SuppressWarnings("unchecked")
	protected Nodes nodes = new Nodes();
	
	
	/**************************** constructor ****************************************/
	
	public DataRouter(){
	}
	
	
	/********************************* methods *************************************/
	
	public abstract String getConfigLocation();
	
	public void setClients(Clients clients){
		this.clients = clients;
	}

	@SuppressWarnings("unchecked")
	public <D extends Databean,N extends Node<D>> N register(N node){
		this.nodes.register(node);
		return node;
	}
	
	@SuppressWarnings("unchecked")
	public Nodes getNodes() {
		return nodes;
	}

	
	/************************************** app wrappers **************************************/

	public <T> T run(App<T> app){
		T t = app.runInEnvironment();
		return t;
	}
	
	/************************************** caching ***********************************/
	
	public void clearThreadSpecificState(){
		this.nodes.clearThreadSpecificState();
	}
	
	/************************************** getting clients *************************/
	
	public Client getClient(String clientName){
		return clients.getClient(clientName);
	}
	
	public List<Client> getAllClients(){
		return this.clients.getAllClients();
	}
	
	public List<Client> getClients(Collection<String> clientNames){
		return this.clients.getClients(clientNames);
	}

	@SuppressWarnings("unchecked")
	public <D extends Databean> List<String> getClientNamesForKeys(Collection<Key<D>> keys){
		List<String> clientNames = this.nodes.getClientNamesForKeys(keys);
		return clientNames;
	}
	
	public <D extends Databean> List<String> getClientNamesForDatabeans(Collection<D> databeans){
		return this.getClientNamesForKeys(DatabeanTool.getKeys(databeans));
	}

	@SuppressWarnings("unchecked")
	public <D extends Databean> List<Client> getClientsForDatabeanType(Class<D> databeanType){
		List<String> clientNames = this.nodes.getClientNamesForDatabeanType(databeanType);
		if(CollectionTool.isEmpty(clientNames)){ return null; }
		return this.clients.getClients(clientNames);
	}

	public <D extends Databean> List<Client> getClientsForKeys(Collection<Key<D>> keys){
		List<Client> clientsForKeys = ListTool.createLinkedList();
		List<String> clientNames = this.getClientNamesForKeys(keys);
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			Client client = this.getClient(clientName);
			clientsForKeys.add(client);
		}
		return clientsForKeys;
	}
	
	public <D extends Databean> List<Client> getClientsForDatabeans(Collection<D> databeans){
		return this.getClientsForKeys(DatabeanTool.getKeys(databeans));
	}
	
	/***************** overexposed accessors *******************************/
	public ConnectionPools getConnectionPools(){
		return this.clients.getConnectionPools();
	}
	
}
