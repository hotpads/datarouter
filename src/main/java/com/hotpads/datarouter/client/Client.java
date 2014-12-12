package com.hotpads.datarouter.client;


/**
 * Client represents a connection to a database, cache, or API endpoint. The Client might send requests to an external
 * datastore like MySQL or it might contain an in-memory datastructure like a cache namespace that contains individual
 * caches for each Node.
 * 
 * A Client must know the location of its backing datastore and any authentication information. It will manage
 * connections to the datastore and usually contains a connection pool. Connecting over the network to external
 * datastores is the slowest part of Datarouter startup.
 * 
 * JDBC necessitates that connections are assigned to a single "schema" in an RDBMS. Because we want to pool these
 * connections, we create one Client per database, even though many databases may reside in the same MySQL instance.
 * 
 * @author mcorgan
 * 
 */
public interface Client 
extends Comparable<Client>{

	String getName();
	ClientType getType();
}
