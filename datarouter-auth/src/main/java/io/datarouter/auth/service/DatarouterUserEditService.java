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
package io.datarouter.auth.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.model.dto.RoleApprovalRequirementStatus;
import io.datarouter.auth.model.dto.UserRoleMetadata;
import io.datarouter.auth.model.dto.UserRoleUpdateDto;
import io.datarouter.auth.model.enums.RoleUpdateType;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.roleapprovals.DatarouterUserRoleApproval;
import io.datarouter.auth.storage.roleapprovals.DatarouterUserRoleApprovalDao;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMap;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.util.BooleanTool;
import io.datarouter.web.user.DatarouterSessionDao;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.role.Role;
import io.datarouter.web.user.role.RoleApprovalType;
import io.datarouter.web.user.role.RoleManager;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.util.PasswordTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserEditService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserEditService.class);

	@Inject
	private BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao;
	@Inject
	private DatarouterUserHistoryService userHistoryService;
	@Inject
	private DatarouterSessionDao datarouterSessionDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private ServiceName serviceName;
	@Inject
	private EnvironmentName environmentName;
	@Inject
	private RoleManager roleManager;
	@Inject
	private DatarouterUserRoleApprovalDao userRoleApprovalDao;
	@Inject
	private DatarouterAccountUserService datarouterAccountUserService;

	// returns error message on partial success
	public Optional<String> editRoles(
			DatarouterUser editor,
			DatarouterUser user,
			List<UserRoleUpdateDto> updates,
			String signinUrl){
		Optional<UserRoleUpdateDto> datarouterAdminUpdate = updates.stream()
				.filter(update -> DatarouterUserRole.DATAROUTER_ADMIN.getPersistentString().equals(update.roleName()))
				.findFirst();
		if(datarouterAdminUpdate.isPresent()
			&& RoleUpdateType.REVOKE.equals(datarouterAdminUpdate.get().updateType())
			&& !user.equals(editor)){
			throw new RuntimeException("cannot revoke another's datarouterAdmin role");
		}
		Map<Role, UserRoleMetadata> userRoleMetadataByRole =
				Scanner.of(datarouterUserService.getRoleMetadataForUser(editor, user))
				.toMap(UserRoleMetadata::role);

		List<String> changes = new ArrayList<>();
		List<UserRoleUpdateDto> failedUpdates = new ArrayList<>();
		for(UserRoleUpdateDto updateDto : updates){
			Optional<Role> roleToUpdateOptional = roleManager.findRoleFromPersistentString(updateDto.roleName());

			if(roleToUpdateOptional.isEmpty()){
				logger.warn("Role update attempted for {} by {} for unknown role={}",
						user.getUsername(),
						editor.getUsername(),
						updateDto.roleName());
				continue;
			}
			Role roleToUpdate = roleToUpdateOptional.get();
			if(!userRoleMetadataByRole.containsKey(roleToUpdate)){  // TODO delete? IN-10435
				logger.warn("Role update attempted for {} by {} for non-conferrable role={}",
						user.getUsername(),
						editor.getUsername(),
						updateDto.roleName());
				continue;
			}
			UserRoleMetadata userRoleMetadata = userRoleMetadataByRole.get(roleToUpdate);
			Optional<UserRoleMetadata> optionalUpdatedRoleMetadata = attemptRoleUpdate(
					editor,
					user,
					userRoleMetadata,
					updateDto.updateType());
			if(optionalUpdatedRoleMetadata.isPresent()){
				var updatedRoleMetadata = optionalUpdatedRoleMetadata.get();
				userRoleMetadataByRole.put(roleToUpdate, updatedRoleMetadata);
				changes.add(updatedRoleMetadata.getChangeString(userRoleMetadata));
			}else{
				failedUpdates.add(updateDto);
			}
		}
		user.setRoles(userRoleMetadataByRole.entrySet()
				.stream()
				.filter(entry -> entry.getValue().privilegesGranted())
				.map(Entry::getKey)
				.collect(Collectors.toSet()));

		if(!changes.isEmpty()){
			userHistoryService.putAndRecordEdit(user, editor, getRolesChangesString(changes), signinUrl);

			datarouterSessionDao.scan()
					.include(session -> session.getUserToken().equals(user.getUserToken()))
					.each(session -> session.setRoles(user.getRoles()))
					.flush(datarouterSessionDao::putMulti);
		}
		return failedUpdates.isEmpty() ? Optional.empty()
				: Optional.of("Failed to update some roles: " + failedUpdates);
	}

	private String getRolesChangesString(List<String> changes){
		String changesStr = "roles updated: [";
		if(changes.size() == 1){
			changesStr += changes.get(0) + "]";
		}else{
			changesStr += "\n\t" + String.join(",\n\t", changes) + "\n]";
		}
		return changesStr;
	}

	private Optional<UserRoleMetadata> attemptRoleUpdate(
			DatarouterUser editor,
			DatarouterUser user,
			UserRoleMetadata userRoleMetadata,
			RoleUpdateType updateType){
		return switch(updateType){
			case APPROVE -> attemptRoleApproval(editor, user, userRoleMetadata);
			case UNAPPROVE -> attemptRoleUnapproval(editor, user, userRoleMetadata);
			case REVOKE -> attemptRoleRevocation(editor, user, userRoleMetadata);
		};
	}

	private Optional<UserRoleMetadata> attemptRoleApproval(
			DatarouterUser editor,
			DatarouterUser user,
			UserRoleMetadata userRoleMetadata){
		if(userRoleMetadata.editorPrioritizedApprovalType().isEmpty()){
			logger.warn("Attempt to approve role={} for user={} which editor={} cannot approve",
					userRoleMetadata.role(),
					user.getUsername(),
					editor.getUsername());
			return Optional.empty();
		}
		RoleApprovalType editorPrioritizedApprovalType = userRoleMetadata.editorPrioritizedApprovalType().get();
		if(userRoleMetadata.privilegesGranted()){
			logger.warn("Attempt to approve already granted role={} for user={} by editor={}",
					userRoleMetadata.role(),
					user.getUsername(),
					editor.getUsername());
			return Optional.empty();
		}
		boolean editorAlreadyApproved = userRoleMetadata.requirementStatusByApprovalType().values()
				.stream()
				.anyMatch(requirementStatus -> requirementStatus.currentApprovers().contains(editor.getUsername()));
		if(editorAlreadyApproved){
			logger.warn("Attempt to doubly approve role={} for user={} by editor={}",
					userRoleMetadata.role(),
					user.getUsername(),
					editor.getUsername());
			return Optional.empty();
		}
		// deep copy the requirement status
		Map<RoleApprovalType,RoleApprovalRequirementStatus> requirementStatusByApprovalTypeSnapshot =
				Scanner.of(userRoleMetadata.requirementStatusByApprovalType().entrySet())
						.toMap(Entry::getKey,
								entry -> new RoleApprovalRequirementStatus(
										entry.getValue().requiredApprovals(),
										new HashSet<>(entry.getValue().currentApprovers())));
		requirementStatusByApprovalTypeSnapshot.get(editorPrioritizedApprovalType)
				.currentApprovers()
				.add(editor.getUsername());
		boolean areAllRequirementsMet = requirementStatusByApprovalTypeSnapshot.values()
				.stream()
				.allMatch(requirement -> requirement.requiredApprovals() == requirement.currentApprovers().size());
		UserRoleMetadata updatedRoleMetadata = new UserRoleMetadata(
				userRoleMetadata.role(),
				areAllRequirementsMet,
				requirementStatusByApprovalTypeSnapshot,
				userRoleMetadata.editorPrioritizedApprovalType(),
				null);
		var userRoleApproval = new DatarouterUserRoleApproval(
				user.getUsername(),
				userRoleMetadata.role().persistentString,
				editor.getUsername(),
				Instant.now(),
				editorPrioritizedApprovalType.persistentString(),
				null /* Filled in later */);
		userRoleApprovalDao.put(userRoleApproval);
		if(areAllRequirementsMet){
			userRoleApprovalDao.setAllRequirementsMetAtForUserRole(user, userRoleMetadata.role().persistentString);
		}
		return Optional.of(updatedRoleMetadata);
	}

	private Optional<UserRoleMetadata> attemptRoleUnapproval(
			DatarouterUser editor,
			DatarouterUser user,
			UserRoleMetadata userRoleMetadata){
		if(userRoleMetadata.privilegesGranted()){
			logger.warn("Attempt to unapprove already granted role={} for {} by {}",
					userRoleMetadata.role(),
					user.getUsername(),
					editor.getUsername());
			return Optional.empty();
		}
		AtomicBoolean unapproved = new AtomicBoolean(false);
		Map<RoleApprovalType,RoleApprovalRequirementStatus> updatedRequirementStatusByApprovalType =
				userRoleMetadata.requirementStatusByApprovalType()
						.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, entry -> {
							Set<String> updatedCurrentApprovers =
									entry.getValue().currentApprovers()
											.stream()
											.filter(approver -> !editor.getUsername().equals(approver))
											.collect(Collectors.toSet());
							if(!updatedCurrentApprovers.equals(entry.getValue().currentApprovers())){
								unapproved.set(true);
							}
							return new RoleApprovalRequirementStatus(
									entry.getValue().requiredApprovals(),
									updatedCurrentApprovers);
						}));
		if(!unapproved.get()){
			logger.warn("Attempt to unapprove role={} for user={} by editor={} where no approval had previously "
							+ "been given",
					userRoleMetadata.role(),
					user.getUsername(),
					editor.getUsername());
			return Optional.empty();
		}
		userRoleApprovalDao.deleteOutstandingApprovals(user, userRoleMetadata.role().persistentString, editor);
		return Optional.of(new UserRoleMetadata(
				userRoleMetadata.role(),
				false, // can't be true after revoking an approval.
				updatedRequirementStatusByApprovalType,
				userRoleMetadata.editorPrioritizedApprovalType(),
				null));
	}

	private Optional<UserRoleMetadata> attemptRoleRevocation(
			DatarouterUser editor,
			DatarouterUser user,
			UserRoleMetadata userRoleMetadata){
		if(!userRoleMetadata.privilegesGranted()){
			logger.warn("Attempt by editor={} to revoke role={} which user={} did not have",
					editor.getUsername(),
					userRoleMetadata.role(),
					user.getUsername());
			return Optional.empty();
		}
		return Optional.of(new UserRoleMetadata(
				userRoleMetadata.role(),
				false,
				new HashMap<>(),
				userRoleMetadata.editorPrioritizedApprovalType(),
				null));
	}

	public void editUser(
			DatarouterUser user,
			DatarouterUser editor,
			Boolean enabled,
			String signinUrl,
			Set<DatarouterAccountKey> requestedAccounts,
			Optional<ZoneId> optionalZoneId,
			Optional<String> description){
		List<String> changes = new ArrayList<>();

		Set<Role> currentRoles = new HashSet<>(user.getRoles());
		boolean isUserDatarouterAdmin = currentRoles.contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole());

		boolean shouldDeleteSessions = false;
		if(enabled != null){//TODO DATAROUTER-2751 remove/update old user edit code
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
			String changesStr = description.orElse("") + colon + String.join(", ", changes);
			userHistoryService.putAndRecordEdit(user, editor, changesStr, signinUrl);
			if(shouldDeleteSessions){
				datarouterSessionDao.scan()
						.include(session -> session.getUserToken().equals(user.getUserToken()))
						.map(DatarouterSession::getKey)
						.flush(datarouterSessionDao::deleteMulti);
			}
		}else{
			logger.warn("User {} submitted edit request for user {}, but no changes were made.", editor, user);
		}
	}

	public Map<String,Boolean> editAccounts(
			DatarouterUser editor,
			DatarouterUser user,
			Map<String,Boolean> updates,
			String signinUrl){
		Set<DatarouterAccountKey> requestedAccounts = Scanner.of(updates.entrySet())
				.include(Entry::getValue)
				.map(Entry::getKey)
				.map(DatarouterAccountKey::new)
				.collect(HashSet::new);
		String changes = handleAccountChanges(user, requestedAccounts).orElseThrow();

		userHistoryService.putAndRecordEdit(user, editor, changes, signinUrl);
		return datarouterAccountUserService.getAccountProvisioningStatusForUser(user);
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
			Set<String> original = Scanner.of(currentAccounts)
					.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
					.map(DatarouterAccountKey::getAccountName)
					.collect(HashSet::new);
			Set<String> current = Scanner.of(requestedAccounts)
					.map(DatarouterAccountKey::getAccountName)
					.collect(HashSet::new);
			return Optional.of(changeList("account", original, current));
		}
		return Optional.empty();
	}

	public void updateTimeZone(DatarouterUser editor, DatarouterUser user, String timeZoneId, String signInUrl){
		Optional<ZoneId> currentZoneId = user.getZoneId();
		ZoneId newZoneId = ZoneId.of(timeZoneId);
		boolean sameZoneId = currentZoneId.isPresent() && currentZoneId.get().equals(newZoneId);
		if(sameZoneId){
			logger.warn("Attempt to update time zone with same time zone");
			return;
		}
		user.setZoneId(newZoneId);
		String changeStr = change(
				"timezone",
				currentZoneId.map(ZoneId::getId).orElse(""),
				newZoneId.getId());
		userHistoryService.putAndRecordEdit(user, editor, changeStr, signInUrl);
	}

	public void changePassword(DatarouterUser user, DatarouterUser editor, String newPassword, String signinUrl){
		updateUserPassword(user, newPassword);
		userHistoryService.putAndRecordPasswordChange(user, editor, signinUrl);
	}

	public static String changeList(String name, Set<String> before, Set<String> after){
		List<String> added = Scanner.of(after)
				.exclude(before::contains)
				.sort()
				.list();
		List<String> removed = Scanner.of(before)
				.exclude(after::contains)
				.sort()
				.list();

		String output = generateChangeOutput(added, name, "added") + generateChangeOutput(removed, name, "removed");
		return output.length() == 0 ? "No changes" : output.trim();
	}

	private static String generateChangeOutput(List<String> updates, String name, String description){
		if(updates.isEmpty()){
			return "";
		}
		return name + (updates.size() > 1 ? "s" : "") + " " + description + ": [" + String.join(", ", updates) + "] ";
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

	// similar to standard datarouter-email subject, but required for proper email threading
	public String getPermissionRequestEmailSubject(DatarouterUser user){
		return String.format("Permission Request %s - %s - %s",
				user.getUsername(),
				environmentName.get(),
				serviceName.get());
	}

}
