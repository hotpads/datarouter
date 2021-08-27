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
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.httpclient.dto.BaseGsonDto;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink;
import io.datarouter.util.time.ZonedDateFormaterTool;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.ContainerTag;

@Singleton
public class JobletDailyDigestService{

	private static final Duration OLD_JOBLETS = Duration.ofDays(30);

	@Inject
	private DatarouterJobletRequestDao dao;
	@Inject
	private TaskTrackerExceptionLink exceptionLink;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private ServletContextSupplier contextSupplier;

	public Map<FailedJobletDto,List<JobletRequest>> getFailedJoblets(){
		return dao.scanFailedJoblets()
				.groupBy(FailedJobletDto::new);
	}

	public List<JobletRequest> getOldJoblets(){
		return dao.scan()
				.include(joblet -> joblet.getKey().getAge().minus(OLD_JOBLETS).toDays() > 0)
				.list();
	}

	public ContainerTag<?> makePageTableForOldJoblets(List<JobletRequest> joblets, ZoneId zoneId){
		return new J2HtmlTable<JobletRequest>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Type", row -> row.getKey().getType())
				.withColumn("Created", row -> ZonedDateFormaterTool.formatLongMsWithZone(row.getKey().getCreated(),
						zoneId))
				.withColumn("Execution Order", row -> row.getKey().getExecutionOrder())
				.withColumn("Status", row -> row.getStatus().getPersistentString())
				.withColumn("Num Timeouts", row -> row.getNumTimeouts())
				.withColumn("Num Failures", row -> row.getNumFailures())
				.build(joblets);
	}

	public ContainerTag<?> makeEmailTableForOldJoblets(Map<OldJobletDto,List<OldJobletDto>> joblets){
		return new J2HtmlEmailTable<Entry<OldJobletDto,List<OldJobletDto>>>()
				.withColumn("Type", row -> row.getKey().type)
				.withColumn("Execution Order", row -> row.getKey().executionOrder)
				.withColumn("Status", row -> row.getKey().status.getPersistentString())
				.withColumn("Num Timeouts", row -> row.getKey().numTimeouts)
				.withColumn("Num Failures", row -> row.getKey().numFailures)
				.withColumn("Count", row -> row.getValue().size())
				.build(joblets.entrySet());
	}

	public ContainerTag<?> makePageTableForFailedJoblets(Map<FailedJobletDto,List<JobletRequest>> map){
		return new J2HtmlTable<Entry<FailedJobletDto,List<JobletRequest>>>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Type", row -> row.getKey().type)
				.withColumn("Execution Order", row -> row.getKey().executionOrder)
				.withColumn("Status", row -> row.getKey().status)
				.withColumn("Num Timeouts", row -> row.getKey().numTimeouts)
				.withColumn("Num Failures", row -> row.getKey().numFailures)
				.withHtmlColumn("Exception", row -> {
					String exceptionId = row.getValue().stream()
							.map(JobletRequest::getExceptionRecordId)
							.findFirst()
							.orElse("");
					return td(makeExceptionLink(exceptionId));
				})
				.build(map.entrySet());
	}

	public ContainerTag<?> makeEmailTableForFailedJoblets(Map<FailedJobletDto,List<JobletRequest>> map){
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

	private ContainerTag<?> makeExceptionLink(String exceptionRecordId){
		String href = "https://" + datarouterService.getDomainPreferPublic() + contextSupplier.get().getContextPath()
				+ exceptionLink.buildExceptionDetailLink(exceptionRecordId);
		return a(exceptionRecordId)
				.withHref(href);
	}

	public static class OldJobletDto extends BaseGsonDto{

		public final String type;
		public final int executionOrder;
		public final JobletStatus status;
		public final int numTimeouts;
		public final int numFailures;

		public OldJobletDto(JobletRequest request){
			this(
					request.getKey().getType(),
					request.getKey().getExecutionOrder(),
					request.getStatus(),
					request.getNumTimeouts(),
					request.getNumFailures());
		}

		public OldJobletDto(String type, int executionOrder, JobletStatus status, int numTimeouts, int numFailures){
			this.type = type;
			this.executionOrder = executionOrder;
			this.status = status;
			this.numTimeouts = numTimeouts;
			this.numFailures = numFailures;
		}
	}

	private static class FailedJobletDto extends BaseGsonDto{

		public final String type;
		public final int executionOrder;
		public final JobletStatus status;
		public final int numTimeouts;
		public final int numFailures;

		public FailedJobletDto(JobletRequest request){
			this(
					request.getKey().getType(),
					request.getKey().getExecutionOrder(),
					request.getStatus(),
					request.getNumTimeouts(),
					request.getNumFailures());
		}

		public FailedJobletDto(String type, int executionOrder, JobletStatus status, int numTimeouts, int numFailures){
			this.type = type;
			this.executionOrder = executionOrder;
			this.status = status;
			this.numTimeouts = numTimeouts;
			this.numFailures = numFailures;
		}

	}

}
