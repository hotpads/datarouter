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
package io.datarouter.client.gcp.pubsub.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.client.gcp.pubsub.GcpPubsubClientType;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager.GcpPubsubMetricDto;
import io.datarouter.client.gcp.pubsub.config.DatarouterGcpPubsubPaths;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubPhysicalNode;
import io.datarouter.client.gcp.pubsub.web.handler.GcpPubsubUpdateQueueHandler;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.H4Tag;

public class GcpPubsubWebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;
	@Inject
	private DatarouterGcpPubsubPaths paths;
	@Inject
	private GcpPubsubClientManager clientManager;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, GcpPubsubClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}

		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var content = div(
				buildClientPageHeader(clientName),
				buildClientOptionsTable(allClientOptions),
				buildQueueNodeTable(clientId, request),
				buildReferenceTable())
				.withClass("container my-3");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - GCP Pubsub")
				.withContent(content)
				.buildMav();
	}

	private DivTag buildQueueNodeTable(ClientId clientId, HttpServletRequest request){
		List<GcpPubsubMetricDto> queueStatsRows = Scanner.of(nodes.getPhysicalNodesForClient(clientId.getName()))
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (GcpPubsubPhysicalNode<?,?,?>)physicalNode)
				.map(baseGcpPubsubNode -> clientManager.getGcpMetricDto(
						baseGcpPubsubNode.getTopicAndSubscriptionName().get(), clientId))
				.sort(Comparator.comparing(GcpPubsubMetricDto::queueName))
				.list();

		var table = new J2HtmlTable<GcpPubsubMetricDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Queue Name", GcpPubsubMetricDto::queueName)
				.withColumn("Number of Undelivered Messages", row -> row.numUndeliveredMessages()
						.map(Object::toString)
						.orElse("error"))
				.withColumn("Age of Oldest Unacknowledged Message", row -> row.oldestUnackedMessageAgeS()
						.map(age -> new DatarouterDuration(age, TimeUnit.SECONDS))
						.map(Object::toString)
						.orElse("error"))
				.withHtmlColumn(th("Purge Queue").withClass("col-xs-1"), row -> {
					String href = buildActionPath(request, clientId, row.queueName());
					ATag purgeIcon = a(i().withClass("fas fa-skull-crossbones fa-lg"))
							.withHref(href)
							.attr("data-toggle", "tooltip")
							.attr("title", "Purge queue " + row.queueName())
							.attr("onclick", "return confirm('Are you sure you want to purge this queue "
							+ row.queueName() + "?');");
					return td(purgeIcon).withStyle("text-align:center");
				})
				.build(queueStatsRows);
		H4Tag header = h4("Queues");
		return div(header, table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private String buildActionPath(
			HttpServletRequest request,
			ClientId clientId,
			String queueName){
		String referer = request.getRequestURI() + "?" + request.getQueryString();
		URIBuilder uriBuilder = new URIBuilder()
				.addParameter(GcpPubsubUpdateQueueHandler.PARAM_clientName, clientId.getName())
				.addParameter(GcpPubsubUpdateQueueHandler.PARAM_referer, referer);
		PathNode path;
		path = paths.datarouter.gcpPubsub.purgeQueue;
		uriBuilder.addParameter(GcpPubsubUpdateQueueHandler.PARAM_queueName, queueName);

		return uriBuilder.setPath(request.getContextPath() + path.toSlashedString()).toString();
	}

	private DivTag buildReferenceTable(){
		return new J2HtmlLegendTable()
				.withHeader("Legend")
				.withClass("sortable table table-sm my-4 border")
				.withEntry("Number of Undelivered Messages",
						"Number of unacknowledged messages (a.k.a. backlog messages) in a subscription")
				.withEntry("Age of Oldest Unacknowledged Message",
						"The Age (in seconds) of the oldest unacknowledged message (a.k.a. backlog message) in a "
						+ "subscription")
				.build();
	}

}
