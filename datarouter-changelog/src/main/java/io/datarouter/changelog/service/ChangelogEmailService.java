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
package io.datarouter.changelog.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.span;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.httpclient.client.service.ServiceName;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDto;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.tuple.Twin;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class ChangelogEmailService{

	@Inject
	private ServiceName serviceName;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterChangelogPaths paths;
	@Inject
	private ServerName serverName;

	public void sendEmail(DatarouterChangelogDto dto){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.changelog.viewAll)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Changelog - " + dto.changelogType)
				.withTitleHref(primaryHref)
				.withContent(makeEmailContent(
						dto.changelogType,
						dto.name,
						dto.action,
						dto.username,
						dto.comment.orElse("")))
				.fromAdmin()
				.toAdmin(dto.includeMainDatarouterAdmin)
				.toSubscribers(dto.includeSubscribers)
				.to(dto.username);
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private ContainerTag<?> makeEmailContent(String changelogType, String name, String action, String username,
			String comment){
		var rows = List.of(
				new Twin<>("Service", serviceName.get()),
				new Twin<>("ServerName", serverName.get()),
				new Twin<>("ChangelogType", changelogType),
				new Twin<>("Name", name),
				new Twin<>("Action", action),
				new Twin<>("Username", username),
				new Twin<>("Comment", comment));
		return new J2HtmlEmailTable<Twin<String>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeSpanWhiteSpacePre(row.getRight())))
				.build(rows);
	}

	private static DomContent makeDivBoldRight(String text){
		return div(text).withStyle("font-weight:bold;text-align:right;");
	}

	private static DomContent makeSpanWhiteSpacePre(String text){
		return span(text).withStyle("white-space:pre");
	}

}
