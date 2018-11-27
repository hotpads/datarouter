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
package io.datarouter.web.browse;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.web.browse.dto.NodeWrapper;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.monitoring.latency.LatencyMonitoringService;

public class RoutersHandler extends BaseHandler{

	public static final String
			ACTION_listRouters = "listRouters",
			ACTION_inspectRouter = "inspectRouter",
			ACTION_inspectClient = "inspectClient";

	public static final String
			PARAM_routerName = "routerName",
			PARAM_clientName = "clientName",
			PARAM_nodeName = "nodeName",
			PARAM_tableName = "tableName",
			PARAM_columnName = "columnName";

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private LatencyMonitoringService monitoringService;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private DatarouterWebFiles files;

	@Handler(defaultHandler = true)
	protected Mav view(){
		Mav mav = new Mav(files.jsp.admin.datarouter.datarouterMenuJsp);
		//DatarouterProperties info
		mav.put("configDirectory", datarouterProperties.getConfigDirectory());
		mav.put("configProfile", datarouterProperties.getConfigProfile());
		mav.put("environment", datarouterProperties.getEnvironment());
		mav.put("serverType", datarouterProperties.getServerTypeString());
		mav.put("serverName", datarouterProperties.getServerName());
		mav.put("administratorEmail", datarouterProperties.getAdministratorEmail());
		mav.put("serverPrivateIp", datarouterProperties.getServerPrivateIp());
		mav.put("serverPublicIp", datarouterProperties.getServerPublicIp());
		//Routers, nodes, clients
		mav.put("routers", datarouter.getRouters());
		mav.put("lazyClientProviderByName", datarouterClients.getLazyClientProviderByName());
		mav.put("uninitializedClientNames", datarouterClients.getClientNamesByInitialized().get(false));
		mav.put("monitoringService", monitoringService);
		return mav;
	}

	@Handler
	private Mav initClient(String clientName){
		datarouterClients.getClient(clientName);
		return new InContextRedirectMav(request, paths.datarouter);
	}

	@Handler
	private Mav initAllClients(){
		datarouterClients.getAllClients();
		return new InContextRedirectMav(request, paths.datarouter);
	}

	@Handler
	private Mav inspectRouter(){
		Mav mav = new Mav(files.jsp.admin.datarouter.routerSummaryJsp);
		String routerName = params.required(PARAM_routerName);
		Collection<Node<?,?,?>> nodes = datarouterNodes.getTopLevelNodesByRouterName().get(routerName);
		List<NodeWrapper> nodeWrappers = NodeWrapper.getNodeWrappers(nodes);
		mav.put("nodeWrappers", nodeWrappers);
		return mav;
	}

}
