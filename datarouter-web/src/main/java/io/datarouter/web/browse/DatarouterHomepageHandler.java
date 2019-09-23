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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.monitoring.latency.CheckResult.CheckResultJspDto;
import io.datarouter.web.monitoring.latency.LatencyMonitoringService;

public class DatarouterHomepageHandler extends BaseHandler{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private LatencyMonitoringService monitoringService;
	@Inject
	private DatarouterWebFiles files;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private DatarouterWebPaths datarouterWebPaths;

	@Handler(defaultHandler = true)
	protected Mav view(){
		Mav mav = new Mav(files.jsp.admin.datarouter.datarouterMenuJsp);

		//DatarouterProperties info
		mav.put("configDirectory", datarouterProperties.getConfigDirectory());
		mav.put("environmentType", datarouterProperties.getEnvironmentType());
		mav.put("environment", datarouterProperties.getEnvironment());
		mav.put("serverType", datarouterProperties.getServerTypeString());
		mav.put("serverName", datarouterProperties.getServerName());
		mav.put("administratorEmail", datarouterProperties.getAdministratorEmail());
		mav.put("serverPrivateIp", datarouterProperties.getServerPrivateIp());
		mav.put("serverPublicIp", datarouterProperties.getServerPublicIp());

		//Clients
		boolean hasUninitializedClients = false;
		List<RoutersRowJspDto> clients = new ArrayList<>();
		for(ClientId clientId : datarouterClients.getClientIds()){
			boolean initialized = clientInitializationTracker.isInitialized(clientId);
			hasUninitializedClients = hasUninitializedClients || !initialized;
			String clientTypeName = datarouterClients.getClientTypeInstance(clientId).getName();
			CheckResultJspDto checkResultJspDto = new CheckResultJspDto(
					monitoringService.getLastResultForDatarouterClient(clientId),
					monitoringService.getGraphLinkForDatarouterClient(clientId));
			clients.add(new RoutersRowJspDto(clientId.getName(), clientTypeName, initialized, checkResultJspDto));
		}
		mav.put("clients", clients);
		mav.put("hasUninitializedClients", hasUninitializedClients);

		mav.put("initClientPath", datarouterWebPaths.datarouter.client.initClient.toSlashedString());
		mav.put("initAllClientsPath", datarouterWebPaths.datarouter.client.initAllClients.toSlashedString());
		mav.put("inspectClientPath", datarouterWebPaths.datarouter.client.inspectClient.toSlashedString());

		return mav;
	}

	public static class RoutersRowJspDto{

		private final String clientName;
		private final String clientTypeName;
		private final Boolean initialized;
		private final CheckResultJspDto checkResult;

		public RoutersRowJspDto(String clientName, String clientTypeName, Boolean initialized,
				CheckResultJspDto checkResult){
			this.clientName = clientName;
			this.clientTypeName = clientTypeName;
			this.initialized = initialized;
			this.checkResult = checkResult;
		}

		public String getClientName(){
			return clientName;
		}

		public Boolean getInitialized(){
			return initialized;
		}

		public String getClientTypeName(){
			return clientTypeName;
		}

		public CheckResultJspDto getCheckResult(){
			return checkResult;
		}

	}

}
