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
package io.datarouter.aws.sqs.web;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.number.NumberTool;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;

public class SqsWebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private SqsClientManager sqsClientManager;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClientOptions clientOptions;

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
				buildQueueNodeTable(clientId),
				buildReferenceTable())
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - SQS")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private ContainerTag buildQueueNodeTable(ClientId clientId){
		List<SqsWebInspectorDto> queueStatsRows = nodes.getPhysicalNodesForClient(clientId.getName()).stream()
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (BaseSqsNode<?,?,?>)physicalNode)
				.map(BaseSqsNode::getQueueUrlAndName)
				.map(Supplier::get)
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
				.sorted(Comparator.comparing(dto -> dto.queueName))
				.collect(Collectors.toList());

		var table = new J2HtmlTable<SqsWebInspectorDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Queue Name", row -> row.queueName)
				.withColumn("Messages Available For Retrieval", row -> row.messagesAvailableForRetrieval)
				.withColumn("Messages Delayed", row -> row.messagesDelayed)
				.withColumn("Messages InFlight", row -> row.messagesInFlight)
				.withColumn("Total Messages (Available + InFlight)", SqsWebInspectorDto::getTotalMessagesAvailable)
				.build(queueStatsRows);
		ContainerTag header = h4("Queues");
		return div(header, table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

	private ContainerTag buildReferenceTable(){
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
				.build();
	}

	private static class SqsWebInspectorDto{

		private final String queueName;
		private final String messagesAvailableForRetrieval;
		private final String messagesDelayed;
		private final String messagesInFlight;

		public SqsWebInspectorDto(
				String queueName,
				String messagesAvailableForRetrieval,
				String messagesDelayed,
				String messagesInFlight){
			this.queueName = queueName;
			this.messagesAvailableForRetrieval = messagesAvailableForRetrieval;
			this.messagesDelayed = messagesDelayed;
			this.messagesInFlight = messagesInFlight;
		}

		public String getTotalMessagesAvailable(){
			long available = NumberTool.getLongNullSafe(messagesAvailableForRetrieval, 0L);
			long inFlight = NumberTool.getLongNullSafe(messagesInFlight, 0L);
			long total = available + inFlight;
			return NumberFormatter.addCommas(total);
		}

	}

}
