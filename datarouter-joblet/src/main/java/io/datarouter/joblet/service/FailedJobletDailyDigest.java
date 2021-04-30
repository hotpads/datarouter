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
package io.datarouter.joblet.service;

import static j2html.TagCreator.div;

import java.time.ZoneId;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.config.DatarouterJobletPaths;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.ContainerTag;

@Singleton
public class FailedJobletDailyDigest implements DailyDigest{

	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterJobletPaths paths;
	@Inject
	private JobletDailyDigestService jobletDailyDigestService;

	@Override
	public Optional<ContainerTag> getPageContent(ZoneId zoneId){
		var rows = jobletDailyDigestService.getFailedJoblets();
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Failed Joblets", paths.datarouter.joblets.list, getType());
		var table = jobletDailyDigestService.makePageTableForFailedJoblets(rows);
		return Optional.of(div(header, table));
	}

	@Override
	public Optional<ContainerTag> getEmailContent(){
		var rows = jobletDailyDigestService.getFailedJoblets();
		if(rows.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Failed Joblets", paths.datarouter.joblets.list, getType());
		var table = jobletDailyDigestService.makeEmailTableForFailedJoblets(rows);
		return Optional.of(div(header, table));
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public String getTitle(){
		return "Failed Joblets";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

}
