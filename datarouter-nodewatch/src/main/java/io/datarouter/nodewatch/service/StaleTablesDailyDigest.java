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
package io.datarouter.nodewatch.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.util.TableSizeMonitoringEmailBuilder;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StaleTablesDailyDigest implements DailyDigest{

	@Inject
	private TableSizeMonitoringService monitoringService;
	@Inject
	private TableSizeMonitoringEmailBuilder emailBuilder;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private DailyDigestRmlService digestService;

	@Override
	public String getTitle(){
		return "Stale Tables";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.STALE_TABLES;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<LatestTableCount> staleTables = monitoringService.getStaleTableEntries();
		if(staleTables.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Stale Tables", paths.datarouter.nodewatch.tables),
				emailBuilder.makeRelayStaleTable(staleTables, zoneId)));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return List.of();
	}

}
