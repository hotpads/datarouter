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
package io.datarouter.aws.rds.job;

import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.rawHtml;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.aws.rds.config.DatarouterAwsPaths;
import io.datarouter.aws.rds.service.AuroraDnsService;
import io.datarouter.aws.rds.service.AuroraDnsService.DnsHostEntryDto;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import j2html.tags.ContainerTag;

public class AuroraDnsMonitoringJob extends BaseJob{

	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private AuroraDnsService dnsService;
	@Inject
	private DatabaseAdministrationConfiguration config;
	@Inject
	private DatarouterAdministratorEmailService additionalAdministratorEmailService;
	@Inject
	private DatarouterAwsPaths paths;
	@Inject
	private ChangelogRecorder changelogRecorder;

	@Override
	public void run(TaskTracker tracker){
		List<DnsHostEntryDto> mismatchedReaderEntries = dnsService.checkReaderEndpoint().getRight();
		if(mismatchedReaderEntries.isEmpty()){
			return;
		}
		Scanner.of(mismatchedReaderEntries)
				.map(config::fixDatabaseDns)
				.flush(fixes -> sendEmail(mismatchedReaderEntries, fixes));
		mismatchedReaderEntries.forEach(entry -> recordChangelog(entry.getInstanceHostname()));
	}

	private void sendEmail(List<DnsHostEntryDto> mismatchedReaderEntries, List<String> fixes){
		String fromEmail = datarouterProperties.getAdministratorEmail();
		String toEmail = additionalAdministratorEmailService.getAdministratorEmailAddressesCsv();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.auroraInstances)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Aurora DNS")
				.withTitleHref(primaryHref)
				.withContent(makeEmailContent(mismatchedReaderEntries, fixes));
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
	}

	private static ContainerTag makeEmailContent(List<DnsHostEntryDto> mismatchedReaderEntries, List<String> fixes){
		var message = h3("Some of the reader DB instances are pointed to the writer instance.");
		var table = new J2HtmlEmailTable<DnsHostEntryDto>()
				.withColumn("client name", row -> row.getClientName())
				.withColumn("hostname", row -> row.getHostname())
				.withColumn("instance hostname", row -> row.getInstanceHostname())
				.build(mismatchedReaderEntries);
		var fixList = div()
				.with(h3("Executing suggested fixes to reset DNS:"));
		fixes.forEach(fix -> fixList
				.with(rawHtml(fix))
				.with(br()));
		return body(message, table, fixList);
	}

	private void recordChangelog(String database){
		changelogRecorder.record(
				"AuroraDns",
				database,
				"mismatch",
				datarouterProperties.getAdministratorEmail());
	}

}
