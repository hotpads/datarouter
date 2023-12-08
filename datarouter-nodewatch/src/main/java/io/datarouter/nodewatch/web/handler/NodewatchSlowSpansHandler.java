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
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.td;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchLinks;
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

public class NodewatchSlowSpansHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchLinks links;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;

	@Handler
	private Mav slowSpans(){
		List<LatestTableCount> latestTableCountsWithSlowSpans = latestTableCountDao.scan()
				.include(latestTableCount -> latestTableCount.getNumSlowSpans() > 0)
				.list();
		var content = div(
				NodewatchHtml.makeHeader(
						"Slow Spans",
						"Tables with any spans that took longer than expected to count."
								+ "  Consider reducing the sampling size"),
				navService.makeNavTabs(paths.datarouter.nodewatch.slowSpans).render(),
				br(),
				makeSlowSpansDiv(latestTableCountsWithSlowSpans))
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Slow Spans")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DivTag makeSlowSpansDiv(List<LatestTableCount> latestTableCountsWithSlowSpans){
		if(latestTableCountsWithSlowSpans.isEmpty()){
			return div(
					br(),
					h5("No slow spans found!").withClass("ml-5"));
		}
		List<LatestTableCount> rows = Scanner.of(latestTableCountsWithSlowSpans)
				.sort(Comparator.comparing(LatestTableCount::getPercentSlowSpans).reversed())
				.list();
		var table = makeTableBuilder().build(rows);
		return div(table);
	}

	private J2HtmlTable<LatestTableCount> makeTableBuilder(){
		return new J2HtmlTable<LatestTableCount>()
				.withClasses("sortable table table-sm table-striped my-2 border")
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
						"Spans",
						LatestTableCount::getNumSpans,
						NumberFormatter::addCommas)
				.withColumn(
						"Slow Spans",
						LatestTableCount::getNumSlowSpans,
						NumberFormatter::addCommas)
				.withColumn(
						"% Slow",
						LatestTableCount::getPercentSlowSpans,
						pct -> NumberFormatter.format(pct, 1) + "%")
				.withColumn(
						"Rows",
						LatestTableCount::getNumRows,
						NumberFormatter::addCommas)
				.withColumn(
						"Count Time",
						LatestTableCount::getCountTimeMs,
						ms -> new DatarouterDuration(ms, TimeUnit.MILLISECONDS).toString(TimeUnit.SECONDS))
				.withColumn(
						"Updated",
						LatestTableCount::getDateUpdated,
						instant -> DatarouterDuration.age(instant).toString(TimeUnit.MINUTES));
	}

}
