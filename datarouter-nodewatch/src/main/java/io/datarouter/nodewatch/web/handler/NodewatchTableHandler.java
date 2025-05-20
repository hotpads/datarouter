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
package io.datarouter.nodewatch.web.handler;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.link.NodewatchTableLink;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.nodewatch.web.NodewatchTableHistoryGraph;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.indexpager.BaseNamedScannerPager;
import io.datarouter.web.html.indexpager.Bootstrap4IndexPagerHtml;
import io.datarouter.web.html.indexpager.IndexPage.IndexPageBuilder;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class NodewatchTableHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchLinks links;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterLinkClient linkClient;

	@Handler
	private Mav table(NodewatchTableLink link){
		String clientName = link.clientName;
		String tableName = link.tableName;
		List<TableCount> historicCounts = tableCountDao.scanForTable(clientName, tableName)
				.list();
		// TODO compute from already loaded databeans
		TableCount liveCount = tableSamplerService.getCurrentTableCountFromSamples(clientName, tableName);
		var graph = new NodewatchTableHistoryGraph(historicCounts);
		var content = div(
				NodewatchHtml.makeHeader(
						"Table Details",
						"Historic stats for a single table"),
				navService.makeNavTabs(paths.datarouter.nodewatch.table)
						.addTableDetailsTab(clientName, tableName)
						.render(),
				br(),
				makeSubheaderDiv(clientName, tableName),
				br(),
				makeChartDiv(),
				br(),
				br(),
				makeLatestCountDiv(liveCount),
				br(),
				makeHistoricCountsDiv(historicCounts))
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Table Details")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withScript(graph.makeDataScript())
				.withScript(graph.makeGraphScript())
				.withContent(content)
				.buildMav();
	}

	/*--------- subheader -----------*/

	private DivTag makeSubheaderDiv(String clientName, String tableName){
		var floatLeft = NodewatchHtml.makeTableInfoDiv(clientName, tableName)
				.withClass("float-left");
		var floatRight = makeActionsDiv(clientName, tableName)
				.withClass("float-right");
		return div(floatLeft, floatRight)
				.withClass("clearfix");
	}

	private DivTag makeActionsDiv(String clientName, String tableName){
		var estimateSizeButton = NodewatchHtml.makeInfoButton(
				"Storage Details",
				links.tableStorage(clientName, tableName));
		var resampleButton = NodewatchHtml.makeWarningButton(
				"Re-sample",
				links.tableResample(clientName, tableName),
				String.format("Are you sure you want to re-count all rows of %s?", tableName));
		var deleteSamplesButton = NodewatchHtml.makeDangerButton(
				"Delete Samples",
				links.tableDeleteSamples(clientName, tableName),
				String.format("Are you sure you want to delete existing samples for %s?", tableName));
		var deleteAllMetadataButton = NodewatchHtml.makeDangerButton(
				"Delete All Metadata",
				links.tableDeleteAllMetadata(clientName, tableName),
				String.format("Are you sure you want to delete all nodewatch metadata for %s?", tableName));
		return div(estimateSizeButton, resampleButton, deleteSamplesButton, deleteAllMetadataButton);
	}

	/*--------- dygraph -----------*/

	private DivTag makeChartDiv(){
		return div("[graph]")
				.withId(NodewatchTableHistoryGraph.GRAPH_DIV_ID)
				.withStyle("width:100%;height:350px;");
	}

	/*-------- count tables ----------*/

	private DivTag makeLatestCountDiv(TableCount liveCount){
		return div(
				h5("Live Sample Summary"),
				makeTableBuilder().build(List.of(liveCount)));
	}

	private DivTag makeHistoricCountsDiv(List<TableCount> historicCounts){
		var namedScannerPager = new NodewatchTableDetailsNamedScannerPager(historicCounts);
		var page = new IndexPageBuilder<>(namedScannerPager)
				.retainParams(NodewatchTableLink.P_clientName, NodewatchTableLink.P_tableName)
				.build(params.toMap());
		String path = linkClient.toInternalUrl(new NodewatchTableLink("",""));
		var pagerDiv = Bootstrap4IndexPagerHtml.render(page, path);
		return div(
				h5("Historic Sample Summaries"),
				pagerDiv,
				makeTableBuilder().build(page.rows));
	}

	private J2HtmlTable<TableCount> makeTableBuilder(){
		ZoneId userZoneId = getUserZoneId();
		return new J2HtmlTable<TableCount>()
			.withClasses("sortable table table-sm table-striped border")
			.withColumn(
					"Samples",
					TableCount::getNumSpans,
					NumberFormatter::addCommas)
			.withColumn(
					"Rows",
					TableCount::getNumRows,
					NumberFormatter::addCommas)
			.withColumn(
					"Count Time",
					TableCount::getCountTimeMs,
					ms -> new DatarouterDuration(ms, TimeUnit.MILLISECONDS).toString())
			.withColumn(
					"Date",
					row -> row.getKey().getCreatedMs(),
					time -> time.format(userZoneId));
	}

	private static class NodewatchTableDetailsNamedScannerPager
	extends BaseNamedScannerPager<Void,TableCount>{

		public NodewatchTableDetailsNamedScannerPager(List<TableCount> tableCounts){
			addWithTotal(
					"Rows",
					_ -> Scanner.of(tableCounts)
							.sort(TableCount.COMPARE_CREATED_MS.reversed()));
		}

	}

}
