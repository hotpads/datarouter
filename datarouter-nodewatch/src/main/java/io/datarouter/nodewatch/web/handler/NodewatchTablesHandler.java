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

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.datarouter.bytes.ByteLength;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.service.NodewatchTableStatsService;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.PhysicalNodeStats;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.SamplerStats;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.StorageStats;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.TableStats;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThresholdKey;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.indexpager.BaseNamedScannerPager;
import io.datarouter.web.html.indexpager.Bootstrap4IndexPagerHtml;
import io.datarouter.web.html.indexpager.IndexPage.IndexPageBuilder;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class NodewatchTablesHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchLinks links;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private NodewatchTableListNamedScannerPager namedScannerPager;
	@Inject
	private DatarouterTableSizeAlertThresholdDao tableSizeAlertThresholdDao;

	@Handler
	private Mav tables(){
		var content = div(
				NodewatchHtml.makeHeader(
						"Tables",
						"All tables being monitored by Nodewatch with latest size stats"),
				navService.makeNavTabs(paths.datarouter.nodewatch.tables).render(),
				br(),
				makeTableDiv())
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Tables")
				.withContent(content)
				.buildMav();
	}

	private DivTag makeTableDiv(){
		var page = new IndexPageBuilder<>(namedScannerPager)
				.withDefaultPageSize(500)
				.build(params.toMap());
		String path = request.getContextPath() + paths.datarouter.nodewatch.tables.toSlashedString();
		var header = Bootstrap4IndexPagerHtml.render(page, path);
		var table = makeTableBuilder(page.fromRow).build(page.rows);
		return div(header, table);
	}

	private J2HtmlTable<TableStats> makeTableBuilder(long fromRowId){
		var rowId = new AtomicLong(fromRowId);
		return new J2HtmlTable<TableStats>()
				.withClasses("table table-sm table-striped my-2 border")
				.withColumn("#", $ -> rowId.getAndIncrement(), NumberFormatter::addCommas)
				.withColumn(
						"Tag",
						row -> row.optPhysicalNodeStats().map(PhysicalNodeStats::tagString).orElse(""))
				.withColumn(
						"Client",
						TableStats::clientName)
				.withHtmlColumn(
						"Table",
						row -> td(a(row.tableName())
								.withHref(links.table(row.clientName(), row.tableName()))))
				.withHtmlColumn(
						"Rows",
						row -> row.optSamplerStats()
								.map(SamplerStats::numRows)
								.map(NumberFormatter::addCommas)
								.map(anchorText -> td(a(anchorText)
										.withHref(links.table(row.clientName(), row.tableName()))))
								.orElse(td()))
				.withHtmlColumn(
						"Bytes",
						row -> row.optStorageStats()
								.map(StorageStats::numBytes)
								.map(ByteLength::ofBytes)
								.map(ByteLength::toDisplay)
								.map(anchorText -> td(a(anchorText)
										.withHref(links.tableStorage(row.clientName(), row.tableName()))))
								.orElse(td()))
				.withColumn(
						"$ / Year",
						row -> row.optStorageStats()
								.flatMap(StorageStats::optYearlyTotalCostDollars)
								.map(dollars -> "$" + NumberFormatter.format(dollars, 2))
								.orElse(""))
				.withColumn(
						"Spans",
						row -> row.optSamplerStats().map(SamplerStats::numSpans).orElse(null),
						NumberFormatter::addCommas)
				.withColumn(
						"Count Time",
						row -> row.optSamplerStats().map(SamplerStats::countTime).orElse(null),
						duration -> new DatarouterDuration(duration).toString(TimeUnit.SECONDS))
				.withColumn(
						"Updated",
						row -> row.optSamplerStats().map(SamplerStats::updatedAgo).orElse(null),
						duration -> new DatarouterDuration(duration).toString(TimeUnit.MINUTES))
				.withHtmlColumn(
						"Alert",
						row -> makeThresholdTableCell(row.clientAndTableNames()));
	}

	private TdTag makeThresholdTableCell(ClientAndTableNames clientAndTableNames){
		var thresholdKey = new TableSizeAlertThresholdKey(
				clientAndTableNames.client(),
				clientAndTableNames.table());
		Optional<TableSizeAlertThreshold> optThreshold = tableSizeAlertThresholdDao.find(thresholdKey);
		String href = links.thresholdEdit(
				clientAndTableNames.client(),
				clientAndTableNames.table());
		var text = optThreshold
				.map(TableSizeAlertThreshold::getMaxRows)
				.map(NumberFormatter::addCommas)
				.orElse("set");
		var decoratedText = optThreshold.isPresent() ? b(text) : text(text);
		return td(a(decoratedText).withHref(href));
	}

	@Singleton
	private static class NodewatchTableListNamedScannerPager
	extends BaseNamedScannerPager<Void,TableStats>{

		@Inject
		public NodewatchTableListNamedScannerPager(NodewatchTableStatsService statsService){
			addWithTotal(
					"Tag / Rows",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_TAG
									.thenComparing(TableStats.COMPARE_NUM_ROWS.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Tag / Bytes",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_TAG
									.thenComparing(TableStats.COMPARE_NUM_BYTES.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Tag / Cost",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_TAG
									.thenComparing(TableStats.COMPARE_YEARLY_STORAGE_COST.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Tag / Spans",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_TAG
									.thenComparing(TableStats.COMPARE_NUM_SPANS.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Tag / Count Time",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_TAG
									.thenComparing(TableStats.COMPARE_COUNT_TIME.reversed())
									.thenComparing(TableStats.COMPARE_NUM_ROWS.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Tag / Client / Table",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_TAG
									.thenComparing(TableStats.COMPARE_CLIENT)
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Client / Rows",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_CLIENT
									.thenComparing(TableStats.COMPARE_NUM_ROWS.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Client / Spans",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_CLIENT
									.thenComparing(TableStats.COMPARE_NUM_SPANS.reversed())
									.thenComparing(TableStats.COMPARE_NUM_ROWS.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Client / Count Time",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_CLIENT
									.thenComparing(TableStats.COMPARE_COUNT_TIME.reversed())
									.thenComparing(TableStats.COMPARE_NUM_ROWS.reversed())
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Client / Table",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_CLIENT
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Rows",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_NUM_ROWS.reversed()
									.thenComparing(TableStats.COMPARE_CLIENT)
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Spans",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_NUM_SPANS.reversed()
									.thenComparing(TableStats.COMPARE_NUM_ROWS.reversed())
									.thenComparing(TableStats.COMPARE_CLIENT)
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Count Time",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_COUNT_TIME.reversed()
									.thenComparing(TableStats.COMPARE_NUM_ROWS.reversed())
									.thenComparing(TableStats.COMPARE_CLIENT)
									.thenComparing(TableStats.COMPARE_TABLE)));
			addWithTotal(
					"Table",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_TABLE
									.thenComparing(TableStats.COMPARE_CLIENT)));
			addWithTotal(
					"Updated Ago",
					$ -> statsService.scanStats()
							.sort(TableStats.COMPARE_UPDATED_AGO
									.thenComparing(TableStats.COMPARE_CLIENT)
									.thenComparing(TableStats.COMPARE_TABLE)));
		}

	}

}
