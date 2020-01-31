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
package io.datarouter.job.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.text;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.autoconfig.ConfigScanDto;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class ConfigurationScanReportService{

	@Inject
	private DatarouterAdministratorEmailService adminEmailService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterWebPaths paths;

	public void scanConfigurationAndSendEmail(String subject, List<ConfigScanDto> scans){
		List<String> configResponses = scans.stream()
				.filter(item -> item.shouldSendEmail)
				.map(item -> item.response)
				.collect(Collectors.toList());
		if(configResponses.isEmpty()){
			return;
		}
		var headerRows = List.of(
				Twin.of("environment", datarouterProperties.getEnvironment()),
				Twin.of("host", datarouterProperties.getServerName()),
				Twin.of("webapp", datarouterProperties.getWebappName()));
		var headerTable = new J2HtmlEmailTable<Twin<String>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> text(row.getRight())))
				.build(headerRows);
		var content = div(
				h3(subject),
				headerTable,
				each(configResponses, TagCreator::rawHtml));
		sendEmail(content, subject);
	}

	private void sendEmail(ContainerTag content, String subject){
		String fromEmail = datarouterProperties.getAdministratorEmail();
		String[] splitFromEmail = datarouterProperties.getAdministratorEmail().split("@");
		if(splitFromEmail.length == 2){
			fromEmail = splitFromEmail[0] + "+configurationreport@" + splitFromEmail[1];
		}
		String toEmail = adminEmailService.getAdministratorEmailAddressesCsv();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter)//TODO link to a new page that mirrors the email?
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle(subject)
				.withTitleHref(primaryHref)
				.withContent(content);
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
	}

	private static DomContent makeDivBoldRight(String text){
		return div(text).withStyle("font-weight:bold;text-align:right;");
	}

}
