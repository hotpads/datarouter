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
package io.datarouter.exception.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.instrumentation.exception.ExceptionRecordSummaryCollector;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryDto;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.MilliTime;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ExceptionRecordAggregationDailyDigest implements DailyDigest{

	private static final int EXCEPTIONS_THRESHOLD = 100;

	@Inject
	private ExceptionRecordSummaryCollector recordSummaryCollector;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ExceptionLinkBuilder exceptionLinkBuilder;

	@Override
	public String getTitle(){
		return "Exception Records";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.HIGH;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<ExceptionRecordSummaryDto> summaries = getExceptionSummaries(zoneId);
		if(summaries.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				DailyDigestRmlService.makeHeading("Exceptions", recordSummaryCollector.getBrowsePageLink(
						serviceName.get())
						.orElse("")),
				Rml.text("Aggregated for the current day (over " + EXCEPTIONS_THRESHOLD + ")").italic(),
				Rml.table(Rml.tableRow(
						Rml.tableHeader(Rml.text("Name")),
						Rml.tableHeader(Rml.text("Count"))))
						.with(summaries.stream()
								.map(summary -> Rml.tableRow(
										Rml.tableCell(Rml.text(summary.name())
												.link(exceptionLinkBuilder.exception(summary.sampleExceptionRecordId())
														.orElseThrow())),
										Rml.tableCell(
												Rml.text(NumberFormatter.addCommas(summary.numExceptions()))))))));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return List.of();
	}

	private List<ExceptionRecordSummaryDto> getExceptionSummaries(ZoneId zoneId){
		long startOfDayMs = MilliTime.atStartOfDay(zoneId).toEpochMilli();
		return recordSummaryCollector.getSummaries(
				startOfDayMs,
				System.currentTimeMillis(),
				serviceName.get(),
				Optional.of(EXCEPTIONS_THRESHOLD));
	}

}
