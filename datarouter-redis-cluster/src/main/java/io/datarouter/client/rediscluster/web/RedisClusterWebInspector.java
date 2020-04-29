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
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.pre;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.client.rediscluster.RedisClusterClientType;
import io.datarouter.client.rediscluster.client.RedisClusterClientManager;
import io.datarouter.client.rediscluster.client.RedisClusterOptions;
import io.datarouter.client.rediscluster.client.RedisClusterOptions.RedisClusterClientMode;
import io.datarouter.storage.client.ClientId;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;
import redis.clients.jedis.JedisPool;

public class RedisClusterWebInspector implements DatarouterClientWebInspector{

	@Inject
	private RedisClusterOptions options;
	@Inject
	private RedisClusterClientManager clientManager;
	@Inject
	private DatarouterWebRequestParamsFactory datarouterWebRequestParamsFactory;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = datarouterWebRequestParamsFactory.new DatarouterWebRequestParams<>(params,
				RedisClusterClientType.class);
		ClientId clientId = clientParams.getClientId();
		var content = div(
				h2("Datarouter " + clientId.getName()),
				DatarouterClientWebInspector.buildNav(servletContext.get().getContextPath(), clientId.getName()),
				h3("Client Summary"),
				buildOverview(clientId))
				.withClass("container my-3");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Redis-Cluster")
				.withContent(content)
				.buildMav();
	}

	private ContainerTag buildOverview(ClientId clientId){
		RedisClusterClientMode clientMode = options.getClientMode(clientId.getName());
		String endpoint = "";
		if(clientMode == RedisClusterClientMode.AUTO_DISCOVERY){
			endpoint = options.getClusterEndpoint(clientId.getName()).get().toString();
		}else{
			endpoint = options.getNodes(clientId.getName()).stream()
					.map(InetSocketAddress::toString)
					.collect(Collectors.joining("\n"));
		}
		Set<String> nodes = clientManager.getJedis(clientId).getClusterNodes().keySet();
		ContainerTag clusterInfo = clientManager.getJedis(clientId).getClusterNodes().values().stream()
				.findFirst()
				.map(JedisPool::getResource)
				.map(jedis -> {
					return div(pre(jedis.clusterInfo()), pre(jedis.info()));
				})
				.get();
		return dl(
				dt("Client mode:"), dd(clientMode.getPersistentString()),
				dt("Endpoint:"), dd(endpoint),
				dt("Nodes"), dd(String.join("\n", nodes)),
				dt("Info"), dd(clusterInfo));
	}

}
