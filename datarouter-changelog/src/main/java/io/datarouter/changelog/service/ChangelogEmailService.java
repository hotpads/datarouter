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
package io.datarouter.changelog.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class ChangelogEmailService{

	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterAdministratorEmailService additionalAdministratorEmailService;
	@Inject
	private DatarouterChangelogPaths paths;

	public void sendEmail(String changelogType, String name, String action, String username,
			Optional<String> toEmailParam, Optional<String> comment){
		String from = datarouterProperties.getAdministratorEmail();
		List<String> toEmails = additionalAdministratorEmailService.getAdministratorEmailAddresses();
		toEmails.add(username);
		toEmailParam
				.map(email -> email.split(","))
				.map(Arrays::stream)
				.ifPresent(stream -> stream.forEach(toEmails::add));
		String to = String.join(",", toEmails);

		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.changelog.view)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject("Changelog - " + changelogType + " - " + datarouterService.getName())
				.withTitle("Changelog - " + changelogType)
				.withTitleHref(primaryHref)
				.withContent(makeEmailContent(changelogType, name, action, username, comment.orElse("")));
		htmlEmailService.trySendJ2Html(from, to, emailBuilder);
	}

	private ContainerTag makeEmailContent(String changelogType, String name, String action, String username,
			String comment){
		var rows = List.of(
				new Twin<>("Service", datarouterService.getName()),
				new Twin<>("ServerName", datarouterProperties.getServerName()),
				new Twin<>("ChangelogType", changelogType),
				new Twin<>("Name", name),
				new Twin<>("Action", action),
				new Twin<>("Username", username),
				new Twin<>("Comment", comment));
		return new J2HtmlEmailTable<Twin<String>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> text(row.getRight())))
				.build(rows);
	}

	private static DomContent makeDivBoldRight(String text){
		return div(text).withStyle("font-weight:bold;text-align:right;");
	}

}
