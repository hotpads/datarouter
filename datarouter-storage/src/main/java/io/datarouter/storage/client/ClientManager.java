/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.client;

import java.util.Optional;
import java.util.concurrent.Future;

import io.datarouter.storage.node.type.physical.PhysicalNode;

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
 */
public interface ClientManager{

	void shutdown(ClientId clientId);
	boolean monitorLatency();
	Future<Optional<SchemaUpdateResult>> notifyNodeRegistration(PhysicalNode<?,?,?> node);
	void initClient(ClientId clientId);
	void sendEmail();

}
