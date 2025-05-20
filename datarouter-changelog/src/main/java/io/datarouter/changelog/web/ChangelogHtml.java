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
import static j2html.TagCreator.h3;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.config.DatarouterChangelogPaths.ChangelogPaths;
import io.datarouter.pathnode.PathNode;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.DivTag;

public class ChangelogHtml{

	private static final ChangelogPaths PATHS = new DatarouterChangelogPaths().datarouter.changelog;

	public static DivTag makeHeader(PathNode currentPath){
		return div(
				makeTitle(),
				makeNavTabs(currentPath));
	}

	public static DivTag makeTitle(){
		return div(
				h3("Changelog"),
				div("A record of administrative events for monitoring, debugging, and compliance"))
				.withClass("mt-3");
	}

	public static DivTag makeNavTabs(PathNode currentPath){
		var navTabs = new NavTabs()
				.add(new NavTab(
						"View All",
						PATHS.viewAll.getValue(),
						currentPath.equals(PATHS.viewAll)))
				.add(new NavTab(
						"View Dates",
						PATHS.viewForDateRange.getValue(),
						currentPath.equals(PATHS.viewForDateRange)));
		if(currentPath.equals(PATHS.viewExact)){
			navTabs.add(new NavTab(
					"View",
					PATHS.viewExact.getValue(),
					true));
		}
		if(currentPath.equals(PATHS.edit)){
			navTabs.add(new NavTab(
					"Edit",
					PATHS.edit.getValue(),
					true));
		}
		return div(Bootstrap4NavTabsHtml.render(navTabs))
				.withClass("mt-3");
	}

}
