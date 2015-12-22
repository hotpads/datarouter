package com.hotpads.datarouter.routing;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseRouter
implements Router{

	public static final String
		MODE_development = "development",
		MODE_production = "production"
		;

	/********************************* fields **********************************/

	private final Datarouter datarouter;
	private final String configLocation;
	private final String name;
	private final List<String> clientNames;
	private final RouterOptions routerOptions;


	/**************************** constructor  ****************************************/

	public BaseRouter(Datarouter datarouter, String configLocation, String name){
		this.datarouter = datarouter;
		this.configLocation = configLocation;
		this.name = name;
		this.clientNames = ClientId.getNames(getClientIds());
		this.routerOptions = new RouterOptions(getConfigLocation());
		this.datarouter.registerConfigFile(getConfigLocation());
		registerWithContext();
	}


	/********************************* methods *************************************/

	@Override
	public final String getConfigLocation(){
		return configLocation;
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>> N register(N node){
		this.datarouter.getNodes().register(name, node);
		this.datarouter.registerClientIds(node.getClientIds());
		return node;
	}

	@Override
	public void registerWithContext(){
		datarouter.register(this);
	}

	/************************************** getting clients *************************/

	@Override
	public List<String> getClientNames(){
		return clientNames;
	}

	@Override
	public Client getClient(String clientName){
		return datarouter.getClientPool().getClient(clientName);
	}

	@Override
	public ClientType getClientType(String clientName){
		return datarouter.getClientPool().getClientTypeInstance(clientName);
	}

	@Override
	public List<Client> getAllClients(){
		return datarouter.getClientPool().getClients(datarouter, getClientNames());
	}

//	@Override
//	public List<Client> getClients(Collection<String> clientNames){
//		return context.getClientPool().getClients(clientNames);
//	}

	/***************** overexposed accessors *******************************/

	@Deprecated
	@Override
	public Datarouter getContext() {
		return datarouter;
	}


	/********************* Object ********************************/

	@Override
	public String toString(){
		return name;
	}

	@Override
	public int compareTo(Router otherDatarouter){
		return getName().compareToIgnoreCase(otherDatarouter.getName());
	}


	/********************* get/set ******************************/

	@Override
	public String getName() {
		return name;
	}

	@Override
	public RouterOptions getRouterOptions(){
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
