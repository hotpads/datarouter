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
import static j2html.TagCreator.td;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.config.service.DomainFinder;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.TableTag;

@Singleton
public class JobletDailyDigestService{

	private static final Duration OLD_JOBLETS = Duration.ofDays(30);

	@Inject
	private DatarouterJobletRequestDao dao;
	@Inject
	private TaskTrackerExceptionLink exceptionLink;
	@Inject
	private DomainFinder domainFinder;
	@Inject
	private ServletContextSupplier contextSupplier;

	public Map<FailedJobletDto,List<JobletRequest>> getFailedJoblets(){
		return dao.scanFailedJoblets()
				.groupBy(FailedJobletDto::fromRequest);
	}

	public List<JobletRequest> getOldJoblets(){
		return dao.scan()
				.include(joblet -> joblet.getKey().getAge().minus(OLD_JOBLETS).toDays() > 0)
				.list();
	}

	public TableTag makePageTableForOldJoblets(Map<OldJobletDto,List<OldJobletDto>> joblets){
		return new J2HtmlTable<Entry<OldJobletDto,List<OldJobletDto>>>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Type", row -> row.getKey().type)
				.withColumn("Execution Order", row -> row.getKey().executionOrder, Number::toString)
				.withColumn("Status", row -> row.getKey().status.persistentString)
				.withColumn("Num Timeouts", row -> row.getKey().numTimeouts, Number::toString)
				.withColumn("Num Failures", row -> row.getKey().numFailures, Number::toString)
				.withColumn("Count", row -> row.getValue().size(), Number::toString)
				.build(joblets.entrySet());
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

	public TableTag makePageTableForFailedJoblets(Map<FailedJobletDto,List<JobletRequest>> map){
		return new J2HtmlTable<Entry<FailedJobletDto,List<JobletRequest>>>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Type", row -> row.getKey().type)
				.withColumn("Execution Order", row -> row.getKey().executionOrder, Number::toString)
				.withColumn("Status", row -> row.getKey().status, JobletStatus::name)
				.withColumn("Num Timeouts", row -> row.getKey().numTimeouts, Number::toString)
				.withColumn("Num Failures", row -> row.getKey().numFailures, Number::toString)
				.withHtmlColumn("Exception", row -> {
					String exceptionId = row.getValue().stream()
							.map(JobletRequest::getExceptionRecordId)
							.findFirst()
							.orElse("");
					return td(makeExceptionLink(exceptionId));
				})
				.build(map.entrySet());
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
							return makeExceptionLink(exceptionId);
						}))
				.build(map.entrySet());
	}

	private ATag makeExceptionLink(String exceptionRecordId){
		String href = "https://" + domainFinder.getDomainPreferPublic() + contextSupplier.get().getContextPath()
				+ exceptionLink.buildExceptionDetailLink(exceptionRecordId);
		return a(exceptionRecordId)
				.withHref(href);
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

	private record FailedJobletDto(
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
