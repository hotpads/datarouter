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
import static j2html.TagCreator.th;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.TableTag;

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
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<ContainerTag<?>> getPageContent(ZoneId zoneId){
		return buildContent(ContentType.PAGE);
	}

	@Override
	public Optional<ContainerTag<?>> getEmailContent(ZoneId zoneId){
		return buildContent(ContentType.EMAIL);
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	private Optional<ContainerTag<?>> buildContent(ContentType contentType){
		ClientId clientId = Scanner.of(datarouterClients.getClientIds())
				.include(client -> datarouterClients.getClientTypeInstance(client) instanceof SqsClientType)
				.findFirst()
				.orElse(null);
		if(clientId == null){
			return Optional.empty();
		}

		List<String> unreferencedQueues = queueRegistryService.getSqsQueuesForClient(clientId).getRight();
		if(unreferencedQueues.isEmpty()){
			return Optional.empty();
		}

		TableTag unreferencedQueuesTable = new TableTag();
		if(contentType == ContentType.PAGE){
			unreferencedQueuesTable = new J2HtmlTable<String>()
					.withClasses("sortable table table-sm table-striped my-4 border")
					.withHtmlColumn(th("Queue Name"), TagCreator::td)
					.build(unreferencedQueues);
		}else if(contentType == ContentType.EMAIL){
			unreferencedQueuesTable = new J2HtmlEmailTable<String>()
					.withColumn("Queue Name", row -> row)
					.build(unreferencedQueues);
		}
		ContainerTag<?> header = digestService.makeHeader(
				"Unreferenced Sqs Queues",
				paths.datarouter.client.inspectClient,
				"?clientName=sqs");

		return Optional.of(div(header, unreferencedQueuesTable));
	}

	private enum ContentType{
		PAGE,
		EMAIL
	}

}
