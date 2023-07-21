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

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDto;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
						dto.comment.orElse(""),
						dto.note.orElse("")))
				.fromAdmin()
				.toAdmin(dto.includeMainDatarouterAdmin)
				.toSubscribers(dto.includeSubscribers)
				.to(dto.username);
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private TableTag makeEmailContent(
			String changelogType,
			String name,
			String action,
			String username,
			String comment,
			String note){
		var rows = List.of(
				new Row("Service", serviceName.get()),
				new Row("ServerName", serverName.get()),
				new Row("ChangelogType", changelogType),
				new Row("Name", name),
				new Row("Action", action),
				new Row("Username", username),
				new Row("Comment", comment),
				new Row("Note", note));
		return new J2HtmlEmailTable<Row>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.header())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeSpanWhiteSpacePre(row.content())))
				.build(rows);
	}

	private record Row(
			String header,
			String content){
	}

	private static DomContent makeDivBoldRight(String text){
		return div(text).withStyle("font-weight:bold;text-align:right;");
	}

	private static DomContent makeSpanWhiteSpacePre(String text){
		return span(text).withStyle("white-space:pre");
	}

}
