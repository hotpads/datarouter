package com.hotpads.datarouter.routing;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.app.App;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

/**
 * @author mcorgan
 *
 */
public abstract class BaseDataRouter implements DataRouter {

	public static final String
		MODE_development = "development",
		MODE_production = "production";
	
	/********************************* fields **********************************/
	protected String name;
	protected List<ClientId> clientIds;
	protected List<String> clientNames;
	protected RouterOptions routerOptions;
	protected ConnectionPools connectionPools;
	protected Clients clients;
	
	@SuppressWarnings("unchecked")
	protected Nodes nodes = new Nodes();
	
	
	/**************************** constructor  ****************************************/
	
	public BaseDataRouter(String name, List<ClientId> clientIds) throws IOException{
		this.name = name;
		this.clientIds = clientIds;
		this.clientNames = ClientId.getNames(clientIds);
		this.routerOptions = new RouterOptions(getConfigLocation());
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
	public <PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>> 
	N register(N node){
		this.nodes.register(node);
		return node;
	}
	
	/*
	 * be careful that this gets called after the nodes are registered, otherwise you get SessionFactories
	 *  without databean configs, and Hibernate will silently return empty results (when called with entityName instead of class)
	 */
	@Override
	public void activate() throws IOException{
		this.connectionPools = new ConnectionPools(this);
		this.clients = new Clients(this.getConfigLocation(), this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Nodes getNodes() {
		return nodes;
	}

	@SuppressWarnings("unchecked")
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends NodeOps<PK,D>> 
	N cast(Node<PK,D> in){
		return (N)in;
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
	public List<ClientId> getClientIds(){
		return clientIds;
	}
	
	@Override
	public List<String> getClientNames(){
		return clientNames;
	}
	
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
	public <PK extends PrimaryKey<PK>,D extends Databean<PK>> List<String> 
	getClientNamesForKeys(Collection<? extends Key<PK>> keys){
		List<String> clientNames = this.nodes.getClientNamesForKeys(keys);
		return clientNames;
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK>> List<String> 
	getClientNamesForDatabeans(Collection<D> databeans){
		return this.getClientNamesForKeys(KeyTool.getKeys(databeans));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <PK extends PrimaryKey<PK>,D extends Databean<PK>> List<Client> 
	getClientsForDatabeanType(Class<D> databeanType){
		List<String> clientNames = this.nodes.getClientNamesForDatabeanType(databeanType);
		if(CollectionTool.isEmpty(clientNames)){ return null; }
		return this.clients.getClients(clientNames);
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK>> List<Client> 
	getClientsForKeys(Collection<? extends Key<PK>> keys){
		List<Client> clientsForKeys = ListTool.createLinkedList();
		List<String> clientNames = this.getClientNamesForKeys(keys);
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			Client client = this.getClient(clientName);
			clientsForKeys.add(client);
		}
		return clientsForKeys;
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK>> List<Client> 
	getClientsForDatabeans(Collection<D> databeans){
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

	@Override
	public RouterOptions getClientOptions(){
		return routerOptions;
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
