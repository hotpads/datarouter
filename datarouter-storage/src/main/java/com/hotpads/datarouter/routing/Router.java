package com.hotpads.datarouter.routing;

import java.io.IOException;
import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A Router is a strongly-typed collection of Nodes. It is easy to trace all uses of the node through your code using
 * tools like Eclipse's ctrl-shift-G shortcut. The generic types on each node prevent you at compile time from
 * accidentally saving a databean to the wrong node or requesting a Databean with a PrimaryKey that doesn't match the
 * node.
 *
 * You can inject a router into application code for standard operations like get() or putMulti(), or you can inject it
 * into a DAO layer that adds more complex logic.
 *
 * Nodes are instantiated in the router, and the router is where each PhysicalNode is mapped to a Client.
 *
 * While a small application may have only one router, a large application may group related nodes into separate
 * routers. A good rule of thumb is to have one router per database giving access to all tables in the database. It is
 * also feasible to have a router talk to multiple clients/databases, or to have multiple routers talking to the same
 * client.
 *
 * The router does not "own" a Client, it merely keeps a ClientId reference to each Client. Clients are owned by
 * Datarouter, and there is no penalty for using a Client in multiple routers.
 */
public interface Router extends Comparable<Router>{

	/********************************* methods *************************************/

	String getConfigLocation();
	RouterOptions getRouterOptions();

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>, N extends Node<PK,D>> N register(N node);

	void registerWithContext() throws IOException;

	@Deprecated
	Datarouter getContext();


	/************************************** getting clients *************************/

	List<ClientId> getClientIds();
	List<String> getClientNames();
	Client getClient(String clientName);
	ClientType getClientType(String clientName);
	List<Client> getAllClients();


	/********************* get/set ******************************/

	String getName();

}