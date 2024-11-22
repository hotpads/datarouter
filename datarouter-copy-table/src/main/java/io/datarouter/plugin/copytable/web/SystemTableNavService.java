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

import static j2html.TagCreator.div;

import io.datarouter.pathnode.PathNode;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths.SystemTableCopyPaths;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SystemTableNavService{

	@Inject
	private DatarouterCopyTablePaths paths;
	@Inject
	private SystemTableLinks links;

	public SystemTableNavTabs makeNavTabs(PathNode currentPath){
		return new SystemTableNavTabs(paths, links, currentPath);
	}

	public static class SystemTableNavTabs{

		public final SystemTableLinks links;
		public final NavTabs navTabs = new NavTabs();

		public SystemTableNavTabs(
				DatarouterCopyTablePaths paths,
				SystemTableLinks links,
				PathNode currentPath){
			this.links = links;
			SystemTableCopyPaths root = paths.datarouter.systemTableCopier;
			navTabs.add(new NavTab(
					"List System Tables",
					links.listSystemTables(),
					currentPath.equals(root.listSystemTables)));
			navTabs.add(new NavTab(
					"Migrate System Tables",
					links.migrateSystemTables(),
					currentPath.equals(root.migrateSystemTables)));

		}

		public DivTag render(){
			return div(Bootstrap4NavTabsHtml.render(navTabs))
					.withClass("mt-3");
		}

	}

}
