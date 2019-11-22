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
package io.datarouter.web.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.util.BooleanTool;
import io.datarouter.web.user.authenticate.PermissionRequestAdditionalEmailsSupplier;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserHistory;
import io.datarouter.web.user.databean.DatarouterUserHistory.DatarouterUserChangeType;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.util.PasswordTool;

@Singleton
public class DatarouterUserEditService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserEditService.class);

	@Inject
	private DatarouterAdministratorEmailService adminEmailService;
	@Inject
	private DatarouterUserHistoryService userHistoryService;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private PermissionRequestAdditionalEmailsSupplier permissionRequestAdditionalEmails;
	@Inject
	private DatarouterService datarouterService;

	public void editUser(DatarouterUser user, DatarouterUser editor, String[] requestedRoles, Boolean enabled,
			String signinUrl, List<String> requestedAccounts, List<String> currentAccounts){
		DatarouterUserHistory history = new DatarouterUserHistory(user.getId(), new Date(), editor.getId(),
				DatarouterUserChangeType.EDIT, null);

		List<String> changes = new ArrayList<>();

		Set<Role> allowedRoles = datarouterUserService.getAllowedUserRoles(editor, requestedRoles);
		Set<Role> currentRoles = new HashSet<>(user.getRoles());
		if(!allowedRoles.equals(currentRoles)){
			changes.add(change("roles", currentRoles, allowedRoles));
			user.setRoles(allowedRoles);
		}
		if(!BooleanTool.nullSafeSame(enabled, user.getEnabled())){
			changes.add(change("enabled", user.getEnabled(), enabled));
			user.setEnabled(enabled);
		}
		if(requestedAccounts.size() != 0){
			String current = String.join(",", currentAccounts);
			String accountsAdded = String.join(",", requestedAccounts);
			changes.add(change("accounts", current, accountsAdded));
		}

		if(changes.size() > 0){
			history.setChanges(String.join(", ", changes));
			userHistoryService.recordRoleEdit(user, history, signinUrl);
		}else{
			logger.warn("User {} submitted edit request for user {}, but no changes were made.", editor.toString(), user
					.toString());
		}
	}

	public void changePassword(DatarouterUser user, DatarouterUser editor, String newPassword, String signinUrl){
		DatarouterUserHistory history = new DatarouterUserHistory(user.getId(), new Date(), editor.getId(),
				DatarouterUserChangeType.RESET, null);
		updateUserPassword(user, newPassword);
		history.setChanges("password");
		userHistoryService.recordPasswordChange(user, history, signinUrl);
	}

	private void updateUserPassword(DatarouterUser user, String password){
		String passwordSalt = PasswordTool.generateSalt();
		String passwordDigest = PasswordTool.digest(passwordSalt, password);
		user.setPasswordSalt(passwordSalt);
		user.setPasswordDigest(passwordDigest);
	}

	private static String change(String name, Object before, Object after){
		return name + ": " + before + " => " + after;
	}

	public String getUserEditEmailRecipients(DatarouterUser... users){
		Set<String> recipients = Arrays.stream(users)
				.map(DatarouterUser::getUsername)
				.collect(Collectors.toSet());
		permissionRequestAdditionalEmails.get().forEach(recipients::add);
		adminEmailService.getAdministratorEmailAddresses().forEach(recipients::add);
		return String.join(",", recipients);
	}

	public String getPermissionRequestEmailSubject(DatarouterUser user){
		return user.getUsername() + " permissions request for " + datarouterService.getName();
	}

}
