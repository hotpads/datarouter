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

import io.datarouter.joblet.config.DatarouterJobletPaths;
import io.datarouter.joblet.service.JobletDailyDigestService.OldJobletDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OldJobletDailyDigest implements DailyDigest{

	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterJobletPaths paths;
	@Inject
	private JobletDailyDigestService jobletDailyDigestService;

	@Override
	public Optional<DivTag> getPageContent(ZoneId zoneId){
		Map<OldJobletDto,List<OldJobletDto>> rows = Scanner.of(jobletDailyDigestService.getOldJoblets())
				.groupBy(OldJobletDto::fromRequest, OldJobletDto::fromRequest);
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Old Joblets", paths.datarouter.joblets.list);
		var table = jobletDailyDigestService.makePageTableForOldJoblets(rows);
		return Optional.of(div(header, table));
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		Map<OldJobletDto,List<OldJobletDto>> rows = Scanner.of(jobletDailyDigestService.getOldJoblets())
				.groupBy(OldJobletDto::fromRequest, OldJobletDto::fromRequest);
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Old Joblets", paths.datarouter.joblets.list);
		var table = jobletDailyDigestService.makeEmailTableForOldJoblets(rows);
		return Optional.of(div(header, table));
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public String getTitle(){
		return "Old or Stuck Joblets";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

}
