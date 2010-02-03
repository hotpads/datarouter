package com.hotpads.datarouter.routing;

import java.io.IOException;
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

public abstract class BaseDataRouter implements DataRouter {
	
	/********************************* fields **********************************/
	protected String name;

	protected ConnectionPools connectionPools;
	
	protected Clients clients;
	
	@SuppressWarnings("unchecked")
	protected Nodes nodes = new Nodes();
	
	
	/**************************** constructor  ****************************************/
	
	public BaseDataRouter(String name) throws IOException{
		this.name = name;
		this.connectionPools = new ConnectionPools(this.getConfigLocation());
		this.clients = new Clients(this.getConfigLocation(), this);
	}
	
	
	/********************************* methods *************************************/
	
	public String getConfigLocation(){
		return null;
	}
	
	public void setClients(Clients clients){  //called by DataRouterFactory during init
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
		return this.connectionPools;
	}

	
	/********************* get/set ******************************/


	public String getName() {
		return name;
	}
	
	
	

	/********************************* sample config file ***********************************/
	/*
	 * 
implementation=development

# connectionPools
connectionPoolNames=animal0,pets0,pets1,pets0_slave0,pets1_slave0

connectionPools.defaultInitMode=lazy
#connectionPools.forceInitMode=eager

connectionPool.animal0.url=localhost:3306/animal0
connectionPool.animal0.maxPoolSize=10

connectionPool.pets0.url=localhost:3306/pets0
connectionPool.pets0.maxPoolSize=10

connectionPool.pets1.url=localhost:3306/pets1
connectionPool.pets1.maxPoolSize=10

connectionPool.pets0_slave0.url=localhost:3306/pets0
connectionPool.pets0_slave0.maxPoolSize=10
connectionPool.pets0_slave0.readOnly=true

connectionPool.pets1_slave0.url=localhost:3306/pets1
connectionPool.pets1_slave0.maxPoolSize=10
connectionPool.pets1_slave0.readOnly=true


# clients
clientNames=testHashMap,animal0,pets0,pets1,pets0_slave0,pets1_slave0

clients.defaultInitMode=lazy
#clients.forceInitMode=eager

client.testHashMap.type=hashMap

client.animal0.type=hibernate

client.pets0.type=hibernate

client.pets1.type=hibernate

client.pets0_slave0.type=hibernate
client.pets0_slave0.slave=true
client.pets0_slave0.initMode=eager

client.pets1_slave0.type=hibernate
client.pets1_slave0.slave=true

client.event.type=hibernate
client.event.springBeanName=sessionFactoryEvent

	 */
	
}
