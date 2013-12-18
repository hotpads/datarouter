package com.hotpads.datarouter.routing;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public abstract class BaseDataRouter
implements DataRouter{

	public static final String
		MODE_development = "development",
		MODE_production = "production";
	
	/********************************* fields **********************************/
	
	protected DataRouterContext context;
	protected String name;
	protected List<String> clientNames;
	protected RouterOptions routerOptions;
	
	
	/**************************** constructor  ****************************************/
	
	public BaseDataRouter(DataRouterContext context, String name){
		this.context = context;
		this.name = name;
		this.clientNames = ClientId.getNames(getClientIds());
		this.routerOptions = new RouterOptions(getConfigLocation());
	}
	
	
	/********************************* methods *************************************/

//	@Override
//	public String getConfigLocation(){
//		return null;
//	}

	@Override
	@SuppressWarnings("unchecked")
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>> N register(N node){
		this.context.getNodes().register(name, node);
		return node;
	}
	
	/*
	 * be careful that this gets called after the nodes are registered, otherwise you get SessionFactories
	 *  without databean configs, and Hibernate will silently return empty results (when called with entityName instead of class)
	 */
	@Override
	public void activate(){
		context.register(this);
	}

	@Override
	public SortedSet<Node> getNodes() {
		return context.getNodes().getNodesForRouterName(getName());
	}

	@SuppressWarnings("unchecked")
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends NodeOps<PK,D>> 
	N cast(Node<PK,D> in){
		return (N)in;
	}
	
	/************************************** app wrappers **************************************/

	@Override
	public <T> T run(TxnOp<T> parallelTxnOp){
		TraceContext.startSpan(parallelTxnOp.getClass().getSimpleName());
		T t;
		try{
			t = new SessionExecutorImpl<T>(parallelTxnOp).call();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		TraceContext.finishSpan();
		return t;
	}
	
	/************************************** caching ***********************************/

	@Override
	public void clearThreadSpecificState(){
		context.getNodes().clearThreadSpecificState();
	}
	
	/************************************** getting clients *************************/
	
	@Override
	public List<String> getClientNames(){
		return clientNames;
	}
	
	@Override
	public Client getClient(String clientName){
		return context.getClientPool().getClient(clientName);
	}

	@Override
	public List<Client> getAllClients(){
		return context.getClientPool().getClients(getClientNames());
	}
	
	@Override
	public List<Client> getAllInstantiatedClients(){
		return context.getClientPool().getAllInstantiatedClients();
	}

	@Override
	public List<Client> getClients(Collection<String> clientNames){
		return context.getClientPool().getClients(clientNames);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K extends Key<K>>List<String> getClientNamesForKeys(Collection<? extends Key<K>> keys){
		List<String> clientNames = context.getNodes().getClientNamesForKeys(keys);
		return clientNames;
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>List<String> getClientNamesForDatabeans(
			Collection<D> databeans){
		return getClientNamesForKeys(KeyTool.getKeys(databeans));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<Client> 
	getClientsForDatabeanType(Class<D> databeanType){
		List<String> clientNames = context.getNodes().getClientNamesForDatabeanType(databeanType);
		if(CollectionTool.isEmpty(clientNames)){ return null; }
		return context.getClientPool().getClients(clientNames);
	}

	@Override
	public <K extends Key<K>>List<Client> getClientsForKeys(Collection<? extends Key<K>> keys){
		List<Client> clientsForKeys = ListTool.createLinkedList();
		List<String> clientNames = getClientNamesForKeys(keys);
		for(String clientName : CollectionTool.nullSafe(clientNames)){
			Client client = getClient(clientName);
			clientsForKeys.add(client);
		}
		return clientsForKeys;
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>List<Client> getClientsForDatabeans(
			Collection<D> databeans){
		return getClientsForKeys(KeyTool.getKeys(databeans));
	}
	
	/***************** overexposed accessors *******************************/
	
	@Override
	public ConnectionPools getConnectionPools(){
		return context.getConnectionPools();
	}
	
	@Override
	public DataRouterContext getContext() {
		return context;
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
