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

import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.pathnode.PathNode;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths.SystemTableCopyPaths;
import io.datarouter.plugin.copytable.link.MigrateSystemTablesLink;
import io.datarouter.plugin.copytable.link.MigrateSystemTablesMetadataLink;
import io.datarouter.plugin.copytable.link.SystemTablesListLink;
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
	private DatarouterLinkClient linkClient;

	public SystemTableNavTabs makeNavTabs(PathNode currentPath){
		return new SystemTableNavTabs(paths, linkClient, currentPath);
	}

	public static class SystemTableNavTabs{

		public final DatarouterLinkClient linkClient;
		public final NavTabs navTabs = new NavTabs();

		public SystemTableNavTabs(
				DatarouterCopyTablePaths paths,
				DatarouterLinkClient linkClient,
				PathNode currentPath){
			this.linkClient = linkClient;
			SystemTableCopyPaths root = paths.datarouter.systemTableCopier;
			navTabs.add(new NavTab(
					"List System Tables",
					linkClient.toInternalUrl(new SystemTablesListLink()),
					currentPath.equals(root.listSystemTables)));
			navTabs.add(new NavTab(
					"Migrate System Tables",
					linkClient.toInternalUrl(new MigrateSystemTablesLink()),
					currentPath.equals(root.migrateSystemTables)));
			navTabs.add(new NavTab(
					"Migrate System Tables Metadata",
					linkClient.toInternalUrl(new MigrateSystemTablesMetadataLink()),
					currentPath.equals(root.migrateSystemTablesMetadata)));

		}

		public DivTag render(){
			return div(Bootstrap4NavTabsHtml.render(navTabs))
					.withClass("mt-3");
		}

	}

}
