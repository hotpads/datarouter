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
package io.datarouter.changelog.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.td;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.util.number.RandomTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.pager.Bootstrap4PagerHtml;
import io.datarouter.web.html.pager.MemoryPager;
import io.datarouter.web.html.pager.MemoryPager.Page;
import io.datarouter.web.html.pager.MemorySorter;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class ViewChangelogHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ChangelogDao dao;
	@Inject
	private DatarouterChangelogPaths paths;

	@Handler(defaultHandler = true)
	public Mav view(){
		MemoryPager<Changelog> pager = new MemoryPager<>(
				Collections.emptyList(),
				new MemorySorter<>(),
				request.getContextPath() + paths.datarouter.changelog.view.toSlashedString(),
				params,
				100);
		Page<Changelog> page = pager.collect(dao.scan());
		return pageFactory.startBuilder(request)
				.withTitle("Changelog")
				.withContent(makeContent(page))
				.buildMav();
	}

	private static ContainerTag makeContent(Page<Changelog> page){
		var form = Bootstrap4PagerHtml.renderForm(page)
				.withClass("mt-4");
		var linkBar = Bootstrap4PagerHtml.renderLinkBar(page)
				.withClass("mt-2");
		var table = buildTable(page.rows);
		return div(form, linkBar, table)
				.withClass("container-fluid");
	}

	public static ContainerTag buildTable(List<Changelog> rows){
		return new J2HtmlTable<Changelog>()
				.withClasses("table table-sm table-striped my-4 border")
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

	public static DomContent makeCommentModal(String id, String comment){
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
