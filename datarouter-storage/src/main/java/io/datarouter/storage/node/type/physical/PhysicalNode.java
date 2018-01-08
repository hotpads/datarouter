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
package io.datarouter.storage.node.type.physical;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.Node;

/**
 * A PhysicalNode is a node backed by a specific location like a database table, a memcached namespace, an in-memory
 * collection, or a remote API endpoint.  It is therefore tied to a specific Client, and a table accessible through that
 * Client.  By default, Datarouter will name the backing database table after the Databean it stores, but a PhysicalNode
 * can override the table name via getTableName().
 */
public interface PhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends Node<PK,D,F>{

	public ClientId getClientId();
	Client getClient();

	String getTableName();

}
