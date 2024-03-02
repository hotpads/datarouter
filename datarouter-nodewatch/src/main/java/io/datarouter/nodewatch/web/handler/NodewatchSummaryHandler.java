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

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class NodewatchSummaryHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;

	@Handler
	private Mav summary(){
		List<LatestTableCount> latestTableCounts = latestTableCountDao.scan()
				.list();
		var content = div(
				NodewatchHtml.makeHeader(
						"Summary",
						"Total rows in all databases, also split by client"),
				navService.makeNavTabs(paths.datarouter.nodewatch.summary).render(),
				br(),
				makeSummaryDiv(latestTableCounts),
				br(),
				makeClientsDiv(latestTableCounts))
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Summary")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DivTag makeSummaryDiv(List<LatestTableCount> latestTableCounts){
		var summary = new Summary(latestTableCounts);
		List<NameValue> rows = List.of(
				new NameValue("rows", summary.rowsDisplay()),
				new NameValue("countTime", summary.countTimeDisplay()),
				new NameValue("spans", summary.spansDisplay()),
				new NameValue("slowSpans", summary.slowSpansDisplay()));

		var table = makeSummaryTableBuilder().build(rows);
		var wrapper = div(table)
				.withStyle("width:400px;");
		return div(
				h5("Summary"),
				wrapper);
	}

	private DivTag makeClientsDiv(List<LatestTableCount> latestTableCounts){
		List<ClientSummary> rows = Scanner.of(latestTableCounts)
				.splitBy(count -> count.getKey().getClientName())
				.map(Scanner::list)
				.map(ClientSummary::new)
				.sort(ClientSummary.COMPARE_ROWS.reversed())
				.list();
		var table = makeClientSummaryTableBuilder().build(rows);
		return div(
				h5("Clients"),
				table);
	}

	/*-------- table builders ---------*/

	private J2HtmlTable<NameValue> makeSummaryTableBuilder(){
		return new J2HtmlTable<NameValue>()
				.withClasses("table table-sm table-striped border")
				.withColumn("Name", NameValue::name)
				.withColumn("Value", NameValue::value);
	}

	private J2HtmlTable<ClientSummary> makeClientSummaryTableBuilder(){
		return new J2HtmlTable<ClientSummary>()
				.withClasses("sortable table table-sm table-striped border")
				.withColumn(
						"Client",
						ClientSummary::clientName)
				.withColumn(
						"Rows",
						row -> row.summary().rowsDisplay())
				.withColumn(
						"Count Time",
						row -> row.summary().countTimeDisplay())
				.withColumn(
						"Spans",
						row -> row.summary().spansDisplay())
				.withColumn(
						"Slow Spans",
						row -> row.summary().slowSpansDisplay());
	}

	/*----------- records --------------*/

	private record NameValue(
			String name,
			String value){
	}

	private record Summary(
			long rows,
			long countTimeMs,
			long spans,
			long slowSpans){

		Summary(List<LatestTableCount> counts){
			this(
					counts.stream().mapToLong(LatestTableCount::getNumRows).sum(),
					counts.stream().mapToLong(LatestTableCount::getCountTimeMs).sum(),
					counts.stream().mapToLong(LatestTableCount::getNumSpans).sum(),
					counts.stream().mapToLong(LatestTableCount::getNumSlowSpans).sum());
		}

		public String rowsDisplay(){
			return NumberFormatter.addCommas(rows);
		}

		public String countTimeDisplay(){
			return new DatarouterDuration(countTimeMs, TimeUnit.MILLISECONDS).toString(TimeUnit.SECONDS);
		}

		public String spansDisplay(){
			return NumberFormatter.addCommas(spans);
		}

		public String slowSpansDisplay(){
			return NumberFormatter.addCommas(slowSpans);
		}
	}

	private record ClientSummary(
			String clientName,
			Summary summary){

		public static final Comparator<ClientSummary> COMPARE_ROWS = Comparator.comparing(
				clientSummary -> clientSummary.summary().rows());

		ClientSummary(List<LatestTableCount> latestTableCounts){
			this(latestTableCounts.getFirst().getKey().getClientName(), new Summary(latestTableCounts));

		}
	}


}
