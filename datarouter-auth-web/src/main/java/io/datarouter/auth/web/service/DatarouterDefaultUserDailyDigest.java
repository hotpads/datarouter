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
package io.datarouter.auth.web.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserKey;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDefaultUserDailyDigest implements DailyDigest{

	private static final String DEFAULT_USER_CATEGORY = "defaultUser";

	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DailyDigestRmlService dailyDigestService;
	@Inject
	private DatarouterAuthPaths authPaths;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	@Override
	public String getTitle(){
		return "Users with default userIds";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.MEDIUM;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		if(!serverTypeDetector.mightBeProduction()){
			return Optional.empty();
		}
		Optional<List<String>> username = findDefaultUsername()
				.map(List::of);
		if(username.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(
				Rml.paragraph(
						dailyDigestService.makeHeading("Please remove the default user", authPaths.admin.viewUsers),
						Rml.table(
								Rml.tableRow(Rml.tableHeader(Rml.text("Users"))))
								.with(username.get().stream()
										.map(Rml::text)
										.map(Rml::tableCell)
										.map(Rml::tableRow))));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		if(!serverTypeDetector.mightBeProduction()){
			return List.of();
		}
		return findDefaultUsername()
				.map(username -> new DailyDigestPlatformTask(
						List.of(DEFAULT_USER_CATEGORY, username),
						List.of(DEFAULT_USER_CATEGORY),
						"Default user " + username + " should be removed",
						dailyDigestService.makeLink("Please remove the default user", authPaths.admin.viewUsers)))
				.map(List::of)
				.orElseGet(List::of);
	}

	private Optional<String> findDefaultUsername(){
		Long defaultAdminId = DatarouterUserCreationService.ADMIN_ID;
		return datarouterUserDao.find(new DatarouterUserKey(defaultAdminId))
				.map(DatarouterUser::getUsername);
	}

}
