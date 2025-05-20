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
package io.datarouter.clustersetting.web.log;

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.link.ClusterSettingAllLogLink;
import io.datarouter.clustersetting.link.ClusterSettingBrowseLink;
import io.datarouter.clustersetting.link.ClusterSettingNodeLogLink;
import io.datarouter.clustersetting.link.ClusterSettingSettingLogLink;
import io.datarouter.clustersetting.link.ClusterSettingSingleLogLink;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLogKey;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.indexpager.BaseNamedScannerPager;
import io.datarouter.web.html.indexpager.Bootstrap4IndexPagerHtml;
import io.datarouter.web.html.indexpager.IndexPage.IndexPageBuilder;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class ClusterSettingLogHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClusterSettingPaths paths;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;
	@Inject
	private ClusterSettingLogHtml clusterSettingLogHtml;
	@Inject
	private ClusterSettingLogNamedScannerPager namedScannerPager;
	@Inject
	private DatarouterClusterSettingLogDao dao;
	@Inject
	private DatarouterLinkClient linkClient;

	/*------------ handlers ------------*/

	@Handler
	public Mav all(ClusterSettingAllLogLink logLink){
		Optional<String> beforeDate = logLink.beforeDate;
		String title = clusterSettingHtml.makeTitle("Logs For All Settings");

		// form
		var dateForm = new HtmlForm(HtmlFormMethod.GET);
		dateForm.addDateField()
				.withLabel("Before")
				.withName(ClusterSettingAllLogLink.P_beforeDate)
				.withValue(beforeDate.orElse(null));
		dateForm.addButtonWithoutSubmitAction()
				.withLabel("Search");

		// result data
		Instant beforeTime = beforeDate
				.map(Bootstrap4FormHtml::parseDateFieldToEpochMillis)
				.map(Instant::ofEpochMilli)
				.orElseGet(Instant::now);
		var page = new IndexPageBuilder<>(namedScannerPager)
				.retainParams(ClusterSettingAllLogLink.P_beforeDate)
				.build(beforeTime, params.toMap());

		// result html
		String path = linkClient.toInternalUrl(new ClusterSettingAllLogLink());
		var headerDiv = clusterSettingHtml.makeHeader(title, "Changes to all settings in this cluster");
		var formTag = Bootstrap4FormHtml.render(dateForm, true);
		var pagerDiv = Bootstrap4IndexPagerHtml.render(page, path);
		var tableDiv = makeTableDiv(page.rows);
		var content = div(
				headerDiv,
				br(),
				formTag,
				br(),
				pagerDiv,
				tableDiv)
				.withClass("container-fluid");
		return pageFactory.simplePage(request, title, content);
	}

	@Handler
	public Mav node(ClusterSettingNodeLogLink nodeLink){
		String nodeName = nodeLink.nodeName;
		String title = clusterSettingHtml.makeTitle("Logs For Setting Node");
		List<ClusterSettingLog> logs = dao.scanWithWildcardPrefix(nodeName)
				.sort(Comparator.comparing((ClusterSettingLog log) -> log.getKey().getMilliTimeReversed()))
				.list();
		var headerDiv = clusterSettingHtml.makeHeader(title, "Changes to settings in the same parent node");
		String href = linkClient.toInternalUrl(new ClusterSettingBrowseLink()
				.withLocation(nodeName));
		var nodeNameDiv = div(
				h5("Node name"),
				div(a(nodeName).withHref(href)));
		var tableDiv = makeTableDiv(logs);
		var content = div(
				headerDiv,
				br(),
				nodeNameDiv,
				br(),
				tableDiv)
				.withClass("container-fluid");
		return pageFactory.simplePage(request, title, content);
	}

	@Handler
	public Mav setting(ClusterSettingSettingLogLink settingLink){
		String settingName = settingLink.settingName;
		String title = clusterSettingHtml.makeTitle("Logs For Single Setting");
		ClusterSettingLogKey prefix = ClusterSettingLogKey.prefix(settingName);
		List<ClusterSettingLog> logs = dao.scanWithPrefix(prefix).list();
		var headerDiv = clusterSettingHtml.makeHeader(title, "Changes to a single setting");
		String href = linkClient.toInternalUrl(new ClusterSettingBrowseLink()
				.withLocation(settingName));
		var settingNameDiv = div(
				h5("Setting name"),
				div(a(settingName).withHref(href)));
		var tableDiv = makeTableDiv(logs);
		var content = div(
				headerDiv,
				br(),
				settingNameDiv,
				br(),
				tableDiv)
				.withClass("container-fluid");
		return pageFactory.simplePage(request, title, content);
	}

	@Handler
	public Mav single(ClusterSettingSingleLogLink singleLink){
		String settingName = singleLink.settingName;
		MilliTimeReversed reverseCreatedMs = singleLink.reverseCreatedMs;
		String title = clusterSettingHtml.makeTitle("Log Entry Details");
		var key = new ClusterSettingLogKey(settingName, reverseCreatedMs);
		ClusterSettingLog log = dao.find(key).orElseThrow();
		var content = div(
				clusterSettingHtml.makeHeader(title, "Single setting log entry"),
				br(),
				clusterSettingLogHtml.makeCard(getUserZoneId(), log))
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

	/*------------ pager ------------*/

	private static class ClusterSettingLogNamedScannerPager
	extends BaseNamedScannerPager<Instant,ClusterSettingLog>{

		@Inject
		public ClusterSettingLogNamedScannerPager(DatarouterClusterSettingLogDao clusterSettingLogDao){
			addWithTotal("Most Recent", clusterSettingLogDao::scanBeforeDesc);
		}

	}

	/*------------ table ------------*/

	private DivTag makeTableDiv(List<ClusterSettingLog> pageOfLogs){
		boolean showServerName = Scanner.of(pageOfLogs)
				.map(ClusterSettingLog::getServerName)
				.anyMatch(StringTool::notEmpty);
		var table = clusterSettingLogHtml.makeTableBuilder(getUserZoneId(), showServerName)
				.build(pageOfLogs);
		return div(table);
	}

}
