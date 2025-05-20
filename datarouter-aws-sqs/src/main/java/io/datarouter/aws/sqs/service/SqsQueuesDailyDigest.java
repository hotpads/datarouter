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
package io.datarouter.aws.sqs.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SqsQueuesDailyDigest implements DailyDigest{

	private static final String SQS_CATEGORY = "sqs";
	private static final String UNREFERENCED_CATEGORY = "unreferenced";

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private SqsQueueRegistryService queueRegistryService;
	@Inject
	private DailyDigestRmlService digestService;

	@Override
	public String getTitle(){
		return "Sqs Queues";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<String> unreferencedQueues = getUnreferencedQueues();
		if(unreferencedQueues.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Unreferenced Sqs Queues", paths.datarouter.client.inspectClient,
						"?clientName=" + SqsClientType.NAME),
				Rml.table(
						Rml.tableRow(Rml.tableHeader(Rml.text("Queue Name"))))
						.with(unreferencedQueues.stream()
								.map(Rml::text)
								.map(Rml::tableCell)
								.map(Rml::tableRow))));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return Scanner.of(getUnreferencedQueues())
				.map(unreferenced -> new DailyDigestPlatformTask(
						List.of(SQS_CATEGORY, UNREFERENCED_CATEGORY, unreferenced),
						List.of(SQS_CATEGORY, UNREFERENCED_CATEGORY),
						"Unreferenced SQS queue " + unreferenced,
						Rml.paragraph(
								Rml.text("Sqs queue "), Rml.text(unreferenced).code(), Rml.text(" is unreferenced. "),
								digestService.makeLink("View queues", paths.datarouter.client.inspectClient,
										"?clientName=" + SqsClientType.NAME))))
				.list();
	}

	private List<String> getUnreferencedQueues(){
		ClientId clientId = Scanner.of(datarouterClients.getClientIds())
				.include(client -> datarouterClients.getClientTypeInstance(client) instanceof SqsClientType)
				.findFirst()
				.orElse(null);
		if(clientId == null){
			return List.of();
		}

		return queueRegistryService.getSqsQueuesForClient(clientId).unreferencedQueues();
	}

}
