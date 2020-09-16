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
package io.datarouter.client.rediscluster.web;

import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.each;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.client.rediscluster.RedisClusterClientType;
import io.datarouter.client.rediscluster.client.RedisClusterClientManager;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class RedisClusterWebInspector implements DatarouterClientWebInspector{

	@Inject
	private RedisClusterClientManager clientManager;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, RedisClusterClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}

		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var content = div(
				buildClientPageHeader(clientName),
				buildClientOptionsTable(allClientOptions),
				buildOverview(clientId))
				.withClass("container my-3");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Redis-Cluster")
				.withContent(content)
				.buildMav();
	}

	private ContainerTag buildOverview(ClientId clientId){
		var client = clientManager.getClient(clientId).sync();
		List<String> clusterNodes = Scanner.of(client.clusterNodes().split("\n"))
				.list();
		String clusterInfo = client.clusterInfo();
		String info = client.info();
		return dl(
				dt("Nodes: " + clusterNodes.size()), dd(ul(each(clusterNodes, TagCreator::li))),
				dt("Cluster Info:"), dd(pre(clusterInfo)),
				dt("Info:"), dd(pre(info)),
				dt("Lettuce Options:"),
						dd(pre(GsonTool.GSON_PRETTY_PRINT.toJson(clientManager.getClient(clientId).getOptions()))));
	}

}
