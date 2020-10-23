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

import static j2html.TagCreator.a;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.td;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.web.ViewExactChangelogHandler;
import io.datarouter.util.number.RandomTool;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class ViewChangelogService{

	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterChangelogPaths paths;

	public ContainerTag buildTable(List<Changelog> rows){
		return new J2HtmlTable<Changelog>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn("", row -> {
					String href = servletContext.get().getContextPath()
							+ paths.datarouter.changelog.viewExact.toSlashedString()
							+ "?" + ViewExactChangelogHandler.P_reversedDateMs + "=" + row.getKey().getReversedDateMs()
							+ "&" + ViewExactChangelogHandler.P_changelogType + "=" + row.getKey().getChangelogType()
							+ "&" + ViewExactChangelogHandler.P_name + "=" + row.getKey().getName();
					var linkButton = a()
							.withClass("fa fa-link")
							.withHref(href);
					return td(linkButton);
				})
				.withColumn("Date", row -> {
					Long reversedDateMs = row.getKey().getReversedDateMs();
					return new Date(Long.MAX_VALUE - reversedDateMs);
				})
				.withColumn("Type", row -> row.getKey().getChangelogType())
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Action", row -> row.getAction())
				.withColumn("User", row -> row.getUsername())
				.withHtmlColumn("Comment", row -> {
					String id = row.getKey().getReversedDateMs() + "" + RandomTool.nextPositiveInt();
					return makeCommentModal(id, row.getComment());
				})
				.build(rows);
	}

	public DomContent makeCommentModal(String id, String comment){
		if(comment == null){
			return td();
		}
		String modalId = "commentModal" + id;
		var commentButton = a()
				.withClass("fa fa-sticky-note")
				.attr("data-toggle", "modal")
				.attr("data-target", "#" + modalId)
				.withHref("#" + modalId);

		var modalBody = div(comment)
				.withClass("modal-body")
				.withStyle("text-align:left");
		var modalFooter = div(button("Close")
				.withType("button")
				.withClass("btn btn-secondary")
				.attr("data-dismiss", "modal"))
				.withClass("modal-footer");
		var modalContent = div(modalBody, modalFooter)
				.withClass("modal-content");
		var modalDialog = div(modalContent)
				.withClass("modal-dialog")
				.withRole("document");
		var modal = div(modalDialog)
				.withClass("modal fade")
				.withId(modalId)
				.attr("tabindex", "-1")
				.withRole("dialog");
		return td(div(commentButton, modal))
				.withStyle("text-align:center");
	}

}
