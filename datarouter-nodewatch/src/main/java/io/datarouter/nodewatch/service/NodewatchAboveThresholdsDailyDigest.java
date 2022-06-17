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
package io.datarouter.nodewatch.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.td;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.service.TableSizeMonitoringService.CountStat;
import io.datarouter.nodewatch.util.TableSizeMonitoringEmailBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;

@Singleton
public class NodewatchAboveThresholdsDailyDigest implements DailyDigest{

	@Inject
	private TableSizeMonitoringService monitoringService;
	@Inject
	private TableSizeMonitoringEmailBuilder emailBuilder;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterNodewatchPaths paths;

	@Override
	public Optional<DivTag> getPageContent(ZoneId zoneId){
		Optional<Pair<String,DivTag>> aboveThresholdList = Scanner
				.of(monitoringService.getAboveThresholdLists().getLeft())
				.listTo(rows -> makePageTable(rows, "Tables exceeding threshold", zoneId));
		Optional<Pair<String,DivTag>> abovePercentageList = Scanner
				.of(monitoringService.getAboveThresholdLists().getRight())
				.listTo(rows -> makePageTable(rows, "Tables that grew or shrank by more than "
						+ TableSizeMonitoringService.PERCENTAGE_THRESHOLD + "%", zoneId));
		List<DivTag> tables = Stream.of(aboveThresholdList, abovePercentageList)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted(Comparator.comparing(Pair::getLeft))
				.map(Pair::getRight)
				.collect(Collectors.toList());
		if(tables.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Table Thresholds", paths.datarouter.nodewatch.threshold);
		return Optional.of(div(header, each(tables, tag -> div(tag))));
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		Optional<Pair<String,DivTag>> aboveThresholdList = Scanner
				.of(monitoringService.getAboveThresholdLists().getLeft())
				.listTo(rows -> makeEmailTable(rows, "Tables exceeding threshold"));
		Optional<Pair<String,DivTag>> abovePercentageList = Scanner
				.of(monitoringService.getAboveThresholdLists().getRight())
				.listTo(rows -> makeEmailTable(rows, "Tables that grew or shrank by more than "
						+ TableSizeMonitoringService.PERCENTAGE_THRESHOLD + "%"));
		List<DivTag> tables = Stream.of(aboveThresholdList, abovePercentageList)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted(Comparator.comparing(Pair::getLeft))
				.map(Pair::getRight)
				.collect(Collectors.toList());
		if(tables.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Table Thresholds", paths.datarouter.nodewatch.threshold);
		return Optional.of(div(header, each(tables, tag -> div(tag))));
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	private Optional<Pair<String,DivTag>> makePageTable(List<CountStat> rows, String header, ZoneId zoneId){
		if(rows.isEmpty()){
			return Optional.empty();
		}
		TableTag table = new J2HtmlTable<CountStat>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Client", row -> row.latestSample.getKey().getClientName())
				.withHtmlColumn("Table", row -> td(emailBuilder.makeTableLink(
						row.latestSample.getKey().getTableName(),
						row.latestSample.getKey().getClientName())))
				.withColumn("Date Updated",
						row -> ZonedDateFormatterTool.formatInstantWithZone(row.latestSample.getDateUpdated(), zoneId))
				.withColumn("Previous Count ", row -> row.latestSample.getDateUpdated())
				.withColumn("Latest Count", row -> row.latestSample.getDateUpdated())
				.withColumn("% Increase", row -> row.latestSample.getDateUpdated())
				.withColumn("Count Increase", row -> row.latestSample.getDateUpdated())
				.build(rows);
		return Optional.of(new Pair<>(header, div(h4(header), table)));
	}

	private Optional<Pair<String,DivTag>> makeEmailTable(List<CountStat> rows, String header){
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var table = emailBuilder.makeCountStatTable("Previous Count", rows);
		return Optional.of(new Pair<>(header, div(h4(header), table)));
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public String getTitle(){
		return "Table Thresholds";
	}

}
