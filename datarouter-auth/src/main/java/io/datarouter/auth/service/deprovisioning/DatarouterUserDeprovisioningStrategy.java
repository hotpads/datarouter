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
package io.datarouter.auth.service.deprovisioning;

import java.util.List;
import java.util.Set;

import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.session.UserSessionService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserDeprovisioningStrategy implements UserDeprovisioningStrategy{

	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DatarouterUserHistoryService datarouterUserHistoryService;
	@Inject
	private RoleManager roleManager;
	@Inject
	private UserSessionService userSessionService;

	@Override
	public void deprovisionUsers(List<String> usernames){
		userSessionService.deleteUserSessions(usernames);
		List<DatarouterUser> deprovisionedUsers = Scanner.of(usernames)
				.map(DatarouterUserByUsernameLookup::new)
				.listTo(datarouterUserDao::getMultiByUsername)
				.stream()
				.map(user -> {
					user.setRoles(List.of());
					user.setEnabled(false);
					return user;
				})
				.toList();
		datarouterUserDao.putMulti(deprovisionedUsers);
		datarouterUserHistoryService.recordDeprovisions(deprovisionedUsers);
	}

	@Override
	public void restoreUser(String username){
		restoreAndGetUser(username);
	}

	public DatarouterUser restoreAndGetUser(String username){
		Set<Role> defaultRoles = roleManager.getDefaultRoles();
		DatarouterUser user = datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(username));
		user.setEnabled(true);
		user.setRoles(defaultRoles);
		datarouterUserDao.put(user);
		datarouterUserHistoryService.recordRestore(user);
		return user;
	}

}
