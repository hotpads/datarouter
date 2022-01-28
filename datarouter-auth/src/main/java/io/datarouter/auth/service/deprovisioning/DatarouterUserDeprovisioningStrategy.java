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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.service.DatarouterUserHistoryService;
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
public class DatarouterUserDeprovisioningStrategy implements UserDeprovisioningStrategy{

	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DatarouterUserHistoryService datarouterUserHistoryService;
	@Inject
	private DeprovisionedUserDao deprovisionedUserDao;
	@Inject
	private RoleManager roleManager;
	@Inject
	private UserSessionService userSessionService;

	@Override
	public List<String> flagUsers(List<String> usernames, Optional<String> editorUsername){
		return doFlagOrDeprovision(usernames, false, editorUsername);
	}

	@Override
	public List<String> deprovisionUsers(List<String> usernames, Optional<String> editorUsername){
		return doFlagOrDeprovision(usernames, true, editorUsername);
	}

	private List<String> doFlagOrDeprovision(List<String> usernames, boolean shouldDeprovision,
			Optional<String> editorUsername){
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
			Optional<DatarouterUser> editor = editorUsername
					.map(DatarouterUserByUsernameLookup::new)
					.map(datarouterUserDao::getByUsername);
			datarouterUserDao.putMulti(users);
			datarouterUserHistoryService.recordDeprovisions(users, editor);
		}
		return deprovisionedUsernames;
	}

	@Override
	public List<String> restoreUsers(List<String> usernames, Optional<String> editorUsername){
		Set<Role> validRoles = roleManager.getAllRoles();
		var deprovisionedRolesByUsername = Scanner.of(usernames)
				.map(DeprovisionedUserKey::new)
				.listTo(deprovisionedUserDao::scanWithPrefixes)
				.include(user -> user.getStatus() == UserDeprovisioningStatus.DEPROVISIONED)
				.toMap(DeprovisionedUser::getUsername, DeprovisionedUser::getRoles);
		var datarouterUserByUsername = Scanner.of(usernames)
				.map(DatarouterUserByUsernameLookup::new)
				.listTo(datarouterUserDao::getMultiByUsername)
				.stream()
				.collect(Collectors.toMap(DatarouterUser::getUsername, Function.identity()));

		Scanner<DatarouterUser> normalUsersToRestore = Scanner.of(usernames)
				.include(deprovisionedRolesByUsername::containsKey)//record of roles
				.include(datarouterUserByUsername::containsKey)
				.map(username -> {
					var rolesToRestore = deprovisionedRolesByUsername.get(username).stream()
							.filter(validRoles::contains)
							.collect(Collectors.toList());
					var datarouterUser = datarouterUserByUsername.get(username);
					datarouterUser.setRoles(rolesToRestore);
					datarouterUser.setEnabled(true);
					return datarouterUser;
				});
		Scanner<DatarouterUser> unrecordedUsersToRestore = Scanner.of(usernames)
				.exclude(deprovisionedRolesByUsername::containsKey)//no record of roles
				.include(datarouterUserByUsername::containsKey)
				.map(username -> {
					var datarouterUser = datarouterUserByUsername.get(username);
					// make sure the set is mutable
					var rolesToRestore = new HashSet<>(roleManager.getRolesForDefaultGroup());
					rolesToRestore.addAll(datarouterUser.getRoles());
					datarouterUser.setRoles(rolesToRestore);
					datarouterUser.setEnabled(true);
					return datarouterUser;
				});

		return normalUsersToRestore.append(unrecordedUsersToRestore)
				.flush(datarouterUserDao::putMulti)
				.flush(buildRecordRestoreConsumer(editorUsername))
				.map(DatarouterUser::getUsername)
				.flush(deprovisionedUserDao::deleteMultiUsernames)
				.list();
	}

	private Consumer<List<DatarouterUser>> buildRecordRestoreConsumer(Optional<String> editorUsername){
		Optional<DatarouterUser> editor = editorUsername
				.map(DatarouterUserByUsernameLookup::new)
				.map(datarouterUserDao::getByUsername);
		return users -> datarouterUserHistoryService.recordRestores(users, editor);
	}

}
