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
package io.datarouter.client.redis.web;

import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.each;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class RedisWebInspector implements DatarouterClientWebInspector{
	private static final Logger logger = LoggerFactory.getLogger(RedisWebInspector.class);

	@Inject
	private RedisClientManager clientManager;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, RedisClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}
		if(clientManager.getMode(clientId).isStandard()){
			return inspectRegularRedis(clientId, request);
		}
		return inspectClusterClient(clientId, request);
	}

	private Mav inspectRegularRedis(ClientId clientId, HttpServletRequest request){
		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var content = div(
				buildClientPageHeader(clientName),
				buildClientOptionsTable(allClientOptions),
				buildRegularOverview(clientId))
				.withClass("container my-3");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Redis")
				.withContent(content)
				.buildMav();
	}

	private ContainerTag buildRegularOverview(ClientId clientId){
		ContainerTag infoDiv = null;
		try{
			infoDiv = pre(clientManager.getClient(clientId).info().get());
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
		return dl(dt("Info:"), dd(infoDiv));
	}

	private Mav inspectClusterClient(ClientId clientId, HttpServletRequest request){
		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		ContainerTag overview = null;
		try{
			overview = buildClusterOverview(clientId);
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
		var content = div(
				buildClientPageHeader(clientName),
				buildClientOptionsTable(allClientOptions),
				overview)
				.withClass("container my-3");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Redis-Cluster")
				.withContent(content)
				.buildMav();
	}

	private ContainerTag buildClusterOverview(ClientId clientId) throws InterruptedException, ExecutionException{
		var client = clientManager.getClient(clientId);
		List<String> clusterNodes = Scanner.of(client.clusterNodes().get().split("\n"))
				.list();
		String clusterInfo = client.clusterInfo().get();
		String info = client.info().get();
		return dl(
				dt("Nodes: " + clusterNodes.size()), dd(ul(each(clusterNodes, TagCreator::li))),
				dt("Cluster Info:"), dd(pre(clusterInfo)),
				dt("Info:"), dd(pre(info)));
	}

}
