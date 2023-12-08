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
package io.datarouter.webappinstance.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths.WebappInstancePaths;
import j2html.tags.specialized.DivTag;

public class WebappInstanceHtml{

	private static final WebappInstancePaths PATHS = new DatarouterWebappInstancePaths().datarouter.webappInstances;

	public static DivTag makeHeader(PathNode currentPath){
		return div(
				makeTitle(),
				makeNavTabs(currentPath),
				br());
	}

	public static DivTag makeTitle(){
		return div(
				h3("Running Servers"),
				div("Currently running and historical Servers or K8s Pods"))
				.withClass("mt-3");
	}

	public static DivTag makeNavTabs(PathNode currentPath){
		var navTabs = new NavTabs()
				.add(new NavTab(
						"Running Servers",
						PATHS.running.getValue(),
						currentPath.equals(PATHS.running)))
				.add(new NavTab(
						"Server History",
						PATHS.history.getValue(),
						currentPath.equals(PATHS.history)));
		return div(Bootstrap4NavTabsHtml.render(navTabs))
				.withClass("mt-3");
	}

}
