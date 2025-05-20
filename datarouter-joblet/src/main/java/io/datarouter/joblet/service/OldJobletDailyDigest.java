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
import io.datarouter.joblet.service.JobletDailyDigestService.OldJobletDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OldJobletDailyDigest implements DailyDigest{

	private static final String JOBLET_CATEGORY = "joblet";
	private static final String OLD_CATEGORY = "old";

	@Inject
	private DailyDigestRmlService digestService;
	@Inject
	private DatarouterJobletPaths paths;
	@Inject
	private JobletDailyDigestService jobletDailyDigestService;

	@Override
	public String getTitle(){
		return "Old or Stuck Joblets";
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
		Map<OldJobletDto,List<OldJobletDto>> rows = Scanner.of(jobletDailyDigestService.getOldJoblets())
				.groupBy(OldJobletDto::fromRequest, OldJobletDto::fromRequest);
		if(rows.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Old Joblets", paths.datarouter.joblets.list),
				jobletDailyDigestService.makeRelayTableForOldJoblets(rows)));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return Scanner.of(jobletDailyDigestService.getOldJoblets())
				.groupBy(joblet -> joblet.getKey().getType())
				.entrySet().stream()
				.map(entry -> new DailyDigestPlatformTask(
						List.of(JOBLET_CATEGORY, OLD_CATEGORY, entry.getKey()),
						List.of(JOBLET_CATEGORY, OLD_CATEGORY),
						"Old Joblets - " + entry.getKey(),
						Rml.doc(
								Rml.paragraph(
										Rml.text(NumberFormatter.addCommas(entry.getValue().size())).strong(),
										Rml.text(" old joblets found for "), Rml.text(entry.getKey()).code(),
										Rml.text(".")),
								Rml.paragraph(
										Rml.text("Decide whether to delete or retry these joblets.")))))
				.toList();
	}

}
