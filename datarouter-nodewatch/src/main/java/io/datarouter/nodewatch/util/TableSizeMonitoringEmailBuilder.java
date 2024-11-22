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

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlTable;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.service.TableSizeMonitoringService.PercentageCountStat;
import io.datarouter.nodewatch.service.TableSizeMonitoringService.ThresholdCountStat;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.web.handler.NodewatchTableHandler;
import io.datarouter.util.DateTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.config.properties.DefaultEmailDistributionListZoneId;
import io.datarouter.web.digest.DailyDigestEmailZoneId;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.BodyTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TableSizeMonitoringEmailBuilder{

	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private DailyDigestEmailZoneId dailyDigestEmailZoneId;
	@Inject
	private DefaultEmailDistributionListZoneId defaultDistributionListZoneId;

	public BodyTag build(
			List<ThresholdCountStat> thresholdRows,
			float percentageThreshold,
			List<PercentageCountStat> percentageRows,
			List<LatestTableCount> staleRows){
		var mainHeader = standardDatarouterEmailHeaderService.makeStandardHeader();
		var body = body(mainHeader);
		if(!thresholdRows.isEmpty()){
			var header = h4("Tables exceeding threshold");
			var table = makeThresholdCountStatTable("THRESHOLD", thresholdRows);
			body.with(div(header, table));
		}
		if(!percentageRows.isEmpty()){
			var header = h4("Tables that grew or shrank by more than " + percentageThreshold + "%");
			var header2 = p("Please reply-all to indicate whether the changes are expected.");
			var table = makePercentageCountStatTable("PREVIOUS COUNT", percentageRows);
			body.with(div(header, header2, table));
		}
		if(!staleRows.isEmpty()){
			var header = h4("Unrecognized tables");
			var header2 = p("Please reply-all to confirm these tables can be dropped from the database.");
			var table = makeEmailStaleTable(staleRows, dailyDigestEmailZoneId.get());
			body.with(div(header, header2, table));
		}
		return body;
	}

	public TableTag makeEmailStaleTable(List<LatestTableCount> staleRows, ZoneId zoneId){
		return new J2HtmlEmailTable<LatestTableCount>()
				.withColumn("Client", row -> row.getKey().getClientName())
				.withColumn(new J2HtmlEmailTableColumn<>("TABLE", row -> makeTableLink(
						row.getKey().getTableName(),
						row.getKey().getClientName())))
				.withColumn(alignRight("Latest Count", row -> NumberFormatter.addCommas(row.getNumRows())))
				.withColumn(alignRight("Date Updated",
						row -> ZonedDateFormatterTool.formatInstantWithZone(row.getDateUpdated(), zoneId)))
				.withColumn(alignRight("Updated Ago",
						row -> DateTool.getAgoString(row.getDateUpdated().toEpochMilli())))
				.build(staleRows);
	}

	public RmlTable makeRelayStaleTable(List<LatestTableCount> staleRows, ZoneId zoneId){
		return Rml.table(
				Rml.tableRow(
						Rml.tableHeader(Rml.text("Client")),
						Rml.tableHeader(Rml.text("TABLE")),
						Rml.tableHeader(Rml.text("Latest Count")),
						Rml.tableHeader(Rml.text("Date Updated")),
						Rml.tableHeader(Rml.text("Updated Ago"))))
				.with(staleRows.stream()
						.map(row -> Rml.tableRow(
								Rml.tableCell(Rml.text(row.getKey().getClientName())),
								Rml.tableCell(Rml.text(row.getKey().getTableName())
										.link(makeTableHref(
												row.getKey().getTableName(),
												row.getKey().getClientName()))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(row.getNumRows()))),
								Rml.tableCell(Rml.text(ZonedDateFormatterTool.formatInstantWithZone(
										row.getDateUpdated(), zoneId))),
								Rml.tableCell(Rml.text(DateTool.getAgoString(row.getDateUpdated().toEpochMilli()))))));
	}

	public TableTag makePercentageCountStatTable(String comparableCount, List<PercentageCountStat> stats){
		ZoneId zoneId = defaultDistributionListZoneId.get();
		return new J2HtmlEmailTable<PercentageCountStat>()
				.withColumn("Client", row -> row.latestSample.getKey().getClientName())
				.withColumn(new J2HtmlEmailTableColumn<>("Table", row -> makeTableLink(
						row.latestSample.getKey().getTableName(),
						row.latestSample.getKey().getClientName())))
				.withColumn("Date Updated",
						row -> ZonedDateFormatterTool.formatInstantWithZone(row.latestSample.getDateUpdated(), zoneId))
				.withColumn(alignRight(comparableCount, row -> NumberFormatter.addCommas(row.previousCount)))
				.withColumn(alignRight("Latest Count", row -> NumberFormatter.addCommas(row.latestSample.getNumRows())))
				.withColumn(alignRight("% Increase", row -> new DecimalFormat("#,###.##").format(row.percentageIncrease)
						+ "%"))
				.withColumn(alignRight("Count Increase", row -> NumberFormatter.addCommas(row.countDifference)))
				.build(stats);
	}

	public RmlTable makeRelayPercentageCountStatTable(String comparableCount, List<PercentageCountStat> stats){
		return Rml.table(
				Rml.tableRow(
						Rml.tableHeader(Rml.text("Client")),
						Rml.tableHeader(Rml.text("Table")),
						Rml.tableHeader(Rml.text("Date Updated")),
						Rml.tableHeader(Rml.text(comparableCount)),
						Rml.tableHeader(Rml.text("Latest Count")),
						Rml.tableHeader(Rml.text("% Increase")),
						Rml.tableHeader(Rml.text("Count Increase"))))
				.with(stats.stream()
						.map(row -> Rml.tableRow(
								Rml.tableCell(Rml.text(row.latestSample.getKey().getClientName())),
								Rml.tableCell(Rml.text(row.latestSample.getKey().getTableName())
										.link(makeTableHref(
												row.latestSample.getKey().getTableName(),
												row.latestSample.getKey().getClientName()))),
								Rml.tableCell(Rml.text(ZonedDateFormatterTool.formatInstantWithZone(
										row.latestSample.getDateUpdated(), defaultDistributionListZoneId.get()))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(row.previousCount))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(row.latestSample.getNumRows()))),
								Rml.tableCell(Rml.text(NumberFormatter.format(row.percentageIncrease, "", "%", 2))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(row.countDifference))))));
	}

	public TableTag makeThresholdCountStatTable(String comparableCount, List<ThresholdCountStat> stats){
		ZoneId zoneId = defaultDistributionListZoneId.get();
		return new J2HtmlEmailTable<ThresholdCountStat>()
				.withColumn("Client", row -> row.latestSample().getKey().getClientName())
				.withColumn(new J2HtmlEmailTableColumn<>("Table", row -> makeTableLink(
						row.latestSample().getKey().getTableName(),
						row.latestSample().getKey().getClientName())))
				.withColumn("Date Updated",
						row -> ZonedDateFormatterTool.formatInstantWithZone(
								row.latestSample().getDateUpdated(), zoneId))
				.withColumn(alignRight(comparableCount, row -> NumberFormatter.addCommas(row.threshold())))
				.withColumn(alignRight("Latest Count", row -> NumberFormatter.addCommas(
						row.latestSample().getNumRows())))
				.build(stats);
	}

	public RmlTable makeRelayThresholdCountStatTable(String comparableCount, List<ThresholdCountStat> stats){
		ZoneId zoneId = defaultDistributionListZoneId.get();
		return Rml.table(
				Rml.tableRow(
						Rml.tableHeader(Rml.text("Client")),
						Rml.tableHeader(Rml.text("Table")),
						Rml.tableHeader(Rml.text("Date Updated")),
						Rml.tableHeader(Rml.text(comparableCount)),
						Rml.tableHeader(Rml.text("Latest Count"))))
				.with(stats.stream()
						.map(row -> Rml.tableRow(
								Rml.tableCell(Rml.text(row.latestSample().getKey().getClientName())),
								Rml.tableCell(Rml.text(row.latestSample().getKey().getTableName())
										.link(makeTableHref(
												row.latestSample().getKey().getTableName(),
												row.latestSample().getKey().getClientName()))),
								Rml.tableCell(Rml.text(ZonedDateFormatterTool.formatInstantWithZone(
										row.latestSample().getDateUpdated(),
										zoneId))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(row.threshold()))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(row.latestSample().getNumRows()))))));
	}

	private static <T> J2HtmlEmailTableColumn<T> alignRight(String name, Function<T,Object> valueFunction){
		return J2HtmlEmailTableColumn.ofText(name, valueFunction)
				.withStyle("text-align:right");
	}

	public ATag makeTableLink(String tableName, String clientName){
		return a(tableName)
				.withHref(makeTableHref(tableName, clientName));
	}

	public String makeTableHref(String tableName, String clientName){
		return emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.nodewatch.table)
				.withParam(NodewatchTableHandler.P_clientName, clientName)
				.withParam(NodewatchTableHandler.P_tableName, tableName)
				.build();
	}

}
