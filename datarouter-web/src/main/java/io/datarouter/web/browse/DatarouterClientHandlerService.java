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
package io.datarouter.web.browse;

import static j2html.TagCreator.a;
import static j2html.TagCreator.caption;
import static j2html.TagCreator.div;
import static j2html.TagCreator.span;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.monitoring.latency.CheckResult.CheckResultJspDto;
import io.datarouter.web.monitoring.latency.LatencyMonitoringService;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterClientHandlerService{

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private LatencyMonitoringService monitoringService;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterWebPaths paths;

	public DivTag buildClientsTable(){
		boolean hasUninitializedClients = false;
		List<ClientsJspDto> clients = new ArrayList<>();
		for(ClientId clientId : datarouterClients.getClientIds()){
			boolean initialized = clientInitializationTracker.isInitialized(clientId);
			hasUninitializedClients = hasUninitializedClients || !initialized;
			String clientTypeName = datarouterClients.getClientTypeInstance(clientId).getName();
			CheckResultJspDto checkResultJspDto = new CheckResultJspDto(
					monitoringService.getLastResultForDatarouterClient(clientId),
					monitoringService.getGraphLinkForDatarouterClient(clientId));
			clients.add(new ClientsJspDto(clientId.getName(), clientTypeName, initialized, checkResultJspDto));
		}

		String servletContextPath = servletContext.get().getContextPath();
		var tbody = tbody();

		clients.forEach(clientsJspDto -> {
			String clientName = clientsJspDto.clientName;
			TdTag leftTd;
			TdTag rightTd;
			if(clientsJspDto.initialized){
				ContainerTag<?> latencyGraphTag;
				CheckResultJspDto checkResultJspDto = clientsJspDto.checkResult;
				if(checkResultJspDto == null || checkResultJspDto.getCheckResult() == null){
					latencyGraphTag = span().withClass("status");
				}else{
					latencyGraphTag = a()
							.withHref(checkResultJspDto.getGraphLink())
							.withClass("status " + checkResultJspDto.getCssClass());
				}
				String inspectLinkPath = servletContextPath + paths.datarouter.client.inspectClient.toSlashedString()
						+ "?clientName=" + clientName;
				leftTd = td(TagCreator.join(
						latencyGraphTag,
						a(clientName).withHref(inspectLinkPath)));
				rightTd = td(clientsJspDto.clientTypeName);
			}else{
				leftTd = td(TagCreator.join(
						span().withClass("status"),
						clientName));
				String initLinkPath = servletContextPath + paths.datarouter.client.initClient.toSlashedString()
						+ "?clientName=" + clientName;
				rightTd = td(TagCreator.join("[", a("init").withHref(initLinkPath), "]"));
			}
			tbody.with(tr(leftTd, rightTd));
		});

		var caption = caption("Clients");
		if(hasUninitializedClients){
			String linkPath = servletContextPath + paths.datarouter.client.initAllClients.toSlashedString();
			caption = caption(TagCreator.join("Clients [", a("init remaining clients").withHref(linkPath), "]"));
		}

		var table = table(caption.withStyle("caption-side: top"), tbody)
				.withClass("table table-striped table-bordered table-sm");
		return div(table);
	}

	public static class ClientsJspDto{

		private final String clientName;
		private final String clientTypeName;
		private final Boolean initialized;
		private final CheckResultJspDto checkResult;

		public ClientsJspDto(String clientName, String clientTypeName, Boolean initialized,
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
