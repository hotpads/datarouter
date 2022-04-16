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
package io.datarouter.aws.rds.job;

import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.pre;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.aws.rds.config.DatarouterAwsPaths;
import io.datarouter.aws.rds.service.AuroraDnsService;
import io.datarouter.aws.rds.service.AuroraDnsService.DnsHostEntryDto;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.type.DatarouterEmailTypes.AwsRdsEmailType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.PreTag;

public class AuroraDnsMonitoringJob extends BaseJob{

	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private AuroraDnsService dnsService;
	@Inject
	private DatabaseAdministrationConfiguration config;
	@Inject
	private DatarouterAwsPaths paths;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private AwsRdsEmailType awsRdsEmailType;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private AdminEmail adminEmail;

	@Override
	public void run(TaskTracker tracker){
		List<DnsHostEntryDto> mismatchedEntries = dnsService.checkClientEndpoint().getRight();
		if(mismatchedEntries.isEmpty()){
			return;
		}
		Scanner.of(mismatchedEntries)
				.map(config::fixDatabaseDns)
				.flush(fixes -> sendEmail(mismatchedEntries, fixes));
		mismatchedEntries.forEach(entry -> recordChangelog(entry.getInstanceHostname()));
	}

	private void sendEmail(List<DnsHostEntryDto> mismatchedReaderEntries, List<PreTag> fixes){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.auroraInstances.inspectClientUrl)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Aurora DNS")
				.withTitleHref(primaryHref)
				.withContent(makeEmailContent(mismatchedReaderEntries, fixes))
				.fromAdmin()
				.toAdmin()
				.toSubscribers()
				.to(awsRdsEmailType.tos);
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private ContainerTag<?> makeEmailContent(List<DnsHostEntryDto> mismatchedReaderEntries, List<PreTag> fixes){
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		var message = h3("Some of the reader DB instances are pointed to the writer instance or missing entries");
		var table = new J2HtmlEmailTable<DnsHostEntryDto>()
				.withColumn("client name", DnsHostEntryDto::getClientName)
				.withColumn("hostname", DnsHostEntryDto::getHostname)
				.withColumn("instance hostname", DnsHostEntryDto::getInstanceHostname)
				.withColumn("ip", DnsHostEntryDto::getIp)
				.build(mismatchedReaderEntries);
		var fixHeader = h3("Executing suggested fixes to reset DNS:");
		var fixList = pre();
		fixes.forEach(fix -> fixList
				.with(fix)
				.with(br()));
		return body(header, message, table, div(fixHeader, fixList));
	}

	private void recordChangelog(String database){
		var dto = new DatarouterChangelogDtoBuilder("AuroraDns", database, "mismatch", adminEmail.get())
				.build();
		changelogRecorder.record(dto);
	}

}
