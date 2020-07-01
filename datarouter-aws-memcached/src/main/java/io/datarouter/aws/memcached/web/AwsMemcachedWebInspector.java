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
package io.datarouter.aws.memcached.web;

import static j2html.TagCreator.b;
import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.p;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.aws.memcached.AwsMemcachedClientType;
import io.datarouter.aws.memcached.client.AwsMemcachedOptions;
import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.SpyMemcachedClient;
import io.datarouter.client.memcached.web.MemcachedWebInspector;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import net.spy.memcached.ClientMode;

public class AwsMemcachedWebInspector implements DatarouterClientWebInspector{

	@Inject
	private AwsMemcachedOptions options;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private MemcachedClientManager memcachedClientManager;
	@Inject
	private MemcachedWebInspector memcachedWebInspector;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(
				params,
				AwsMemcachedClientType.class);
		ClientId clientId = clientParams.getClientId();

		SpyMemcachedClient spyClient = memcachedClientManager.getSpyMemcachedClient(clientId);
		String clientName = clientParams.getClientId().getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var clientOptionsTable = buildClientOptionsTable(allClientOptions);
		var content = div(
				h2("Datarouter " + clientId.getName()),
				DatarouterClientWebInspector.buildNav(servletContext.get().getContextPath(), clientId.getName()),
				h3("Client Summary"),
				p(b("Client Name: " + clientName)),
				clientOptionsTable,
				buildOverview(clientId),
				memcachedWebInspector.buildStats(spyClient.getStats()))
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - AWS Memcached")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private ContainerTag buildOverview(ClientId clientId){
		SpyMemcachedClient spyClient = memcachedClientManager.getSpyMemcachedClient(clientId);
		ClientMode mode = options.getClientMode(clientId.getName());

		String clusterEndpoint = "";
		List<ContainerTag> servers;
		if(mode == ClientMode.Dynamic){
			clusterEndpoint = options.getClusterEndpoint(clientId.getName()).get().toString();
			servers = spyClient.getAllNodeEndPoints().stream()
					.map(Object::toString)
					.map(TagCreator::li)
					.collect(Collectors.toList());
		}else{
			servers = spyClient.getAvailableServers().stream()
					.map(Object::toString)
					.map(TagCreator::li)
					.collect(Collectors.toList());
		}
		var nodeList = ul(servers.toArray(new ContainerTag[servers.size()]));
		return dl(
				dt("Client mode:"), dd(mode.name()),
				dt("Cluster endpoint:"), dd(clusterEndpoint),
				dt("Number of nodes:"), dd(servers.size() + ""),
				dt("Nodes:"), dd(nodeList));
	}

}
