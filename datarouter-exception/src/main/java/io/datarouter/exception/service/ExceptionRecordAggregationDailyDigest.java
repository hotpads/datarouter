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
import static j2html.TagCreator.i;
import static j2html.TagCreator.small;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.exception.config.DatarouterExceptionPaths;
import io.datarouter.exception.web.ExceptionAnalysisHandler;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryCollector;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryDto;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.time.LocalDateTimeTool;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;

@Singleton
public class ExceptionRecordAggregationDailyDigest implements DailyDigest{

	private static final int EXCEPTIONS_THRESHOLD = 100;

	@Inject
	private DatarouterExceptionPaths paths;
	@Inject
	private ServletContextSupplier contextSupplier;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private ExceptionRecordSummaryCollector recordSummaryCollector;
	@Inject
	private ServiceName serviceName;

	@Override
	public Optional<DivTag> getPageContent(ZoneId zoneId){
		var header = digestService.makeHeader("Exceptions", recordSummaryCollector.getBrowsePageLink(serviceName
				.get()).orElse(""));
		var description = small("Aggregated for the current day (over " + EXCEPTIONS_THRESHOLD + ")");
		List<ExceptionRecordSummaryDto> summaries = getExceptionSummaries(zoneId);
		if(summaries.size() == 0){
			return Optional.empty();
		}
		return Optional.of(div(header, description, makePageTableV2(summaries)));
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		var header = digestService.makeHeader("Exceptions", recordSummaryCollector.getBrowsePageLink(serviceName
				.get()).orElse(""));
		var description = small("Aggregated for the current day (over " + EXCEPTIONS_THRESHOLD + ")");

		List<ExceptionRecordSummaryDto> summaries = getExceptionSummaries(zoneId);
		if(summaries.size() == 0){
			return Optional.empty();
		}
		return Optional.of(div(header, description, makeEmailTableV2(summaries)));
	}

	@Override
	public String getTitle(){
		return "Exception Records";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.HIGH;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	private List<ExceptionRecordSummaryDto> getExceptionSummaries(ZoneId zoneId){
		Instant startOfDay = LocalDateTimeTool.atStartOfDay(zoneId);
		return recordSummaryCollector.getSummaries(startOfDay.toEpochMilli(),
				System.currentTimeMillis(), serviceName.get(), Optional.of(EXCEPTIONS_THRESHOLD));
	}


	private TableTag makePageTableV2(List<ExceptionRecordSummaryDto> rows){
		return new J2HtmlTable<ExceptionRecordSummaryDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Name", ExceptionRecordSummaryDto::name)
				.withHtmlColumn(th("Count").withStyle("text-align:right"), row -> makeNumericPageTableCell(row
						.numExceptions()))
				.withHtmlColumn("Details", row -> td(a(i().withClass("far fa-file-alt"))
						.withClass("btn btn-link w-100 py-0")
						.withHref(contextSupplier.get().getContextPath() + makeExceptionRecordPathV2(row))))
				.withCaption("Total " + rows.size())
				.build(rows);
	}

	private TableTag makeEmailTableV2(List<ExceptionRecordSummaryDto> rows){
		return new J2HtmlEmailTable<ExceptionRecordSummaryDto>()
				.withColumn(new J2HtmlEmailTableColumn<>("Name", row -> digestService.makeATagLink(row.name(),
						makeExceptionRecordPathV2(row))))
				.withColumn(J2HtmlEmailTableColumn.ofNumber("Count", ExceptionRecordSummaryDto::numExceptions))
				.build(rows);
	}

	private TdTag makeNumericPageTableCell(long value){
		return td(NumberFormatter.addCommas(value))
				.attr("sorttable_customkey", value)
				.withStyle("text-align:right");
	}

	private String makeExceptionRecordPathV2(ExceptionRecordSummaryDto dto){
		return paths.datarouter.exception.details.toSlashedString()
				+ "?" + ExceptionAnalysisHandler.P_exceptionRecord + "=" + dto.sampleExceptionRecordId();
	}

}
