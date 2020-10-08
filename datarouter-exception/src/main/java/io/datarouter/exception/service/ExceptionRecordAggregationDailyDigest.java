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
package io.datarouter.exception.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.i;
import static j2html.TagCreator.small;
import static j2html.TagCreator.td;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.config.DatarouterExceptionPaths;
import io.datarouter.exception.storage.summary.DatarouterExceptionRecordSummaryDao;
import io.datarouter.exception.storage.summary.ExceptionRecordSummary;
import io.datarouter.exception.storage.summary.ExceptionRecordSummaryKey;
import io.datarouter.exception.web.ExceptionAnalysisHandler;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.ContainerTag;

@Singleton
public class ExceptionRecordAggregationDailyDigest implements DailyDigest{

	private static final Comparator<AggregatedExceptionDto> COMPARATOR = Comparator
			.comparing((AggregatedExceptionDto dto) -> dto.value.count)
			.reversed();

	@Inject
	private DatarouterExceptionRecordSummaryDao dao;
	@Inject
	private DatarouterExceptionPaths paths;
	@Inject
	private ServletContextSupplier servletContextSupplier;

	@Override
	public Optional<ContainerTag> getContent(){
		Map<AggregatedExceptionKeyDto,AggregatedExceptionValueDto> aggregatedExceptions = new HashMap<>();
		for(ExceptionRecordSummary exception : dao.scan()
				.advanceUntil(key -> key.getKey().getReversePeriodStart() > atStartOfDay())
				.iterable()){
			var key = new AggregatedExceptionKeyDto(exception.getKey());
			var value = new AggregatedExceptionValueDto(exception);
			AggregatedExceptionValueDto mapValue = aggregatedExceptions.get(key);
			if(mapValue != null){
				value.addCount(mapValue.count);
			}
			aggregatedExceptions.put(key, value);
		}
		List<AggregatedExceptionDto> aggregated = Scanner.of(aggregatedExceptions.entrySet())
				.map(entry -> new AggregatedExceptionDto(entry.getKey(), entry.getValue()))
				.sorted(COMPARATOR)
				.list();
		if(aggregated.size() == 0){
			return Optional.empty();
		}
		return Optional.of(makeContent(aggregated));
	}

	public ContainerTag makeContent(List<AggregatedExceptionDto> rows){
		var header = h3("Exceptions");
		var description = small("Aggregated for the current day");
		var table = new J2HtmlTable<AggregatedExceptionDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Type", row -> row.key.type)
				.withColumn("Location", row -> row.key.location)
				.withColumn("Count", row -> row.value.count)
				.withHtmlColumn("Details", row -> {
					String id = row.value.detailsLink;
					String link = servletContextSupplier.get().getContextPath() + paths.datarouter.exception.details
							.toSlashedString() + "?" + ExceptionAnalysisHandler.P_exceptionRecord + "=" + id;
					return td(a(i().withClass("far fa-file-alt"))
							.withClass("btn btn-link w-100 py-0")
							.withHref(link));
				})
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(header, description, table);
	}

	private static class AggregatedExceptionDto{

		public final AggregatedExceptionKeyDto key;
		public final AggregatedExceptionValueDto value;

		public AggregatedExceptionDto(AggregatedExceptionKeyDto key, AggregatedExceptionValueDto value){
			this.key = key;
			this.value = value;
		}

	}

	private static class AggregatedExceptionKeyDto{

		public final String type;
		public final String location;

		public AggregatedExceptionKeyDto(ExceptionRecordSummaryKey key){
			this.type = key.getType();
			this.location = key.getExceptionLocation();
		}

		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof AggregatedExceptionKeyDto)){
				return false;
			}
			AggregatedExceptionKeyDto that = (AggregatedExceptionKeyDto) other;
			return this.type.equals(that.type) && this.location.equals(that.location);
		}

		@Override
		public int hashCode(){
			return Objects.hash(type, location);
		}

	}

	private static class AggregatedExceptionValueDto{

		public final String detailsLink;
		public long count;

		public AggregatedExceptionValueDto(ExceptionRecordSummary summary){
			this.count = summary.getNumExceptions();
			this.detailsLink = summary.getSampleExceptionRecordId();
		}

		public void addCount(long count){
			this.count = this.count + count;
		}

	}

	private static long atStartOfDay(){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return Long.MAX_VALUE - localDateTimeToDate(startOfDay).getTime();
	}

	private static Date localDateTimeToDate(LocalDateTime localDateTime){
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

}
