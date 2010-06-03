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
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface DataRouter {

	/********************************* methods *************************************/

	String getConfigLocation();

	void setClients(Clients clients);

	<PK extends PrimaryKey<PK>,D extends Databean<PK>, N extends Node<PK,D>> N register(N node);
	
	void activate() throws IOException;

	@SuppressWarnings("unchecked")
	Nodes getNodes();

	/************************************** app wrappers **************************************/

	<T> T run(App<T> app);

	/************************************** caching ***********************************/

	void clearThreadSpecificState();

	/************************************** getting clients *************************/

	Client getClient(String clientName);

	List<Client> getAllClients();

	List<Client> getClients(Collection<String> clientNames);

	<PK extends PrimaryKey<PK>,D extends Databean<PK>> List<String> getClientNamesForKeys(
			Collection<? extends Key<PK>> keys);

	<PK extends PrimaryKey<PK>,D extends Databean<PK>> List<String> getClientNamesForDatabeans(
			Collection<D> databeans);

	<PK extends PrimaryKey<PK>,D extends Databean<PK>> List<Client> getClientsForDatabeanType(
			Class<D> databeanType);

	<PK extends PrimaryKey<PK>,D extends Databean<PK>> List<Client> getClientsForKeys(Collection<? extends Key<PK>> keys);

	<PK extends PrimaryKey<PK>,D extends Databean<PK>> List<Client> getClientsForDatabeans(
			Collection<D> databeans);

	/***************** overexposed accessors *******************************/
	ConnectionPools getConnectionPools();

	/********************* get/set ******************************/

	String getName();

}