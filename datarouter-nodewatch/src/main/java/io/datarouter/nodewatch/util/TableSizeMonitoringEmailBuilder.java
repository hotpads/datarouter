/**
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
package io.datarouter.nodewatch.util;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.p;
import static j2html.TagCreator.span;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.job.TableSizeMonitoringJob.CountStat;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.util.DateTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import j2html.tags.ContainerTag;

@Singleton
public class TableSizeMonitoringEmailBuilder{

	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterNodewatchPaths paths;

	public ContainerTag build(
			String serviceName,
			String serverNameString,
			List<CountStat> thresholdRows,
			float percentageThreshold,
			List<CountStat> percentageRows,
			List<TableCount> staleRows){
		var body = body()
				.with(h4("Service: " + serviceName));
		if(thresholdRows.size() > 0){
			var header = h4("Tables exceeding threshold");
			var table = makeCountStatTable("THRESHOLD", thresholdRows);
			body.with(div(header, table));
		}
		if(percentageRows.size() > 0){
			var header = h4("Tables that grew or shrank by more than " + percentageThreshold + "%");
			var header2 = p("Please reply-all to indicate whether the changes are expected.");
			var table = makeCountStatTable("PREVIOUS COUNT", percentageRows);
			body.with(div(header, header2, table));
		}
		if(staleRows.size() > 0){
			var header = h4("Unrecognized tables");
			var header2 = p("Please reply-all to confirm these tables can be dropped from the database.");
			var table = new J2HtmlEmailTable<TableCount>()
					.withColumn("CLIENT", row -> row.getKey().getClientName())
					.withColumn(new J2HtmlEmailTableColumn<>("TABLE", row -> makeTableLink(row)))
					.withColumn(alignRight("LATEST COUNT", row -> NumberFormatter.addCommas(row.getNumRows())))
					.withColumn(alignRight("DATE CREATED", row -> DateTool.getNumericDate(
							new Date(row.getKey().getCreatedMs()))))
					.withColumn(alignRight("DATE UPDATED", row -> DateTool.getNumericDate(row.getDateUpdated())))
					.build(staleRows);
			body.with(div(header, header2, table));
		}
		return body.with(
				br(),
				br(),
				span("Sent from: " + serverNameString));
	}

	private ContainerTag makeCountStatTable(String comparableCount, List<CountStat> stats){
		return new J2HtmlEmailTable<CountStat>()
				.withColumn("CLIENT", row -> row.latestSample.getKey().getClientName())
				.withColumn(new J2HtmlEmailTableColumn<>("TABLE", row -> makeTableLink(row.latestSample)))
				.withColumn("DATE UPDATED", row -> DateTool.getDateTime(row.latestSample.getDateUpdated()))
				.withColumn(alignRight(comparableCount, row -> NumberFormatter.addCommas(row.previousCount)))
				.withColumn(alignRight("LATEST COUNT", row -> NumberFormatter.addCommas(row.latestSample.getNumRows())))
				.withColumn(alignRight("% INCREASE", row -> new DecimalFormat("#,###.##").format(row.percentageIncrease)
						+ "%"))
				.withColumn(alignRight("COUNT INCREASE", row -> NumberFormatter.addCommas(row.countDifference)))
				.build(stats);
	}

	private static <T> J2HtmlEmailTableColumn<T> alignRight(String name, Function<T,Object> valueFunction){
		return J2HtmlEmailTableColumn.ofText(name, valueFunction)
				.withStyle("text-align:right");
	}

	private ContainerTag makeTableLink(TableCount tableCount){
		String href = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.nodewatch.tableCount)
				.withParam("submitAction", "singleTable")
				.withParam("tableName", tableCount.getKey().getTableName())
				.withParam("clientName", tableCount.getKey().getClientName())
				.build();
		return a(tableCount.getKey().getTableName())
				.withHref(href);
	}

}