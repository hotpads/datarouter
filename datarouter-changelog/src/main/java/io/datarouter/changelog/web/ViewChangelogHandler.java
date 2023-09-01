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
package io.datarouter.changelog.web;

import static j2html.TagCreator.div;

import java.util.Collections;
import java.util.HashMap;

import io.datarouter.auth.service.CurrentUserSessionInfoService;
import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.service.ViewChangelogService;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.pager.Bootstrap4PagerHtml;
import io.datarouter.web.html.pager.MemoryPager;
import io.datarouter.web.html.pager.MemoryPager.Page;
import io.datarouter.web.html.pager.MemorySorter;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class ViewChangelogHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ChangelogDao dao;
	@Inject
	private DatarouterChangelogPaths paths;
	@Inject
	private ViewChangelogService service;
	@Inject
	private CurrentUserSessionInfoService currentSessionInfoService;

	@Handler(defaultHandler = true)
	public Mav viewAll(){
		MemoryPager<Changelog> pager = new MemoryPager<>(
				Collections.emptyList(),
				new MemorySorter<>(),
				request.getContextPath() + paths.datarouter.changelog.viewAll.toSlashedString(),
				new HashMap<>(),
				params,
				100);
		Page<Changelog> page = pager.collect(dao.scan());
		return pageFactory.startBuilder(request)
				.withTitle("Changelog")
				.withContent(makeContent(page))
				.buildMav();
	}

	private DivTag makeContent(Page<Changelog> page){
		var header = ChangelogHtml.makeHeader(paths.datarouter.changelog.viewAll);
		var form = Bootstrap4PagerHtml.renderForm(page)
				.withClass("mt-4");
		var linkBar = Bootstrap4PagerHtml.renderLinkBar(page)
				.withClass("mt-2");
		var table = service.buildTable(page.rows, currentSessionInfoService.getZoneId(request));
		return div(header, form, linkBar, table)
				.withClass("container-fluid");
	}

}
