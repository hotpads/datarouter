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

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMap;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory.DatarouterUserChangeType;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.BooleanTool;
import io.datarouter.web.user.DatarouterSessionDao;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.util.PasswordTool;

@Singleton
public class DatarouterUserEditService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserEditService.class);

	@Inject
	private DatarouterAdministratorEmailService adminEmailService;
	@Inject
	private BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao;
	@Inject
	private DatarouterUserHistoryService userHistoryService;
	@Inject
	private DatarouterSessionDao datarouterSessionDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	public void editUser(
			DatarouterUser user,
			DatarouterUser editor,
			Set<Role> requestedRoles,
			Boolean enabled,
			String signinUrl,
			Set<DatarouterAccountKey> requestedAccounts,
			Optional<ZoneId> optionalZoneId,
			Optional<String> description){
		var history = new DatarouterUserHistory(
				user.getId(),
				Instant.now(),
				editor.getId(),
				DatarouterUserChangeType.EDIT,
				null);
		List<String> changes = new ArrayList<>();

		Set<Role> currentRoles = new HashSet<>(user.getRoles());
		boolean isUserDatarouterAdmin = currentRoles.contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole());
		if(isUserDatarouterAdmin
				&& !requestedRoles.contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole())
				&& !user.equals(editor)){
			throw new RuntimeException("cannot disable datarouterAdmin user");
		}
		Set<Role> allowedRoles = datarouterUserService.getAllowedUserRoles(editor, requestedRoles);
		boolean shouldUpdateSessions = false;
		if(!allowedRoles.equals(currentRoles)){
			changes.add(change("roles", currentRoles, allowedRoles));
			user.setRoles(allowedRoles);
			shouldUpdateSessions = true;
		}

		boolean shouldDeleteSessions = false;
		if(enabled != null){//TODO DATAROTUER-2751 remove/update old user edit code
			if(!BooleanTool.nullSafeSame(enabled, user.getEnabled())){
				if(isUserDatarouterAdmin){
					throw new RuntimeException("cannot disable datarouterAdmin user");
				}
				changes.add(change("enabled", user.getEnabled(), enabled));
				user.setEnabled(enabled);
				shouldDeleteSessions = true;
			}
		}

		handleAccountChanges(user, requestedAccounts).ifPresent(changes::add);
		optionalZoneId.ifPresent(zoneId -> {
			Optional<ZoneId> currentZoneId = user.getZoneId();
			boolean sameZoneId = currentZoneId.isPresent() && currentZoneId.get().equals(zoneId);
			if(!sameZoneId){
				user.setZoneId(zoneId);
				changes.add(change("timezone", currentZoneId.map(ZoneId::getId).orElse(""), zoneId.getId()));
			}
		});

		if(changes.size() > 0 || description.isPresent()){
			String colon = changes.size() > 0 && description.isPresent() ? ": " : "";
			history.setChanges(description.orElse("") + colon + String.join(", ", changes));
			userHistoryService.putAndRecordRoleEdit(user, history, signinUrl);
			if(shouldUpdateSessions || shouldDeleteSessions){
				Scanner<DatarouterSession> sessions = datarouterSessionDao.scan()
						.include(session -> session.getUserToken().equals(user.getUserToken()));
				if(shouldDeleteSessions){
					sessions.map(DatarouterSession::getKey)
							.flush(datarouterSessionDao::deleteMulti);
				}else{
					sessions.each(session -> session.setRoles(user.getRoles()))
							.flush(datarouterSessionDao::putMulti);
				}
			}
			var dto = new DatarouterChangelogDtoBuilder(
					"Admin Edit User",
					"User Edited - " + user.getUsername(),
					"Edit",
					editor.getUsername())
					.withComment(String.join(", ", changes))
					.build();
			changelogRecorder.record(dto);
		}else{
			logger.warn("User {} submitted edit request for user {}, but no changes were made.", editor.toString(),
					user.toString());
		}
	}

	private Optional<String> handleAccountChanges(DatarouterUser user, Set<DatarouterAccountKey> requestedAccounts){
		Set<DatarouterUserAccountMapKey> currentAccounts = datarouterUserAccountMapDao.scanKeysWithPrefix(
				new DatarouterUserAccountMapKey(user.getId(), null))
				.collect(HashSet::new);
		Set<DatarouterUserAccountMapKey> accountsToDelete = currentAccounts.stream()
				.filter(currentAccountKey -> !requestedAccounts.contains(currentAccountKey
						.getDatarouterAccountKey()))
				.collect(Collectors.toSet());
		Set<DatarouterUserAccountMap> accountsToAdd = requestedAccounts.stream()
				.map(accountKey -> new DatarouterUserAccountMap(user.getId(), accountKey.getAccountName()))
				.filter(requestedAccount -> !currentAccounts.contains(requestedAccount.getKey()))
				.collect(Collectors.toSet());
		if(!accountsToDelete.isEmpty() || !accountsToAdd.isEmpty()){
			if(!accountsToDelete.isEmpty()){
				datarouterUserAccountMapDao.deleteMulti(accountsToDelete);
			}
			if(!accountsToAdd.isEmpty()){
				datarouterUserAccountMapDao.putMulti(accountsToAdd);
			}
			String original = currentAccounts.stream()
					.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
					.map(DatarouterAccountKey::getAccountName)
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.collect(Collectors.joining(","));
			String current = requestedAccounts.stream()
					.map(DatarouterAccountKey::getAccountName)
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.collect(Collectors.joining(","));
			return Optional.of(change("accounts", original, current));
		}
		return Optional.empty();
	}

	public void changePassword(DatarouterUser user, DatarouterUser editor, String newPassword, String signinUrl){
		var history = new DatarouterUserHistory(
				user.getId(),
				Instant.now(),
				editor.getId(),
				DatarouterUserChangeType.RESET,
				null);
		updateUserPassword(user, newPassword);
		history.setChanges("password");
		userHistoryService.putAndRecordPasswordChange(user, history, signinUrl);
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
		Set<String> recipients = Scanner.of(users)
				.map(DatarouterUser::getUsername)
				.collect(HashSet::new);
		if(serverTypeDetector.mightBeProduction()){
			permissionRequestEmailType.tos.forEach(recipients::add);
			adminEmailService.getAdditionalAdministratorOnly().forEach(recipients::add);
		}
		if(serverTypeDetector.mightBeDevelopment()){
			recipients.add(datarouterProperties.getAdministratorEmail());
		}
		return String.join(",", recipients);
	}

	// similar to standard datarouter-email subject, but required for proper email threading
	public String getPermissionRequestEmailSubject(DatarouterUser user){
		return String.format("Permission Request %s - %s - %s",
				user.getUsername(),
				datarouterProperties.getEnvironment(),
				datarouterService.getServiceName());
	}

}
