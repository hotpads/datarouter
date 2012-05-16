package com.hotpads.datarouter.routing;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.app.App;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface DataRouter {

	/********************************* methods *************************************/

	String getConfigLocation();
	RouterOptions getClientOptions();

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>, N extends Node<PK,D>> N register(N node);
	
	void activate() throws IOException;

	SortedSet<Node> getNodes();
	DataRouterContext getContext();

	/************************************** app wrappers **************************************/

	<T> T run(App<T> app);

	/************************************** caching ***********************************/

	void clearThreadSpecificState();

	/************************************** getting clients *************************/

	List<ClientId> getClientIds();
	List<String> getClientNames();
	Client getClient(String clientName);
	List<Client> getAllClients();
	List<Client> getAllInstantiatedClients();
	List<Client> getClients(Collection<String> clientNames);

	<K extends Key<K>> List<String> 
			getClientNamesForKeys(Collection<? extends Key<K>> keys);

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<String> 
			getClientNamesForDatabeans(Collection<D> databeans);

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<Client> 
			getClientsForDatabeanType(Class<D> databeanType);

	<K extends Key<K>> List<Client> 
			getClientsForKeys(Collection<? extends Key<K>> keys);

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<Client> 
			getClientsForDatabeans(Collection<D> databeans);

	/***************** overexposed accessors *******************************/
	ConnectionPools getConnectionPools();

	/********************* get/set ******************************/

	String getName();

}