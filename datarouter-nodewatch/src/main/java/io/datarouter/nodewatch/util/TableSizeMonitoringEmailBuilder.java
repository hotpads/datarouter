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
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.p;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.email.StandardDatarouterEmailHeaderService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.service.TableSizeMonitoringService.CountStat;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.util.DateTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.time.ZonedDateFormaterTool;
import j2html.tags.ContainerTag;

@Singleton
public class TableSizeMonitoringEmailBuilder{

	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;

	public ContainerTag build(
			List<CountStat> thresholdRows,
			float percentageThreshold,
			List<CountStat> percentageRows,
			List<LatestTableCount> staleRows){
		var mainHeader = standardDatarouterEmailHeaderService.makeStandardHeader();
		var body = body(mainHeader);
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
			var table = makeEmailStaleTable(staleRows);
			body.with(div(header, header2, table));
		}
		return body;
	}

	public ContainerTag makeEmailStaleTable(List<LatestTableCount> staleRows){
		ZoneId zoneId = datarouterService.getZoneId();
		return new J2HtmlEmailTable<LatestTableCount>()
				.withColumn("Client", row -> row.getKey().getClientName())
				.withColumn(new J2HtmlEmailTableColumn<>("TABLE", row -> makeTableLink(
						row.getKey().getTableName(),
						row.getKey().getClientName())))
				.withColumn(alignRight("Latest Count", row -> NumberFormatter.addCommas(row.getNumRows())))
				.withColumn(alignRight("Date Updated",
						row -> ZonedDateFormaterTool.formatDateWithZone(row.getDateUpdated(), zoneId)))
				.withColumn(alignRight("Updated Ago", row -> DateTool.getAgoString(row.getDateUpdated().getTime())))
				.build(staleRows);
	}

	public ContainerTag makeCountStatTable(String comparableCount, List<CountStat> stats){
		ZoneId zoneId = datarouterService.getZoneId();
		return new J2HtmlEmailTable<CountStat>()
				.withColumn("Client", row -> row.latestSample.getKey().getClientName())
				.withColumn(new J2HtmlEmailTableColumn<>("Table", row -> makeTableLink(
						row.latestSample.getKey().getTableName(),
						row.latestSample.getKey().getClientName())))
				.withColumn("Date Updated",
						row -> ZonedDateFormaterTool.formatDateWithZone(row.latestSample.getDateUpdated(), zoneId))
				.withColumn(alignRight(comparableCount, row -> NumberFormatter.addCommas(row.previousCount)))
				.withColumn(alignRight("Latest Count", row -> NumberFormatter.addCommas(row.latestSample.getNumRows())))
				.withColumn(alignRight("% Increase", row -> new DecimalFormat("#,###.##").format(row.percentageIncrease)
						+ "%"))
				.withColumn(alignRight("Count Increase", row -> NumberFormatter.addCommas(row.countDifference)))
				.build(stats);
	}

	private static <T> J2HtmlEmailTableColumn<T> alignRight(String name, Function<T,Object> valueFunction){
		return J2HtmlEmailTableColumn.ofText(name, valueFunction)
				.withStyle("text-align:right");
	}

	public ContainerTag makeTableLink(String tableName, String clientName){
		String href = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.nodewatch.tableCount)
				.withParam("submitAction", "singleTable")
				.withParam("tableName", tableName)
				.withParam("clientName", clientName)
				.build();
		return a(tableName)
				.withHref(href);
	}

}