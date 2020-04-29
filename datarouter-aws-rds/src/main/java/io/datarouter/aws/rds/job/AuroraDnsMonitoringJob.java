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
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
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

	@Override
	public void run(TaskTracker tracker){
		List<DnsHostEntryDto> mismatchedSlaveEntries = dnsService.checkSlaveEndpoint().getRight();
		if(mismatchedSlaveEntries.isEmpty()){
			return;
		}
		Scanner.of(mismatchedSlaveEntries)
				.map(config::fixDatabaseDns)
				.flush(fixes -> sendEmail(mismatchedSlaveEntries, fixes));
	}

	private void sendEmail(List<DnsHostEntryDto> mismatchedSlaveEntries, List<String> fixes){
		String fromEmail = datarouterProperties.getAdministratorEmail();
		String toEmail = additionalAdministratorEmailService.getAdministratorEmailAddressesCsv();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.auroraInstances)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Aurora DNS")
				.withTitleHref(primaryHref)
				.withContent(makeEmailContent(mismatchedSlaveEntries, fixes));
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
	}

	private static ContainerTag makeEmailContent(List<DnsHostEntryDto> mismatchedSlaveEntries, List<String> fixes){
		var message = h3("Some of the slave DB instances are pointed to the writer instance.");
		var table = new J2HtmlEmailTable<DnsHostEntryDto>()
				.withColumn("client name", row -> row.getClientName())
				.withColumn("hostname", row -> row.getHostname())
				.withColumn("instance hostname", row -> row.getInstanceHostname())
				.build(mismatchedSlaveEntries);
		var fixList = div()
				.with(h3("Executing suggested fixes to reset DNS:"));
		fixes.forEach(fix -> fixList
				.with(rawHtml(fix))
				.with(br()));
		return body(message, table, fixList);
	}

}
