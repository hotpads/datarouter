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
package io.datarouter.aws.sqs.web.handler;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.i;

import java.util.List;

import javax.inject.Inject;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.service.SqsQueueRegistryService;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class SqsUpdateQueueHandler extends BaseHandler{

	public static final String PARAM_clientName = "clientName";
	public static final String PARAM_queueName = "queueName";
	public static final String PARAM_referer = "referer";

	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private SqsClientManager sqsClientManager;
	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private SqsQueueRegistryService queueRegistryService;

	@Handler
	private Mav deleteQueue(
			@Param(PARAM_clientName) String clientName,
			@Param(PARAM_queueName) String queueName,
			@Param(PARAM_referer) String referer){
		ClientId clientId = datarouterClients.getClientId(clientName);
		AmazonSQS sqs = sqsClientManager.getAmazonSqs(clientId);
		GetQueueUrlResult queueUrlResult = sqs.getQueueUrl(queueName);
		sqs.deleteQueue(queueUrlResult.getQueueUrl());
		String message = "Deleted unreferenced SQS queue: " + queueName;
		var dto = new DatarouterChangelogDtoBuilder(
				"Sqs",
				queueName,
				"deleteQueue",
				getSessionInfo().getRequiredSession().getUsername())
				.build();
		changelogRecorder.record(dto);
		return buildPage(referer, message);
	}

	@Handler
	private Mav deleteAllUnreferencedQueues(
			@Param(PARAM_clientName) String clientName,
			@Param(PARAM_referer) String referer){
		ClientId clientId = datarouterClients.getClientId(clientName);
		List<String> unreferencedQueueNames = queueRegistryService.getSqsQueuesForClient(clientId).getRight();
		AmazonSQS sqs = sqsClientManager.getAmazonSqs(clientId);
		Scanner.of(unreferencedQueueNames)
				.map(sqs::getQueueUrl)
				.map(GetQueueUrlResult::getQueueUrl)
				.forEach(sqs::deleteQueue);
		String message = "Deleted all unreferenced SQS queues";
		Scanner.of(unreferencedQueueNames)
				.map(queueName -> new DatarouterChangelogDtoBuilder(
						"Sqs",
						queueName,
						"delete unreferenced queue",
						getSessionInfo().getRequiredSession().getUsername()))
				.map(DatarouterChangelogDtoBuilder::build)
				.forEach(changelogRecorder::record);
		return buildPage(referer, message);
	}

	@Handler
	private Mav purgeQueue(
			@Param(PARAM_clientName) String clientName,
			@Param(PARAM_queueName) String queueName,
			@Param(PARAM_referer) String referer){
		ClientId clientId = datarouterClients.getClientId(clientName);
		AmazonSQS sqs = sqsClientManager.getAmazonSqs(clientId);
		GetQueueUrlResult queueUrlResult = sqs.getQueueUrl(queueName);
		sqs.purgeQueue(new PurgeQueueRequest(queueUrlResult.getQueueUrl()));
		String message = "Purged SQS queue: " + queueName;
		var dto = new DatarouterChangelogDtoBuilder(
				"Sqs",
				queueName,
				"purgeQueue",
				getSessionInfo().getRequiredSession().getUsername())
				.build();
		changelogRecorder.record(dto);
		return buildPage(referer, message);
	}

	private Mav buildPage(String href, String message){
		DivTag backButton = div(a(i().withClass("fas fa-angle-left"))
				.withText("Go back to client details")
				.withHref(href)
				.withClass("btn btn-primary"));

		DivTag content = div(
				backButton,
				div(message).withClass("my-4"))
				.withClass("container my-4");

		return pageFactory.startBuilder(request)
				.withContent(content)
				.withTitle("Update Sqs Queue")
				.buildMav();
	}

}
