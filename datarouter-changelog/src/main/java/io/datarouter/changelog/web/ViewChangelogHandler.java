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

import static j2html.TagCreator.div;

import java.util.Collections;
import java.util.Date;

import javax.inject.Inject;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.pager.Bootstrap4PagerHtml;
import io.datarouter.web.html.pager.MemoryPager;
import io.datarouter.web.html.pager.MemoryPager.Page;
import io.datarouter.web.html.pager.MemorySorter;
import j2html.tags.ContainerTag;

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
		var table = new J2HtmlTable<Changelog>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("date", row -> {
					Long reversedDateMs = row.getKey().getReversedDateMs();
					return new Date(Long.MAX_VALUE - reversedDateMs);
				})
				.withColumn("type", row -> row.getKey().getChangelogType())
				.withColumn("name", row -> row.getKey().getName())
				.withColumn("action", row -> row.getAction())
				.withColumn("username", row -> row.getUsername())
				.build(page.rows);
		return div(form, linkBar, table)
				.withClass("container-fluid");
	}

}
