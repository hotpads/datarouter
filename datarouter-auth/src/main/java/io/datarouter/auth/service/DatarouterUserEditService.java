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
import java.util.Collection;
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
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleApprovalType;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.roleapprovals.DatarouterUserRoleApproval;
import io.datarouter.auth.storage.user.roleapprovals.DatarouterUserRoleApprovalDao;
import io.datarouter.auth.storage.user.session.DatarouterSessionDao;
import io.datarouter.auth.storage.user.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.user.useraccountmap.DatarouterUserAccountMap;
import io.datarouter.auth.storage.user.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.auth.util.PasswordTool;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserEditService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserEditService.class);

	@Inject
	private BaseDatarouterUserAccountMapDao userAccountMapDao;
	@Inject
	private DatarouterUserHistoryService userHistoryService;
	@Inject
	private DatarouterSessionDao sessionDao;
	@Inject
	private DatarouterUserService userService;
	@Inject
	private RoleManager roleManager;
	@Inject
	private DatarouterUserRoleApprovalDao userRoleApprovalDao;
	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private PermissionRequestService permissionRequestService;

	public record EditRolesResult(
			List<UserRoleMetadata> updatedRoles,
			List<UserRoleUpdateDto> failedUpdates){
	}

	public EditRolesResult editRoles(
			DatarouterUser editor,
			DatarouterUser user,
			List<UserRoleUpdateDto> updates,
			String signinUrl){
		Map<Role,UserRoleMetadata> userRoleMetadataByRole =
				Scanner.of(userService.getRoleMetadataForUser(editor, user))
				.toMap(UserRoleMetadata::role);
		return editRolesHelper(editor, user, updates, signinUrl, userRoleMetadataByRole);
	}

	public DatarouterUser requestPermissions(
			DatarouterUser user,
			Set<Role> requestedRoles,
			String reason,
			String signinUrl,
			Optional<String> deniedUrl,
			Optional<String> allowedRoles){
		Set<Role> acquiredRoles = attemptPermissionRequestSelfServe(user, requestedRoles, signinUrl)
				.acquiredRoles();
		Set<Role> remainingRequestedRoles = Scanner.of(requestedRoles)
				.exclude(acquiredRoles::contains)
				.collect(HashSet::new);

		if(!remainingRequestedRoles.isEmpty()){
			permissionRequestService.createPermissionRequest(
					user,
					remainingRequestedRoles,
					reason,
					deniedUrl,
					allowedRoles);
		}
		return user;
	}

	public record AcquiredRoles(Set<Role> acquiredRoles){}

	public AcquiredRoles attemptPermissionRequestSelfServe(
			DatarouterUser user,
			Set<Role> requestedRoles,
			String signinUrl){
		Map<Role,UserRoleMetadata> userRoleMetadataByRole =
				Scanner.of(userService.getRoleMetadataForUser(user, user))
						.toMap(UserRoleMetadata::role);
		List<UserRoleUpdateDto> updates = Scanner.of(requestedRoles)
				.include(role -> userRoleMetadataByRole.get(role).editorPrioritizedApprovalType().isPresent())
				.map(role -> new UserRoleUpdateDto(role.persistentString(), RoleUpdateType.APPROVE))
				.list();
		EditRolesResult result = editRolesHelper(user, user, updates, signinUrl, userRoleMetadataByRole);
		return new AcquiredRoles(Scanner.of(result.updatedRoles())
				.include(UserRoleMetadata::hasRole)
				.map(UserRoleMetadata::role)
				.collect(HashSet::new));
	}

	public void resetRoles(DatarouterUser user){
		Collection<Role> currentRoles = user.getRolesIgnoreSaml();
		logger.info("Resetting roles for user={} from currentRoles={} to defaultRoles={}",
				currentRoles,
				user.getRolesIgnoreSaml(),
				roleManager.getDefaultRoles());
		user.setRoles(roleManager.getDefaultRoles());
		userDao.put(user);
		userHistoryService.recordRoleReset(user,
				"Reset roles from %s to default roles: %s"
				.formatted(currentRoles, roleManager.getDefaultRoles()));
	}

	private EditRolesResult editRolesHelper(
			DatarouterUser editor,
			DatarouterUser user,
			List<UserRoleUpdateDto> updates,
			String signinUrl,
			Map<Role,UserRoleMetadata> userRoleMetadataByRole){
		List<String> changes = new ArrayList<>();
		List<UserRoleUpdateDto> failedUpdates = new ArrayList<>();
		List<UserRoleMetadata> updatedRoles = new ArrayList<>();
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
			UserRoleMetadata userRoleMetadata = userRoleMetadataByRole.get(roleToUpdate);
			Optional<UserRoleMetadata> optionalUpdatedRoleMetadata = attemptRoleUpdate(
					editor,
					user,
					userRoleMetadata,
					updateDto.updateType());
			if(optionalUpdatedRoleMetadata.isPresent()){
				var updatedRoleMetadata = optionalUpdatedRoleMetadata.get();
				userRoleMetadataByRole.put(roleToUpdate, updatedRoleMetadata);
				updatedRoles.add(updatedRoleMetadata);
				Optional<String> changeString = updatedRoleMetadata.getChangeString(userRoleMetadata);
				changes.add(changeString.orElseThrow(() -> new IllegalStateException(
						"Failed to generate change string for update: %s => %s"
								.formatted(user.getUsername(), updateDto))));
			}else{
				failedUpdates.add(updateDto);
			}
		}
		user.setRoles(userRoleMetadataByRole.entrySet()
				.stream()
				.filter(entry -> entry.getValue().hasRole())
				.map(Entry::getKey)
				.collect(Collectors.toSet()));

		if(!changes.isEmpty()){
			userHistoryService.putAndRecordPermissionChange(user, editor, getRolesChangesString(changes), signinUrl);

			sessionDao.scan()
					.include(session -> session.getUserToken().equals(user.getUserToken()))
					.each(session -> session.setRoles(userService.getUserRolesWithSamlGroups(user)))
					.flush(sessionDao::putMulti);
		}
		return new EditRolesResult(updatedRoles, failedUpdates);
	}

	private String getRolesChangesString(List<String> changes){
		String changesStr = "roles updated: [";
		if(changes.size() == 1){
			changesStr += changes.getFirst() + "]";
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
		if(userRoleMetadata.isDefaultRole()){
			throw new IllegalArgumentException("Attempt to %s default role=%s"
					.formatted(updateType.persistentString, userRoleMetadata.role().persistentString()));
		}
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
		if(userRoleMetadata.hasRole()){
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
				userRoleMetadata.isDefaultRole(),
				requirementStatusByApprovalTypeSnapshot,
				userRoleMetadata.editorPrioritizedApprovalType(),
				null,
				null);
		var userRoleApproval = new DatarouterUserRoleApproval(
				user.getUsername(),
				userRoleMetadata.role().persistentString(),
				editor.getUsername(),
				Instant.now(),
				editorPrioritizedApprovalType.persistentString(),
				null /* Filled in later */);
		userRoleApprovalDao.put(userRoleApproval);
		if(areAllRequirementsMet){
			userRoleApprovalDao.setAllRequirementsMetAtForUserRole(user, userRoleMetadata.role().persistentString());
		}
		return Optional.of(updatedRoleMetadata);
	}

	private Optional<UserRoleMetadata> attemptRoleUnapproval(
			DatarouterUser editor,
			DatarouterUser user,
			UserRoleMetadata userRoleMetadata){
		if(userRoleMetadata.hasRole()){
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
		userRoleApprovalDao.deleteOutstandingApprovals(user, userRoleMetadata.role().persistentString(), editor);
		return Optional.of(new UserRoleMetadata(
				userRoleMetadata.role(),
				false, // can't be true after revoking an approval.
				userRoleMetadata.isDefaultRole(),
				updatedRequirementStatusByApprovalType,
				userRoleMetadata.editorPrioritizedApprovalType(),
				null,
				null));
	}

	private Optional<UserRoleMetadata> attemptRoleRevocation(
			DatarouterUser editor,
			DatarouterUser user,
			UserRoleMetadata userRoleMetadata){
		if(!userRoleMetadata.hasRole()){
			logger.warn("Attempt by editor={} to revoke role={} which user={} did not have",
					editor.getUsername(),
					userRoleMetadata.role(),
					user.getUsername());
			return Optional.empty();
		}
		return Optional.of(new UserRoleMetadata(
				userRoleMetadata.role(),
				false,
				userRoleMetadata.isDefaultRole(),
				new HashMap<>(),
				userRoleMetadata.editorPrioritizedApprovalType(),
				null,
				null));
	}

	public void editAccounts(
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

		userHistoryService.putAndRecordPermissionChange(user, editor, changes, signinUrl);
	}

	private Optional<String> handleAccountChanges(DatarouterUser user, Set<DatarouterAccountKey> requestedAccounts){
		Set<DatarouterUserAccountMapKey> currentAccounts = userAccountMapDao.scanKeysWithPrefix(
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
				userAccountMapDao.deleteMulti(accountsToDelete);
			}
			if(!accountsToAdd.isEmpty()){
				userAccountMapDao.putMulti(accountsToAdd);
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

	public void updateTimeZone(DatarouterUser editor, DatarouterUser user, String timeZoneId){
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
		userHistoryService.putAndRecordTimezoneUpdate(user, editor, changeStr);
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
		return output.isEmpty() ? "No changes" : output.trim();
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

}
