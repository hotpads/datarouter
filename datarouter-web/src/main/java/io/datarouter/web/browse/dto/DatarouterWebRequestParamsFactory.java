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

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.web.handler.params.Params;

@Singleton
public class DatarouterWebRequestParamsFactory{

	private static final String PARAM_clientName = "clientName";
	private static final String PARAM_nodeName = "nodeName";
	private static final String PARAM_tableName = "tableName";

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterNodes datarouterNodes;

	public class DatarouterWebRequestParams<CT extends ClientType<?,?>>{

		private final CT clientType;
		private final ClientId clientId;
		private final String tableName;
		private final SingletonSupplier<Node<?,?,?>> node;

		public DatarouterWebRequestParams(Params params, Class<CT> clientTypeClass){
			String clientName = params.required(PARAM_clientName);
			this.clientId = datarouterClients.getClientId(clientName);
			this.clientType = clientTypeClass.cast(datarouterClients.getClientTypeInstance(clientId));
			this.tableName = params.optional(PARAM_tableName).orElse(null);
			this.node = SingletonSupplier.of(() -> {
				Optional<Node<?,?,?>> node = params.optional(PARAM_nodeName)
						.map(datarouterNodes::getNode);
				if(node.isPresent()){
					return node.get();
				}
				return datarouterNodes.getPhysicalNodeForClientAndTable(clientName, tableName);
			});
		}

		public CT getClientType(){
			return clientType;
		}

		public ClientId getClientId(){
			return clientId;
		}

		public Node<?,?,?> getNode(){
			return node.get();
		}

		public PhysicalNode<?,?,?> getPhysicalNode(){
			return NodeTool.extractSinglePhysicalNode(node.get());
		}

		public String getTableName(){
			return tableName;
		}

	}

}
