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

import static j2html.TagCreator.div;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.joblet.config.DatarouterJobletPaths;
import io.datarouter.joblet.service.JobletDailyDigestService.FailedJobletDto;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FailedJobletDailyDigest implements DailyDigest{

	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterJobletPaths paths;
	@Inject
	private JobletDailyDigestService jobletDailyDigestService;

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
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		Map<FailedJobletDto,List<JobletRequest>> rows = jobletDailyDigestService.getFailedJoblets();
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Failed Joblets", paths.datarouter.joblets.list);
		var table = jobletDailyDigestService.makeEmailTableForFailedJoblets(rows);
		return Optional.of(div(header, table));
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

}
