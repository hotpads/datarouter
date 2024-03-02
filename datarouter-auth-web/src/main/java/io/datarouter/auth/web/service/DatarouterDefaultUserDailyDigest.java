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

import static j2html.TagCreator.div;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserKey;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDefaultUserDailyDigest implements DailyDigest{

	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DailyDigestService dailyDigestService;
	@Inject
	private DatarouterAuthPaths authPaths;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	private Optional<String> findDefaultUsername(){
		Long defaultAdminId = DatarouterUserCreationService.ADMIN_ID;
		return datarouterUserDao.find(new DatarouterUserKey(defaultAdminId))
				.map(DatarouterUser::getUsername);
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		if(!serverTypeDetector.mightBeProduction()){
			return Optional.empty();
		}
		Optional<List<String>> username = findDefaultUsername()
				.map(List::of);
		if(username.isEmpty()){
			return Optional.empty();
		}
		var header = dailyDigestService.makeHeader("Please remove the default user", authPaths.admin.viewUsers);
		var table = buildEmailTable(username.get());
		return Optional.of(div(header, table));
	}

	@Override
	public String getTitle(){
		return "Users with default userIds";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.MEDIUM;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	public TableTag buildTable(List<String> rows){
		return new J2HtmlTable<String>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Users", row -> row)
				.build(rows);
	}

	private TableTag buildEmailTable(List<String> rows){
		return new J2HtmlEmailTable<String>()
				.withColumn("Users", row -> row)
				.build(rows);
	}
}
