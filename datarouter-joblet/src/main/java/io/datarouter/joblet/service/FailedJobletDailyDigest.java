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

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.joblet.config.DatarouterJobletPaths;
import io.datarouter.joblet.service.JobletDailyDigestService.FailedJobletDto;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FailedJobletDailyDigest implements DailyDigest{

	private static final String JOBLET_CATEGORY = "joblet";
	private static final String FAILED_CATEGORY = "failed";

	@Inject
	private DailyDigestRmlService digestService;
	@Inject
	private DatarouterJobletPaths paths;
	@Inject
	private JobletDailyDigestService jobletDailyDigestService;
	@Inject
	private ExceptionLinkBuilder exceptionLinkBuilder;

	@Override
	public String getTitle(){
		return "Failed Joblets";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		Map<FailedJobletDto,List<JobletRequest>> rows = jobletDailyDigestService.getFailedJoblets();
		if(rows.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Failed Joblets", paths.datarouter.joblets.list),
				jobletDailyDigestService.makeRelayTableForFailedJoblets(rows)));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return jobletDailyDigestService.getFailedJoblets()
				.entrySet().stream()
				.map(entry -> {
					String exceptionRecordId = entry.getValue().stream()
							.map(JobletRequest::getExceptionRecordId)
							.findFirst()
							.orElse("");
					String exceptionHref = exceptionLinkBuilder.exception(exceptionRecordId).orElse("");

					return new DailyDigestPlatformTask(
							List.of(JOBLET_CATEGORY, FAILED_CATEGORY, entry.getKey().type()),
							List.of(JOBLET_CATEGORY, FAILED_CATEGORY),
							"Joblet " + entry.getKey().type() + " has failed runs",
							Rml.doc(
									Rml.paragraph(
											Rml.text("Joblet "), Rml.text(entry.getKey().type()).code(),
											Rml.text(" has " + entry.getKey().status().persistentString + " runs.")),
									Rml.paragraph(
											Rml.text(NumberFormatter.addCommas(entry.getKey().numTimeouts())),
											Rml.text(" timeouts"),
											Rml.hardBreak(),
											Rml.text(NumberFormatter.addCommas(entry.getKey().numFailures())),
											Rml.text(" failures"),
											Rml.hardBreak(),
											Rml.text("View exception record").link(exceptionHref))));
				})
				.toList();
	}

}
