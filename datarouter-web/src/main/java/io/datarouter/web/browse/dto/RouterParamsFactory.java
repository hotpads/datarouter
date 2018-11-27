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
package io.datarouter.web.browse.dto;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.util.lazy.Lazy;
import io.datarouter.web.handler.params.Params;

@Singleton
public class RouterParamsFactory{

	private static final String PARAM_clientType = "clientType";
	private static final String PARAM_routerName = "routerName";
	private static final String PARAM_clientName = "clientName";
	private static final String PARAM_nodeName = "nodeName";
	private static final String PARAM_tableName = "tableName";

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterNodes datarouterNodes;

	public class RouterParams<C extends Client>{

		private final String clientType;
		private final String clientName;
		private final String routerName;
		private final String tableName;
		private final Lazy<C> client;
		private final Lazy<Node<?,?,?>> node;

		public RouterParams(Params params, Class<C> clientClass){
			this.clientType = params.required(PARAM_clientType);
			this.clientName = params.required(PARAM_clientName);
			this.routerName = params.optional(PARAM_routerName).orElse(null);
			this.tableName = params.optional(PARAM_tableName).orElse(null);
			this.client = Lazy.of(() -> clientClass.cast(datarouterClients.getClient(clientName)));
			this.node = Lazy.of(() -> {
				Optional<Node<?,?,?>> node = params.optional(PARAM_nodeName)
						.map(datarouterNodes::getNode);
				if(node.isPresent()){
					return node.get();
				}
				return datarouterNodes.getPhyiscalNodeForClientAndTable(clientName, tableName);
			});
		}

		public String getClientType(){
			return clientType;
		}

		public String getClientName(){
			return clientName;
		}

		public String getRouterName(){
			return routerName;
		}

		public C getClient(){
			return client.get();
		}

		public Node<?,?,?> getNode(){
			return node.get();
		}

		public String getTableName(){
			return tableName;
		}

	}

}
