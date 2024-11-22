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

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.small;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryCollector;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryDto;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.MilliTime;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
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
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		var header = DailyDigestService.makeHeader("Exceptions", recordSummaryCollector.getBrowsePageLink(serviceName
				.get()).orElse(""));
		var description = small("Aggregated for the current day (over " + EXCEPTIONS_THRESHOLD + ")");

		List<ExceptionRecordSummaryDto> summaries = getExceptionSummaries(zoneId);
		if(summaries.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(div(header, description, makeEmailTableV2(summaries)));
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<ExceptionRecordSummaryDto> summaries = getExceptionSummaries(zoneId);
		if(summaries.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				DailyDigestService.makeHeading("Exceptions", recordSummaryCollector.getBrowsePageLink(serviceName.get())
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

	private List<ExceptionRecordSummaryDto> getExceptionSummaries(ZoneId zoneId){
		long startOfDayMs = MilliTime.atStartOfDay(zoneId).toEpochMilli();
		return recordSummaryCollector.getSummaries(
				startOfDayMs,
				System.currentTimeMillis(),
				serviceName.get(),
				Optional.of(EXCEPTIONS_THRESHOLD));
	}

	private TableTag makeEmailTableV2(List<ExceptionRecordSummaryDto> rows){
		return new J2HtmlEmailTable<ExceptionRecordSummaryDto>()
				.withColumn(new J2HtmlEmailTableColumn<>("Name", this::makeExceptionLink))
				.withColumn(J2HtmlEmailTableColumn.ofNumber("Count", ExceptionRecordSummaryDto::numExceptions))
				.build(rows);
	}

	private ATag makeExceptionLink(ExceptionRecordSummaryDto dto){
		String href = exceptionLinkBuilder.exception(dto.sampleExceptionRecordId()).orElseThrow();
		return a(dto.name()).withHref(href);
	}

}
