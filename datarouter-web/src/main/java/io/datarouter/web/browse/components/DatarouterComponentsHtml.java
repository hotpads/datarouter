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
package io.datarouter.web.browse.components;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.DatarouterWebPaths.InfoPaths;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.UlTag;

public class DatarouterComponentsHtml{

	private static InfoPaths PATHS = new DatarouterWebPaths().datarouter.info;

	public static DivTag makeHeader(
			PathNode currentPath,
			String titleSuffix,
			String subtitle){
		return div(
				h3("Datarouter Components - " + titleSuffix),
				div(subtitle),
				br(),
				makeNavTabs(currentPath))
				.withClass("mt-3");
	}

	private static UlTag makeNavTabs(PathNode currentPath){
		var navTabs = new NavTabs()
				.add(new NavTab(
						"Clients",
						PATHS.clients.getValue(),
						currentPath.equals(PATHS.clients)))
				.add(new NavTab(
						"Filters",
						PATHS.filters.getValue(),
						currentPath.equals(PATHS.filters)))
				.add(new NavTab(
						"Listeners",
						PATHS.listeners.getValue(),
						currentPath.equals(PATHS.listeners)))
				.add(new NavTab(
						"Nodes",
						PATHS.nodes.getValue(),
						currentPath.equals(PATHS.nodes)))
				.add(new NavTab(
						"Plugins",
						PATHS.plugins.getValue(),
						currentPath.equals(PATHS.plugins)))
				.add(new NavTab(
						"Properties",
						PATHS.properties.getValue(),
						currentPath.equals(PATHS.properties)))
				.add(new NavTab(
						"RouteSets",
						PATHS.routeSets.getValue(),
						currentPath.equals(PATHS.routeSets)));
		return Bootstrap4NavTabsHtml.render(navTabs);
	}

}
