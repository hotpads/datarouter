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

import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThresholdKey;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.time.ZonedDateFormatterTool;
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
		var page = new IndexPageBuilder<>(namedScannerPager).build(params.toMap());
		String path = request.getContextPath() + paths.datarouter.nodewatch.tables.toSlashedString();
		var header = Bootstrap4IndexPagerHtml.render(page, path);
		var table = makeTableBuilder().build(page.rows);
		return div(header, table);
	}

	private J2HtmlTable<LatestTableCount> makeTableBuilder(){
		ZoneId userZoneId = getUserZoneId();
		return new J2HtmlTable<LatestTableCount>()
				.withClasses("table table-sm table-striped my-2 border")
				.withColumn(
						"Client",
						row -> row.getKey().getClientName())
				.withHtmlColumn(
						"Table",
						row -> td(a(row.getKey().getTableName())
								.withHref(links.table(
										row.getKey().getClientName(),
										row.getKey().getTableName()))))
				.withColumn(
						"Rows",
						LatestTableCount::getNumRows,
						NumberFormatter::addCommas)
				.withColumn(
						"Count Time",
						LatestTableCount::getCountTimeMs,
						ms -> new DatarouterDuration(ms, TimeUnit.MILLISECONDS).toString())
				.withColumn(
						"Updated",
						LatestTableCount::getDateUpdated,
						instant -> ZonedDateFormatterTool.formatInstantWithZoneDesc(instant, userZoneId))
				.withColumn(
						"Spans",
						LatestTableCount::getNumSpans,
						NumberFormatter::addCommas)
				.withColumn(
						"Slow Spans",
						LatestTableCount::getNumSlowSpans,
						NumberFormatter::addCommas)
				.withHtmlColumn(
						"Alert At",
						this::makeThresholdTableCell);
	}

	private TdTag makeThresholdTableCell(LatestTableCount latestTableCount){
		var thresholdKey = new TableSizeAlertThresholdKey(
				latestTableCount.getKey().getClientName(),
				latestTableCount.getKey().getTableName());
		Optional<TableSizeAlertThreshold> optThreshold = tableSizeAlertThresholdDao.find(thresholdKey);
		String href = links.thresholdEdit(
				latestTableCount.getKey().getClientName(),
				latestTableCount.getKey().getTableName());
		var text = optThreshold
				.map(TableSizeAlertThreshold::getMaxRows)
				.map(NumberFormatter::addCommas)
				.orElse("set");
		var decoratedText = optThreshold.isPresent() ? b(text) : text(text);
		return td(a(decoratedText).withHref(href));
	}

	@Singleton
	private static class NodewatchTableListNamedScannerPager
	extends BaseNamedScannerPager<Void,LatestTableCount>{

		@Inject
		public NodewatchTableListNamedScannerPager(DatarouterLatestTableCountDao dao){
			addWithTotal(
					"Rows",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_ROWS.reversed()
									.thenComparing(LatestTableCount.COMPARE_CLIENT)
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Rows by Client",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_CLIENT
									.thenComparing(LatestTableCount.COMPARE_ROWS.reversed())
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Spans",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_SPANS.reversed()
									.thenComparing(LatestTableCount.COMPARE_ROWS.reversed())
									.thenComparing(LatestTableCount.COMPARE_CLIENT)
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Spans by Client",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_CLIENT
									.thenComparing(LatestTableCount.COMPARE_SPANS.reversed())
									.thenComparing(LatestTableCount.COMPARE_ROWS.reversed())
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Slow Spans",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_SLOW_SPANS.reversed()
									.thenComparing(LatestTableCount.COMPARE_ROWS.reversed())
									.thenComparing(LatestTableCount.COMPARE_CLIENT)
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Slow Spans by Client",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_CLIENT
									.thenComparing(LatestTableCount.COMPARE_SLOW_SPANS.reversed())
									.thenComparing(LatestTableCount.COMPARE_ROWS.reversed())
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Count Time",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_COUNT_TIME.reversed()
									.thenComparing(LatestTableCount.COMPARE_ROWS.reversed())
									.thenComparing(LatestTableCount.COMPARE_CLIENT)
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Count Time by Client",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_CLIENT
									.thenComparing(LatestTableCount.COMPARE_COUNT_TIME.reversed())
									.thenComparing(LatestTableCount.COMPARE_ROWS.reversed())
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Date Updated",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_DATE_UPDATED
									.thenComparing(LatestTableCount.COMPARE_CLIENT)
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Client and Table",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_CLIENT
									.thenComparing(LatestTableCount.COMPARE_TABLE)));
			addWithTotal(
					"Table",
					$ -> dao.scan()
							.sort(LatestTableCount.COMPARE_TABLE
									.thenComparing(LatestTableCount.COMPARE_CLIENT)));
		}

	}

}
