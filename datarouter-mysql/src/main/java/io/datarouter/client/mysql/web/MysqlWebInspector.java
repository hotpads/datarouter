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
package io.datarouter.client.mysql.web;

import static j2html.TagCreator.b;
import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.p;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.client.mysql.MysqlClientManager;
import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.connection.C3p0StatsDto;
import io.datarouter.client.mysql.connection.C3p0StatsService;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory.DatarouterWebRequestParams;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;

public class MysqlWebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private C3p0StatsService c3p0StatsService;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private ClientOptions clientOptions;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, MysqlClientType.class);
		if(clientParams.getClientId() == null){
			return new MessageMav("Client not found");
		}

		ClientId clientId = clientParams.getClientId();
		String clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var clientOptionsTable = buildClientOptionsTable(allClientOptions);
		var content = div(
				h2("Datarouter " + clientName),
				DatarouterClientWebInspector.buildNav(servletContext.get().getContextPath(), clientName),
				h3("Client Summary"),
				p(b("Client Name: " + clientName)),
				clientOptionsTable,
				getC3P0Stats(clientId, clientParams))
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Mysql")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private ContainerTag getC3P0Stats(ClientId clientId, DatarouterWebRequestParams<MysqlClientType> clientParams){
		MysqlClientManager clientManager = injector.getInstance(clientParams.getClientType().getClientManagerClass());
		Optional<C3p0StatsDto> c3p0Stats = c3p0StatsService.getC3p0Stats().stream()
				.filter(stats -> stats.clientName.equals(clientId.getName()))
				.findAny();
		var totalConnections = c3p0Stats
				.map(stats -> stats.total)
				.map(stats -> stats + "")
				.orElse("");
		var busyConnections = c3p0Stats
				.map(stats -> stats.busy)
				.map(stats -> stats + "")
				.orElse("");
		return dl(
				dt("handles:"), dd(clientManager.getStats(clientId)),
				dt("total connection:"), dd(totalConnections),
				dt("busy connection:"), dd(busyConnections));
	}

}
