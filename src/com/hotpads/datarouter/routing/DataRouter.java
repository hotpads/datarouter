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

public interface DataRouter {

	/********************************* methods *************************************/

	String getConfigLocation();

	void setClients(Clients clients);

	<D extends Databean, N extends Node<D>> N register(N node);
	
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

	<D extends Databean> List<String> getClientNamesForKeys(
			Collection<? extends Key<D>> keys);

	<D extends Databean> List<String> getClientNamesForDatabeans(
			Collection<D> databeans);

	<D extends Databean> List<Client> getClientsForDatabeanType(
			Class<D> databeanType);

	<D extends Databean> List<Client> getClientsForKeys(Collection<? extends Key<D>> keys);

	<D extends Databean> List<Client> getClientsForDatabeans(
			Collection<D> databeans);

	/***************** overexposed accessors *******************************/
	ConnectionPools getConnectionPools();

	/********************* get/set ******************************/

	String getName();

}