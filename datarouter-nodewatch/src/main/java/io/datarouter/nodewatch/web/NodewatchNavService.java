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
package io.datarouter.nodewatch.web;

import static j2html.TagCreator.div;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths.NodewatchPaths;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.pathnode.PathNode;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.DivTag;

@Singleton
public class NodewatchNavService{

	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchLinks links;
	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;

	public boolean showSlowSpans(){
		return latestTableCountDao.scan()
				.anyMatch(latestTableCount -> latestTableCount.getNumSlowSpans() > 0);
	}

	public NodewatchNavTabs makeNavTabs(PathNode currentPath){
		return new NodewatchNavTabs(paths, links, currentPath, showSlowSpans());
	}

	public static class NodewatchNavTabs{

		public final NodewatchLinks links;
		public final NavTabs navTabs = new NavTabs();

		public NodewatchNavTabs(
				DatarouterNodewatchPaths paths,
				NodewatchLinks links,
				PathNode currentPath,
				boolean showSlowSpans){
			this.links = links;
			NodewatchPaths root = paths.datarouter.nodewatch;
			navTabs.add(new NavTab(
					"Tables",
					links.tables(),
					currentPath.equals(root.tables)));
			navTabs.add(new NavTab(
					"Summary",
					links.summary(),
					currentPath.equals(root.summary)));
			navTabs.add(new NavTab(
					"Table Configs",
					links.configs(),
					currentPath.equals(root.configs)));
			if(showSlowSpans){
				navTabs.add(new NavTab(
						"Slow Spans",
						links.slowSpans(),
						currentPath.equals(root.slowSpans)));
			}
			navTabs.add(new NavTab(
					"Migrate Metadata",
					links.metadataMigrate(),
					currentPath.equals(root.metadata.migrate)));
		}

		public NodewatchNavTabs addTableDetailsTab(String clientName, String tableName){
			navTabs.add(new NavTab(
					"Table Details",
					links.table(clientName, tableName),
					true));
			return this;
		}

		public NodewatchNavTabs addThresholdEditTab(String clientName, String tableName){
			navTabs.add(new NavTab(
					"Edit Alert",
					links.thresholdEdit(clientName, tableName),
					true));
			return this;
		}

		public DivTag render(){
			return div(Bootstrap4NavTabsHtml.render(navTabs))
					.withClass("mt-3");
		}
	}
}
