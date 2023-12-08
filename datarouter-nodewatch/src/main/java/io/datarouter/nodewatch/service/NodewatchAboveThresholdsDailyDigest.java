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

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.service.TableSizeMonitoringService.PercentageCountStat;
import io.datarouter.nodewatch.service.TableSizeMonitoringService.ThresholdCountStat;
import io.datarouter.nodewatch.util.TableSizeMonitoringEmailBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.WarnOnModifyList;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		Optional<TableRow> aboveThresholdList = Scanner
				.of(monitoringService.getAboveThresholdLists().aboveThreshold())
				.listTo(rows -> makeThresholdEmailTable(rows, "Tables exceeding threshold"));
		Optional<TableRow> abovePercentageList = Scanner
				.of(monitoringService.getAboveThresholdLists().abovePercentage())
				.listTo(rows -> makePercentageEmailTable(rows, "Tables that grew or shrank by more than "
						+ TableSizeMonitoringService.PERCENTAGE_THRESHOLD + "%"));
		List<DivTag> tables = Stream.of(aboveThresholdList, abovePercentageList)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted(Comparator.comparing(TableRow::header))
				.map(TableRow::content)
				.collect(WarnOnModifyList.deprecatedCollector());
		if(tables.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Table Thresholds", paths.datarouter.nodewatch.tables);
		return Optional.of(div(header, each(tables, content -> TagCreator.div((DomContent)content))));
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	private Optional<TableRow> makePercentageEmailTable(List<PercentageCountStat> rows, String header){
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var table = emailBuilder.makePercentageCountStatTable("Previous Count", rows);
		return Optional.of(new TableRow(header, div(h4(header), table)));
	}

	private Optional<TableRow> makeThresholdEmailTable(List<ThresholdCountStat> rows, String header){
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var table = emailBuilder.makeThresholdCountStatTable("Threshold", rows);
		return Optional.of(new TableRow(header, div(h4(header), table)));
	}

	private record TableRow(
			String header,
			DivTag content){
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
