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
package io.datarouter.client.redis.web;

import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.ul;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.client.redis.client.RedisOptions;
import io.datarouter.storage.client.ClientId;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import redis.clients.jedis.Jedis;

public class RedisWebInspector implements DatarouterClientWebInspector{

	@Inject
	private RedisOptions redisOptions;
	@Inject
	private RedisClientManager redisClientManager;
	@Inject
	private DatarouterWebRequestParamsFactory datarouterWebRequestParamsFactory;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = datarouterWebRequestParamsFactory.new DatarouterWebRequestParams<>(params,
				RedisClientType.class);
		ClientId clientId = clientParams.getClientId();
		var content = div(
				h2("Datarouter " + clientId.getName()),
				DatarouterClientWebInspector.buildNav(servletContext.get().getContextPath(), clientId.getName()),
				h3("Client Summary"),
				buildOverview(clientId))
				.withClass("container my-3");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Redis")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private ContainerTag buildOverview(ClientId clientId){
		String mode = redisOptions.getClientMode(clientId.getName());

		String clusterEndpoint = "";
		if(mode.equals(RedisOptions.DYNAMIC_CLIENT_MODE)){
			clusterEndpoint = redisOptions.getClusterEndpoint(clientId.getName()).get().toString();
		}
		Jedis client = redisClientManager.getJedis(clientId);
		var infoList = Arrays.stream(client.clusterInfo().split(" "))
				.collect(Collectors.toList());
		var infoDiv = ul(each(infoList, TagCreator::li));
		return dl(
				dt("Client mode:"), dd(mode),
				dt("Cluster endpoint:"), dd(clusterEndpoint),
				dt("Cluster info"), dd(infoDiv));
	}

}
