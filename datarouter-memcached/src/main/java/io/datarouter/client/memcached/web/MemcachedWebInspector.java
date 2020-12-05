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
package io.datarouter.client.memcached.web;

import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.p;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.options.MemcachedOptions;
import io.datarouter.client.memcached.client.spy.SpyMemcachedClient;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientType;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class MemcachedWebInspector implements DatarouterClientWebInspector{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private MemcachedClientManager memcachedClientManager;
	@Inject
	private MemcachedOptions memcachedOptions;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, ClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}

		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var content = div(
				buildClientPageHeader(clientName),
				buildOverview(clientId),
				buildClientOptionsTable(allClientOptions),
				buildStats(getClient(clientId).getStats()))
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - " + clientOptions.getClientType(clientId))
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	protected SpyMemcachedClient getClient(ClientId clientId){
		return memcachedClientManager.getSpyMemcachedClient(clientId);
	}

	protected Pair<Integer,ContainerTag> getDetails(ClientId clientId){
		Pair<Integer,ContainerTag> nodeCountByNodeTag = new Pair<>();
		List<ContainerTag> socketAddresses = memcachedOptions.getServers(clientId.getName()).stream()
				.map(InetSocketAddress::toString)
				.map(TagCreator::li)
				.collect(Collectors.toList());
		ContainerTag div = div(ul(socketAddresses.toArray(new ContainerTag[0])));
		nodeCountByNodeTag.setLeft(socketAddresses.size());
		nodeCountByNodeTag.setRight(div);
		return nodeCountByNodeTag;
	}

	private ContainerTag buildOverview(ClientId clientId){
		Pair<Integer,ContainerTag> listElements = getDetails(clientId);
		return div(
				p(b("Number of nodes: " + listElements.getLeft())),
				h4("Nodes"),
				listElements.getRight());
	}

	private ContainerTag buildStats(Map<SocketAddress,Map<String,String>> statsPerSocketAddress){
		var allStats = div();
		statsPerSocketAddress.entrySet().stream()
				.map(entry -> buildSingleNodeStats(entry.getKey().toString(), entry.getValue()))
				.forEach(allStats::with);
		return allStats;
	}

	private ContainerTag buildSingleNodeStats(String socketAddress, Map<String,String> stats){
		var tbody = TagCreator.tbody();
		stats.entrySet().stream()
				.sorted(Entry.comparingByKey())
				.map(entry -> tr(th(entry.getKey()), td(entry.getValue())))
				.forEach(tbody::with);
		var table = table(tbody).withClass("table table-striped table-hover table-sm");
		return div(h4(socketAddress + " Node Details"), table);
	}

}
