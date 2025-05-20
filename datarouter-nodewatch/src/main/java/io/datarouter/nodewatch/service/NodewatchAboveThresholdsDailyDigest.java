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

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.service.TableSizeMonitoringService.AboveThreshold;
import io.datarouter.nodewatch.util.TableSizeMonitoringEmailBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class NodewatchAboveThresholdsDailyDigest implements DailyDigest{

	@Inject
	private TableSizeMonitoringService monitoringService;
	@Inject
	private TableSizeMonitoringEmailBuilder emailBuilder;
	@Inject
	private DailyDigestRmlService digestService;
	@Inject
	private DatarouterNodewatchPaths paths;

	@Override
	public String getTitle(){
		return "Table Thresholds";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		AboveThreshold thresholds = monitoringService.getAboveThresholdLists();

		Optional<Table> aboveThreshold = Optional.of(thresholds.aboveThreshold())
				.filter(items -> !items.isEmpty())
				.map(items -> emailBuilder.makeRelayThresholdCountStatTable("Threshold", items))
				.map(table -> new Table("Tables exceeding threshold", table));

		Optional<Table> abovePercentage = Optional.of(thresholds.abovePercentage())
				.filter(items -> !items.isEmpty())
				.map(items -> emailBuilder.makeRelayPercentageCountStatTable("Previous Count", items))
				.map(table -> new Table(
						"Tables that grew or shrank by more than " + TableSizeMonitoringService.PERCENTAGE_THRESHOLD
								+ "%",
						table));
		List<Table> tables = Scanner.of(aboveThreshold, abovePercentage)
				.concatOpt(Function.identity())
				.sort(Comparator.comparing(Table::header))
				.list();

		if(tables.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Table Thresholds", paths.datarouter.nodewatch.tables))
				.with(tables.stream()
						.flatMap(table -> Stream.of(
								Rml.heading(4, Rml.text(table.header())),
								table.table()))));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return List.of();
	}

	private record Table(
			String header,
			RmlBlock table){
	}

}
