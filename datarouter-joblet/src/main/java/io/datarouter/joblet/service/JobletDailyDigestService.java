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
package io.datarouter.joblet.service;

import static j2html.TagCreator.a;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobletDailyDigestService{

	private static final Duration OLD_JOBLETS = Duration.ofDays(30);

	@Inject
	private DatarouterJobletRequestDao dao;
	@Inject
	private ExceptionLinkBuilder exceptionLinkBuilder;

	public Map<FailedJobletDto,List<JobletRequest>> getFailedJoblets(){
		return dao.scanFailedJoblets()
				.groupBy(FailedJobletDto::fromRequest);
	}

	public List<JobletRequest> getOldJoblets(){
		return dao.scan()
				.include(joblet -> joblet.getKey().getAge().minus(OLD_JOBLETS).toDays() > 0)
				.list();
	}

	public TableTag makeEmailTableForOldJoblets(Map<OldJobletDto,List<OldJobletDto>> joblets){
		return new J2HtmlEmailTable<Entry<OldJobletDto,List<OldJobletDto>>>()
				.withColumn("Type", row -> row.getKey().type)
				.withColumn("Execution Order", row -> row.getKey().executionOrder)
				.withColumn("Status", row -> row.getKey().status.persistentString)
				.withColumn("Num Timeouts", row -> row.getKey().numTimeouts)
				.withColumn("Num Failures", row -> row.getKey().numFailures)
				.withColumn("Count", row -> row.getValue().size())
				.build(joblets.entrySet());
	}

	public RmlBlock makeRelayTableForOldJoblets(Map<OldJobletDto,List<OldJobletDto>> joblets){
		return Rml.table(
				Rml.tableRow(
						Rml.tableHeader(Rml.text("Type")),
						Rml.tableHeader(Rml.text("Execution Order")),
						Rml.tableHeader(Rml.text("Status")),
						Rml.tableHeader(Rml.text("Num Timeouts")),
						Rml.tableHeader(Rml.text("Num Failures")),
						Rml.tableHeader(Rml.text("Count"))))
				.with(joblets.entrySet().stream()
						.map(entry -> Rml.tableRow(
								Rml.tableCell(Rml.text(entry.getKey().type)),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(entry.getKey().executionOrder))),
								Rml.tableCell(Rml.text(entry.getKey().status.persistentString)),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(entry.getKey().numTimeouts))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(entry.getKey().numFailures))),
								Rml.tableCell(Rml.text(NumberFormatter.addCommas(entry.getValue().size()))))));
	}

	public TableTag makeEmailTableForFailedJoblets(Map<FailedJobletDto,List<JobletRequest>> map){
		return new J2HtmlEmailTable<Entry<FailedJobletDto,List<JobletRequest>>>()
				.withColumn("Type", row -> row.getKey().type)
				.withColumn("Execution Order", row -> row.getKey().executionOrder)
				.withColumn("Status", row -> row.getKey().status)
				.withColumn("Num Timeouts", row -> row.getKey().numTimeouts)
				.withColumn("Num Failures", row -> row.getKey().numFailures)
				.withColumn(new J2HtmlEmailTableColumn<>(
						"Exception",
						row -> {
							String exceptionId = row.getValue().stream()
									.map(JobletRequest::getExceptionRecordId)
									.findFirst()
									.orElse("");

							return a(exceptionId).withHref(exceptionLinkBuilder.exception(exceptionId).orElse(""));
						}))
				.build(map.entrySet());
	}

	public RmlBlock makeRelayTableForFailedJoblets(Map<FailedJobletDto,List<JobletRequest>> map){
		return Rml.table(
				Rml.tableRow(
						Rml.tableHeader(Rml.text("Type")),
						Rml.tableHeader(Rml.text("Execution Order")),
						Rml.tableHeader(Rml.text("Status")),
						Rml.tableHeader(Rml.text("Num Timeouts")),
						Rml.tableHeader(Rml.text("Num Failures")),
						Rml.tableHeader(Rml.text("Exception"))))
				.with(map.entrySet().stream()
						.map(entry -> {
							String exceptionRecordId = entry.getValue().stream()
									.map(JobletRequest::getExceptionRecordId)
									.findFirst()
									.orElse("");
							String exceptionHref = exceptionLinkBuilder.exception(exceptionRecordId).orElse("");

							return Rml.tableRow(
									Rml.tableCell(Rml.text(entry.getKey().type)),
									Rml.tableCell(Rml.text(NumberFormatter.addCommas(entry.getKey().executionOrder))),
									Rml.tableCell(Rml.text(entry.getKey().status.persistentString)),
									Rml.tableCell(Rml.text(NumberFormatter.addCommas(entry.getKey().numTimeouts))),
									Rml.tableCell(Rml.text(NumberFormatter.addCommas(entry.getKey().numFailures))),
									Rml.tableCell(Rml.text(exceptionRecordId).link(exceptionHref)));
						}));
	}

	public record OldJobletDto(
			String type,
			int executionOrder,
			JobletStatus status,
			int numTimeouts,
			int numFailures){

		public static OldJobletDto fromRequest(JobletRequest request){
			return new OldJobletDto(
					request.getKey().getType(),
					request.getKey().getExecutionOrder(),
					request.getStatus(),
					request.getNumTimeouts(),
					request.getNumFailures());
		}
	}

	public record FailedJobletDto(
			String type,
			int executionOrder,
			JobletStatus status,
			int numTimeouts,
			int numFailures){

		public static FailedJobletDto fromRequest(JobletRequest request){
			return new FailedJobletDto(
					request.getKey().getType(),
					request.getKey().getExecutionOrder(),
					request.getStatus(),
					request.getNumTimeouts(),
					request.getNumFailures());
		}

	}

}
