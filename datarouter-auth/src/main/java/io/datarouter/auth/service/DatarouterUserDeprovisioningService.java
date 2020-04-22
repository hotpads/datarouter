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
package io.datarouter.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUser;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUser.UserDeprovisioningStatus;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserDao;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserKey;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserSessionService;

@Singleton
public class DatarouterUserDeprovisioningService implements UserDeprovisioningService{

	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DeprovisionedUserDao deprovisionedUserDao;
	@Inject
	private RoleManager roleManager;
	@Inject
	private ShouldFlagUsersInsteadOfDeprovisioningSupplier shouldFlagUsersInsteadOfDeprovisioningSupplier;
	@Inject
	private UserSessionService userSessionService;

	@Override
	public List<String> flagUsersForDeprovisioning(List<String> usernames){
		return doFlagOrDeprovision(usernames, false);
	}

	@Override
	public List<String> deprovisionUsers(List<String> usernames){
		return doFlagOrDeprovision(usernames, true);
	}

	private List<String> doFlagOrDeprovision(List<String> usernames, boolean shouldDeprovision){
		userSessionService.deleteUserSessions(usernames);
		List<DeprovisionedUser> deprovisionedUsers = new ArrayList<>();
		List<DatarouterUser> users = Scanner.of(usernames)
				.map(DatarouterUserByUsernameLookup::new)
				.listTo(datarouterUserDao::getMultiByUsername);
		var deprovisionedUsernames = new ArrayList<String>();
		users.forEach(user -> {
			deprovisionedUsers.add(new DeprovisionedUser(user.getUsername(), user.getRoles(),
					shouldDeprovision ? UserDeprovisioningStatus.DEPROVISIONED : UserDeprovisioningStatus.FLAGGED));
			user.setRoles(List.of());
			user.setEnabled(false);
			deprovisionedUsernames.add(user.getUsername());
		});
		deprovisionedUserDao.putMulti(deprovisionedUsers);
		if(shouldDeprovision){
			datarouterUserDao.putMulti(users);
		}
		return deprovisionedUsernames;
	}

	@Override
	public List<String> restoreDeprovisionedUsers(List<String> usernames){
		Set<Role> validRoles = roleManager.getAllRoles();
		var deprovisionedRolesByUsername = Scanner.of(usernames)
				.map(DeprovisionedUserKey::new)
				.listTo(deprovisionedUserDao::scanWithPrefixes)
				.include(user -> user.getStatus() == UserDeprovisioningStatus.DEPROVISIONED)
				.collect(Collectors.toMap(DeprovisionedUser::getUsername, DeprovisionedUser::getRoles));
		var datarouterUserByUsername = Scanner.of(usernames)
				.map(DatarouterUserByUsernameLookup::new)
				.listTo(datarouterUserDao::getMultiByUsername)
				.stream()
				.collect(Collectors.toMap(DatarouterUser::getUsername, Function.identity()));
		return Scanner.of(usernames)
				.include(deprovisionedRolesByUsername::containsKey)
				.include(datarouterUserByUsername::containsKey)
				.map(username -> {
					var rolesToRestore = deprovisionedRolesByUsername.get(username).stream()
							.filter(validRoles::contains)
							.collect(Collectors.toList());
					var datarouterUser = datarouterUserByUsername.get(username);
					datarouterUser.setRoles(rolesToRestore);
					datarouterUser.setEnabled(true);
					return datarouterUser;
				})
				.flush(datarouterUserDao::putMulti)
				.map(DatarouterUser::getUsername)
				.flush(deprovisionedUserDao::deleteMultiUsernames)
				.list();
	}

	@Override
	public boolean shouldFlagUsersInsteadOfDeprovisioning(){
		return shouldFlagUsersInsteadOfDeprovisioningSupplier.get();
	}

}
