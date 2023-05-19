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
package io.datarouter.clustersetting.web.browse;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.span;
import static j2html.TagCreator.strong;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseLinks;
import io.datarouter.clustersetting.web.browse.ClusterSettingHierarchy.HierarchyNode;
import io.datarouter.clustersetting.web.browse.setting.ClusterSettingBrowseSettingHtml;
import io.datarouter.clustersetting.web.log.ClusterSettingLogHandler.ClusterSettingLogLinks;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4BreadcrumbsHtml;
import io.datarouter.web.html.nav.Breadcrumbs;
import j2html.tags.specialized.DivTag;

@Singleton
public class ClusterSettingBrowseSettingNodeHtml{

	@Inject
	private ClusterSettingBrowseSettingHtml settingHtml;
	@Inject
	private ClusterSettingBrowseLinks browseLinks;
	@Inject
	private ClusterSettingLogLinks logLinks;
	@Inject
	private DatarouterClusterSettingLogDao logDao;

	public DivTag makeSettingsDiv(
			HierarchyNode settingNodeHierarchy,
			List<? extends CachedSetting<?>> settings,
			String location,
			Optional<String> optPartialName){
		String nodeName = nodeName(settingNodeHierarchy.name());
		long nodeLogCount = logDao.scanWithWildcardPrefix(nodeName).count();
		var breadcrumbsDiv = makeBreadcrumbsDiv(
				location,
				optPartialName)
				.withClass("mx-2 mt-2");
		var settingsDiv = div();
		settingsDiv.with(breadcrumbsDiv);
		if(isNode(location) && nodeLogCount > 0){
			var logsDiv = makeNodeLogsDiv(nodeName, nodeLogCount)
					.withClass("bg-light ml-2 mb-3 p-2");
			settingsDiv.with(logsDiv);
		}
		Scanner.of(settings)
				.map(setting -> settingHtml.makeSettingDiv(setting, optPartialName))
				.forEach(settingsDiv::with);
		return settingsDiv;
	}

	private DivTag makeBreadcrumbsDiv(
			String location,
			Optional<String> optPartialName){
		var breadcrums = new Breadcrumbs();
		boolean isNode = location.endsWith(".");
		boolean isSetting = !isNode;
		List<String> tokens = Arrays.asList(location.split("\\."));
		for(int i = 0; i < tokens.size(); ++i){
			String subLocation = String.join(".", tokens.subList(0, i + 1));
			if(isNode || i < tokens.size() - 1){
				subLocation += ".";
			}
			String href = browseLinks.all(Optional.of(subLocation), optPartialName);
			boolean active = isSetting && i == tokens.size() - 1;
			breadcrums.add(tokens.get(i), href, active);
		}
		if(isNode){
			// Add a slash at the end indicating there are deeper items.
			// TODO could the generic breadcrumbs support this?
			breadcrums.add("", "", false);
		}
		return Bootstrap4BreadcrumbsHtml.render(breadcrums);
	}

	private DivTag makeNodeLogsDiv(
			String nodeName,
			long nodeLogCount){
		var logLink = a("View")
				.withHref(logLinks.node(nodeName));
		var span = span(
				strong("Node Logs"),
				span(String.format("(%s)", nodeLogCount)).withStyle("color:gray;"),
				span("-").withClass("mx-2"),
				logLink);
		return div(span);
	}

	/*--------- helper ------------*/

	private boolean isNode(String location){
		return location.endsWith(".");
	}

	private String nodeName(String settingName){
		int splitAt = settingName.lastIndexOf('.') + 1;
		return settingName.substring(0, splitAt);
	}

}