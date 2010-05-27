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
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
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
	}
	
	
	/********************************* methods *************************************/

	@Override
	public String getConfigLocation(){
		return null;
	}

	@Override
	public void setClients(Clients clients){  //called by DataRouterFactory during init
		this.clients = clients;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <D extends Databean,PK extends PrimaryKey<D>,N extends Node<D,PK>> N register(N node){
		this.nodes.register(node);
		return node;
	}
	
	/*
	 * be careful that this gets called after the nodes are registered, otherwise you get SessionFactories
	 *  without databean configs, and Hibernate will silently return empty results (when called with entityName instead of class)
	 */
	@Override
	public void activate() throws IOException{
		this.connectionPools = new ConnectionPools(this.getConfigLocation());
		this.clients = new Clients(this.getConfigLocation(), this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Nodes getNodes() {
		return nodes;
	}

	
	/************************************** app wrappers **************************************/

	@Override
	public <T> T run(App<T> app){
		TraceContext.startSpan(app.getClass().getSimpleName());
		T t = app.runInEnvironment();
		TraceContext.finishSpan();
		return t;
	}
	
	/************************************** caching ***********************************/

	@Override
	public void clearThreadSpecificState(){
		this.nodes.clearThreadSpecificState();
	}
	
	/************************************** getting clients *************************/

	@Override
	public Client getClient(String clientName){
		return clients.getClient(clientName);
	}

	@Override
	public List<Client> getAllClients(){
		return this.clients.getAllClients();
	}

	@Override
	public List<Client> getClients(Collection<String> clientNames){
		return this.clients.getClients(clientNames);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <D extends Databean> List<String> getClientNamesForKeys(Collection<? extends Key<D>> keys){
		List<String> clientNames = this.nodes.getClientNamesForKeys(keys);
		return clientNames;
	}

	@Override
	public <D extends Databean> List<String> getClientNamesForDatabeans(Collection<D> databeans){
		return this.getClientNamesForKeys(KeyTool.getKeys(databeans));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <D extends Databean> List<Client> getClientsForDatabeanType(Class<D> databeanType){
		List<String> clientNames = this.nodes.getClientNamesForDatabeanType(databeanType);
		if(CollectionTool.isEmpty(clientNames)){ return null; }
		return this.clients.getClients(clientNames);
	}

	@Override
	public <D extends Databean> List<Client> getClientsForKeys(Collection<? extends Key<D>> keys){
		List<Client> clientsForKeys = ListTool.createLinkedList();
		List<String> clientNames = this.getClientNamesForKeys(keys);
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			Client client = this.getClient(clientName);
			clientsForKeys.add(client);
		}
		return clientsForKeys;
	}

	@Override
	public <D extends Databean> List<Client> getClientsForDatabeans(Collection<D> databeans){
		return this.getClientsForKeys(KeyTool.getKeys(databeans));
	}
	
	/***************** overexposed accessors *******************************/
	
	@Override
	public ConnectionPools getConnectionPools(){
		return this.connectionPools;
	}

	
	/********************* get/set ******************************/

	@Override
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
