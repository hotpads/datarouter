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
package io.datarouter.plugin.copytable.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;

import io.datarouter.pathnode.PathNode;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths.CopyTablePaths;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.UlTag;

public class CopyTableHtml{

	public static DivTag makeContent(PathNode currentPath, HtmlForm htmlForm){
		var form = Bootstrap4FormHtml.render(htmlForm)
				.withClass("card card-body bg-light");
		return div(
				h3("Copy Table"),
				div("Copy databeans from one table to another table with the same Databean type"),
				br(),
				makeNav(currentPath),
				form,
				br())
				.withClass("container mt-3");
	}

	private static UlTag makeNav(PathNode currentPath){
		CopyTablePaths parentPath = new DatarouterCopyTablePaths().datarouter.copyTable;
		var navTabs = new NavTabs()
				.add(new NavTab(
						"Joblets",
						parentPath.joblets.getValue(),
						currentPath.equals(parentPath.joblets)))
				.add(new NavTab(
						"Single Thread",
						parentPath.singleThread.getValue(),
						currentPath.equals(parentPath.singleThread)));
		return Bootstrap4NavTabsHtml.render(navTabs);
	}

}
