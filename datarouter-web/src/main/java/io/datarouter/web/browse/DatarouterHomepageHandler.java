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
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.DatarouterPropertiesService;
import io.datarouter.web.browse.widget.NodeWidgetDatabeanExporterLinkSupplier;
import io.datarouter.web.browse.widget.NodeWidgetTableCountLinkSupplier;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.monitoring.latency.CheckResult.CheckResultJspDto;
import io.datarouter.web.monitoring.latency.LatencyMonitoringService;
import io.datarouter.web.user.role.DatarouterUserRole;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class DatarouterHomepageHandler extends BaseHandler{

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private LatencyMonitoringService monitoringService;
	@Inject
	private DatarouterWebFiles files;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private NodeWidgetDatabeanExporterLinkSupplier nodeWidgetDatabeanExporterLink;
	@Inject
	private NodeWidgetTableCountLinkSupplier nodeWidgetTableCountLink;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterPropertiesService datarouterPropertiesService;

	@Handler(defaultHandler = true)
	protected Mav view(){
		Mav mav = new Mav(files.jsp.admin.datarouter.datarouterMenuJsp);

		//DatarouterProperties info
		mav.put("serverPropertiesTable", buildDatarouterPropertiesTable());

		//Clients
		var showStorage = getSessionInfo().hasRole(DatarouterUserRole.DATAROUTER_ADMIN);
		mav.put("showStorage", showStorage);
		if(showStorage){
			mav.put("clientsTable", buildClientsTable());

			mav.put("viewNodeDataPath", paths.datarouter.nodes.browseData.toSlashedString()
					+ "?submitAction=browseData&nodeName=");
			mav.put("getNodeDataPath", paths.datarouter.nodes.getData.toSlashedString() + "?nodeName=");
			mav.put("countKeysPath", paths.datarouter.nodes.browseData.toSlashedString() + "/countKeys?nodeName=");

			Optional<String> exporterLink = Optional.ofNullable(nodeWidgetDatabeanExporterLink.get())
					.map(link -> link + "?nodeName=");
			mav.put("exporterLink", exporterLink.orElse(""));
			mav.put("showExporterLink", exporterLink.isPresent());

			Optional<String> tableCountLink = Optional.ofNullable(nodeWidgetTableCountLink.get())
					.map(link -> link + "&nodeName=");
			mav.put("tableCountLink", tableCountLink.orElse(""));
			mav.put("showTableCountLink", tableCountLink.isPresent());
		}
		return mav;
	}

	private String buildDatarouterPropertiesTable(){
		var tbody = tbody();
		datarouterPropertiesService.getAllProperties().stream()
				.map(entry -> tr(td(entry.getLeft()), td(entry.getRight() == null ? "" : entry.getRight())))
				.forEach(tbody::with);
		var table = table(caption("Server Info").withStyle("caption-side: top"), tbody)
				.withClass("table table-striped table-bordered table-sm");
		return div(table)
				.withClass("col-12 col-sm-6")
				.render();
	}

	private String buildClientsTable(){
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
			ContainerTag<?> leftTd;
			ContainerTag<?> rightTd;
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
		return div(table)
				.withClass("col-12 col-sm-6")
				.render();
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
