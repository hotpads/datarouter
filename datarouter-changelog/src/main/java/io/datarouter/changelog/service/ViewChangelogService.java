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

import static j2html.TagCreator.a;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.td;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.changelog.web.ViewExactChangelogHandler;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.attributes.Attr;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class ViewChangelogService{

	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterChangelogPaths paths;

	public ContainerTag<?> buildTable(List<Changelog> rows, ZoneId zoneId){
		return new J2HtmlTable<Changelog>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn("", row -> td(a().withClass("fa fa-link").withHref(buildViewExactHref(row))))
				.withColumn("Date", row -> {
					Long reversedDateMs = row.getKey().getReversedDateMs();
					Date date = new Date(Long.MAX_VALUE - reversedDateMs);
					return ZonedDateFormatterTool.formatDateWithZone(date, zoneId);
				})
				.withColumn("Type", row -> row.getKey().getChangelogType())
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Action", row -> row.getAction())
				.withColumn("User", row -> row.getUsername())
				.withHtmlColumn("Comment", row -> {
					String id = row.getKey().getReversedDateMs() + "" + RandomTool.nextPositiveInt();
					return makeModal(id, row.getComment(), "comment");
				})
				.withHtmlColumn("Note", row -> {
					String id = row.getKey().getReversedDateMs() + "" + RandomTool.nextPositiveInt();
					return makeModal(id, row.getNote(), "note");
				})
				.withHtmlColumn("Edit", row -> td(a().withClass("fa fa-edit").withHref(buildEditHref(row))))
				.build(rows);
	}

	public DomContent makeModal(String id, String comment, String type){
		if(comment == null){
			return td();
		}
		String modalId = type + "Modal" + id;
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
				.attr(Attr.ROLE, "document");
		var modal = div(modalDialog)
				.withClass("modal fade")
				.withId(modalId)
				.attr("tabindex", "-1")
				.attr(Attr.ROLE, "dialog");
		return td(div(commentButton, modal))
				.withStyle("text-align:center");
	}

	public String buildViewExactHref(Changelog log){
		ChangelogKey key = log.getKey();
		return new URIBuilder()
				.setPath(servletContext.get().getContextPath() + paths.datarouter.changelog.viewExact.toSlashedString())
				.addParameter(ViewExactChangelogHandler.P_reversedDateMs, key.getReversedDateMs().toString())
				.addParameter(ViewExactChangelogHandler.P_changelogType, key.getChangelogType())
				.addParameter(ViewExactChangelogHandler.P_name, key.getName())
				.toString();
	}

	public String buildEditHref(Changelog log){
		ChangelogKey key = log.getKey();
		return new URIBuilder()
				.setPath(servletContext.get().getContextPath() + paths.datarouter.changelog.edit.toSlashedString())
				.addParameter(ViewExactChangelogHandler.P_reversedDateMs, key.getReversedDateMs().toString())
				.addParameter(ViewExactChangelogHandler.P_changelogType, key.getChangelogType())
				.addParameter(ViewExactChangelogHandler.P_name, key.getName())
				.toString();
	}

}
