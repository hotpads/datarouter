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

import static j2html.TagCreator.div;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.H3Tag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SqsQueuesDailyDigest implements DailyDigest{

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private SqsQueueRegistryService queueRegistryService;
	@Inject
	private DailyDigestService digestService;

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
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		ClientId clientId = Scanner.of(datarouterClients.getClientIds())
				.include(client -> datarouterClients.getClientTypeInstance(client) instanceof SqsClientType)
				.findFirst()
				.orElse(null);
		if(clientId == null){
			return Optional.empty();
		}

		List<String> unreferencedQueues = queueRegistryService.getSqsQueuesForClient(clientId).unreferencedQueues();
		if(unreferencedQueues.isEmpty()){
			return Optional.empty();
		}

		TableTag unreferencedQueuesTable = new J2HtmlEmailTable<String>()
				.withColumn("Queue Name", row -> row)
				.build(unreferencedQueues);
		H3Tag header = digestService.makeHeader(
				"Unreferenced Sqs Queues",
				paths.datarouter.client.inspectClient,
				"?clientName=sqs");

		return Optional.of(div(header, unreferencedQueuesTable));
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		ClientId clientId = Scanner.of(datarouterClients.getClientIds())
				.include(client -> datarouterClients.getClientTypeInstance(client) instanceof SqsClientType)
				.findFirst()
				.orElse(null);
		if(clientId == null){
			return Optional.empty();
		}

		List<String> unreferencedQueues = queueRegistryService.getSqsQueuesForClient(clientId).unreferencedQueues();
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

}
