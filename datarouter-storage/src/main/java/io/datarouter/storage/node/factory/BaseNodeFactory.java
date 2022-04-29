/*
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
package io.datarouter.storage.node.factory;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientNodeFactory;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.Node;

public abstract class BaseNodeFactory{

	@Inject
	protected DatarouterInjector injector;
	@Inject
	protected Datarouter datarouter;
	@Inject
	protected DatarouterClients clients;

	protected <T extends ClientNodeFactory> T getClientNodeFactory(ClientId clientId, Class<T> factoryType){
		ClientType<?,?> clientType = clients.getClientTypeInstance(clientId);
		ClientNodeFactory clientNodeFactory = injector.getInstance(clientType.getClientNodeFactoryClass());
		return factoryType.cast(clientNodeFactory);
	}

	/**
	 * Datarouter modules can come as runtime dependencies. There is no way to know at compile time if a client type
	 * is going to provide the desired interface, hence this unchecked cast.
	 */
	@SuppressWarnings("unchecked")
	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N cast(Node<PK,D,F> node){
		return (N)node;
	}

}
