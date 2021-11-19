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
package io.datarouter.aws.sqs.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;

import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.aws.sqs.config.DatarouterSqsPaths;
import io.datarouter.aws.sqs.service.SqsQueueRegistryService;
import io.datarouter.aws.sqs.web.handler.SqsUpdateQueueHandler;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.ATag;

public class SqsWebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private SqsClientManager sqsClientManager;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;
	@Inject
	private DatarouterSqsPaths paths;
	@Inject
	private SqsQueueRegistryService queueRegistryService;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, SqsClientType.class);
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
				.withClass("container my-4");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - SQS")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private ContainerTag<?> buildQueueNodeTable(ClientId clientId, HttpServletRequest request){
		Pair<List<Twin<String>>,List<String>> queueRegistry = queueRegistryService.getSqsQueuesForClient(clientId);
		List<Twin<String>> knownQueueUrlByName = queueRegistry.getLeft();
		List<SqsWebInspectorDto> queueStatsRows = Scanner.of(knownQueueUrlByName)
				.map(queueUrlAndName -> {
					String queueName = queueUrlAndName.getRight();
					String queueUrl = queueUrlAndName.getLeft();
					Map<String,String> attributesMap = sqsClientManager.getAllQueueAttributes(clientId, queueUrl);
					return new SqsWebInspectorDto(
							queueName,
							attributesMap.get(QueueAttributeName.ApproximateNumberOfMessages.name()),
							attributesMap.get(QueueAttributeName.ApproximateNumberOfMessagesDelayed.name()),
							attributesMap.get(QueueAttributeName.ApproximateNumberOfMessagesNotVisible.name()));
				})
				.sort(Comparator.comparing(dto -> dto.queueName))
				.list();

		var table = new J2HtmlTable<SqsWebInspectorDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Queue Name").withClass("col-xs-5"), row -> td(row.queueName))
				.withHtmlColumn(th("Available For Retrieval").withClass("col-xs-3"),
						row -> td(row.messagesAvailableForRetrieval))
				.withHtmlColumn(th("Delayed").withClass("col-xs-1"), row -> td(row.messagesDelayed))
				.withHtmlColumn(th("InFlight").withClass("col-xs-1"), row -> td(row.messagesInFlight))
				.withHtmlColumn(th("Total").withClass("col-xs-1"), row -> td(row.getTotalMessagesAvailable()))
				.withHtmlColumn(th("").attr("width", "80"), row -> {
					String href = buildActionPath(request, clientId, row.queueName, SqsQueueAction.PURGE);
					ATag purgeIcon = a(i().withClass("fas fa-skull-crossbones fa-lg"))
							.withHref(href)
							.attr("data-toggle", "tooltip")
							.attr("title", "Purge queue " + row.queueName);
					return td(purgeIcon).withStyle("text-align:center");
				})
				.build(queueStatsRows);
		List<String> unreferencedQueues = queueRegistry.getRight();
		if(unreferencedQueues.isEmpty()){
			return div(h4("Queues"), table)
					.withClass("container-fluid my-4")
					.withStyle("padding-left: 0px");
		}
		var unreferencedQueuesTable = new J2HtmlTable<String>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Queue Name"), TagCreator::td)
				.withHtmlColumn(
						th(a("Delete All")
								.withHref(buildActionPath(request, clientId, "", SqsQueueAction.DELETE_ALL)))
								.withStyle("text-align:center")
								.attr("width", "80"),
						row -> {
							String href = buildActionPath(request, clientId, row, SqsQueueAction.DELETE);
							ATag trashIcon = a(i().withClass("fas fa-trash fa-lg"))
									.withHref(href)
									.attr("data-toggle", "tooltip")
									.attr("title", "Delete queue " + row);
							return td(trashIcon).withStyle("text-align:center");
						})
				.build(unreferencedQueues);
		return div(h4("Queues"), table, h4("Unreferenced Queues"), unreferencedQueuesTable)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private ContainerTag<?> buildReferenceTable(){
		return new J2HtmlLegendTable()
				.withHeader("Legend")
				.withClass("sortable table table-sm my-4 border")
				.withEntry(
						"Messages Available For Retrieval",
						"The approximate number of messages available for retrieval from the queue")
				.withEntry(
						"Messages Delayed",
						"The approximate number of messages in the queue that are delayed and not available for reading"
								+ " immediately. This can happen when the queue is configured as a delay queue or when "
								+ "a message has been sent with a delay parameter.")
				.withEntry(
						"Messages In Flight",
						"Messages are considered to be in flight if they have been sent to a client but have not yet "
								+ "been deleted or have not yet reached the end of their visibility window.")
				.withEntry("Total Messages", "A total of Available + InFlight messages")
				.withEntry("Unreferenced Queue", "Queue which exists but the application is not aware of, "
						+ "usually a result of the queue being renamed, or code refactored")
				.build()
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private String buildActionPath(
			HttpServletRequest request,
			ClientId clientId,
			String queueName,
			SqsQueueAction action){
		String referer = request.getRequestURI() + "?" + request.getQueryString();
		URIBuilder uriBuilder = new URIBuilder()
				.addParameter(SqsUpdateQueueHandler.PARAM_clientName, clientId.getName())
				.addParameter(SqsUpdateQueueHandler.PARAM_referer, referer);
		PathNode path;
		if(action == SqsQueueAction.DELETE){
			path = paths.datarouter.sqs.deleteQueue;
			uriBuilder.addParameter(SqsUpdateQueueHandler.PARAM_queueName, queueName);
		}else if(action == SqsQueueAction.PURGE){
			path = paths.datarouter.sqs.purgeQueue;
			uriBuilder.addParameter(SqsUpdateQueueHandler.PARAM_queueName, queueName);
		}else if(action == SqsQueueAction.DELETE_ALL){
			path = paths.datarouter.sqs.deleteAllUnreferencedQueues;
		}else{
			return null;
		}
		return uriBuilder.setPath(request.getContextPath() + path.toSlashedString()).toString();
	}

	private static class SqsWebInspectorDto{

		private final String queueName;
		private final String messagesAvailableForRetrieval;
		private final String messagesDelayed;
		private final String messagesInFlight;

		private SqsWebInspectorDto(
				String queueName,
				String messagesAvailableForRetrieval,
				String messagesDelayed,
				String messagesInFlight){
			this.queueName = queueName;
			this.messagesAvailableForRetrieval = messagesAvailableForRetrieval;
			this.messagesDelayed = messagesDelayed;
			this.messagesInFlight = messagesInFlight;
		}

		private String getTotalMessagesAvailable(){
			long available = NumberTool.getLongNullSafe(messagesAvailableForRetrieval, 0L);
			long inFlight = NumberTool.getLongNullSafe(messagesInFlight, 0L);
			long total = available + inFlight;
			return NumberFormatter.addCommas(total);
		}

	}

	private enum SqsQueueAction{
		DELETE,
		DELETE_ALL,
		PURGE
	}

}
