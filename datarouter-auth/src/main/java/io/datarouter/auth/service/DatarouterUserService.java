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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.auth.model.dto.RoleApprovalRequirementStatus;
import io.datarouter.auth.model.dto.UserRoleMetadata;
import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleApprovalType;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.session.Session;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserKey;
import io.datarouter.auth.storage.user.datarouteruser.cache.DatarouterUserByIdCache;
import io.datarouter.auth.storage.user.datarouteruser.cache.DatarouterUserByUserTokenCache;
import io.datarouter.auth.storage.user.datarouteruser.cache.DatarouterUserByUsernameCache;
import io.datarouter.auth.storage.user.roleapprovals.DatarouterUserRoleApprovalDao;
import io.datarouter.auth.util.PasswordTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserService implements UserInfo{

	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private DatarouterUserByUsernameCache datarouterUserByUsernameCache;
	@Inject
	private DatarouterUserByUserTokenCache datarouterUserByUserTokenCache;
	@Inject
	private DatarouterUserByIdCache datarouterUserByIdCache;
	@Inject
	private DatarouterUserRoleApprovalDao roleApprovalDao;
	@Inject
	private RoleManager roleManager;

	@Override
	public Scanner<DatarouterUser> scanAllUsers(boolean enabledOnly, Set<Role> includedRoles){
		if(includedRoles.isEmpty()){
			return Scanner.empty();
		}
		return userDao.scan()
				.include(user -> !enabledOnly || user.getEnabled())
				.include(user -> user.getRolesIgnoreSaml().stream().anyMatch(includedRoles::contains));
	}

	@Override
	public Optional<DatarouterUser> findUserByUsername(String username, boolean allowCached){
		if(StringTool.isEmptyOrWhitespace(username)){
			return Optional.empty();
		}
		if(allowCached){
			return datarouterUserByUsernameCache.get(username);
		}
		return Optional.ofNullable(userDao.getByUsername(new DatarouterUserByUsernameLookup(username)));
	}

	public DatarouterUser getUserByUsername(String username, boolean allowCached){
		return findUserByUsername(username, allowCached)
				.orElseThrow(() -> new RuntimeException("User not found for username=" + username));
	}

	@Override
	public Optional<DatarouterUser> findUserByToken(String token, boolean allowCached){
		if(StringTool.isEmptyOrWhitespace(token)){
			return Optional.empty();
		}
		if(allowCached){
			return datarouterUserByUserTokenCache.get(token);
		}
		return userDao.find(new DatarouterUserByUserTokenLookup(token));
	}

	public DatarouterUser getUserByToken(String token, boolean allowCached){
		return findUserByToken(token, allowCached)
				.orElseThrow(() -> new RuntimeException("User not found for userToken=" + token));
	}

	@Override
	public Optional<DatarouterUser> findUserById(Long id, boolean allowCached){
		if(id == null){
			return Optional.empty();
		}
		if(allowCached){
			return datarouterUserByIdCache.get(id);
		}
		return userDao.find(new DatarouterUserKey(id));
	}

	public DatarouterUser getUserById(Long id, boolean allowCached){
		return findUserById(id, allowCached)
				.orElseThrow(() -> new RuntimeException("User not found for id=" + id));
	}

	public Set<Role> getUserRolesWithSamlGroups(DatarouterUser user){
		return getUserRolesWithSamlGroups(Optional.ofNullable(user));
	}

	public Set<Role> getUserRolesWithSamlGroups(Optional<DatarouterUser> optionalUser){
		return optionalUser.map(user -> user.getRolesWithSamlGroups(roleManager))
				.map(HashSet::new)
				.orElseGet(HashSet::new);
	}

	@Override
	public Set<Role> getRolesByUsername(String username, boolean allowCached){
		return getUserRolesWithSamlGroups(findUserByUsername(username, allowCached));
	}

	public DatarouterUser getAndValidateCurrentUser(Session session){
		DatarouterUser user = getUserBySession(session);
		if(user == null || !user.getEnabled()){
			throw new RuntimeException("Current user does not exist or is not enabled.");
		}
		return user;
	}

	public DatarouterUser getUserBySession(Session session){
		if(session == null || session.getUserId() == null){
			return null;
		}
		return userDao.get(new DatarouterUserKey(session.getUserId()));
	}

	public boolean canEditUserPassword(DatarouterUser editor, DatarouterUser user){
		return user.equals(editor)
				|| !isDatarouterAdmin(user)
				&& isDatarouterAdmin(editor)
				&& editor.getEnabled();
	}

	public boolean canEditUser(DatarouterUser editor, DatarouterUser user){
		return user.equals(editor)
				|| isDatarouterAdmin(editor)
				&& editor.getEnabled();
	}

	public boolean canHavePassword(DatarouterUser user){
		return user.getPasswordDigest() != null || isDatarouterAdmin(user);
	}

	public boolean isPasswordCorrect(DatarouterUser user, String rawPassword){
		if(user == null || rawPassword == null){
			return false;
		}
		String passwordDigest = PasswordTool.digest(user.getPasswordSalt(), rawPassword);
		return Objects.equals(user.getPasswordDigest(), passwordDigest);
	}

	public void assertUserDoesNotExist(Long id, String userToken, String username){
		Require.isEmpty(findUserById(id, false), "DatarouterUser already exists with id=" + id);
		Require.isEmpty(findUserByToken(userToken, false), "DatarouterUser already exists with userToken=" + userToken);
		Require.isEmpty(findUserByUsername(username, false), "DatarouterUser already exists with username=" + username);
	}

	public boolean isDatarouterAdmin(DatarouterUser user){
		return getUserRolesWithSamlGroups(user).contains(DatarouterUserRoleRegistry.DATAROUTER_ADMIN);
	}

	public Map<Role,Map<RoleApprovalType,Set<String>>> getCurrentRoleApprovals(DatarouterUser user){
		return Scanner.of(roleApprovalDao.getAllOutstandingApprovalsForUser(user))
				// role has been deleted
				.exclude(roleApproval ->
						roleManager.findRoleFromPersistentString(roleApproval.getKey().getRequestedRole()).isEmpty())
				// role approval type has been deleted
				.exclude(roleApproval ->
						roleManager.findRoleApprovalTypeFromPersistentString(roleApproval.getApprovalType()).isEmpty())
				.collect(Collectors.groupingBy(
						roleApproval -> roleManager
								.findRoleFromPersistentString(roleApproval.getKey().getRequestedRole()).get(),
						Collectors.groupingBy(
								roleApproval -> roleManager
										.findRoleApprovalTypeFromPersistentString(roleApproval.getApprovalType()).get(),
								Collectors.mapping(roleApproval -> roleApproval.getKey().getApproverUsername(),
										Collectors.toSet()))));
	}

	public List<UserRoleMetadata> getRoleMetadataForUser(DatarouterUser editor, DatarouterUser user){
		Set<Role> currentRoles = new HashSet<>(user.getRolesIgnoreSaml());
		Set<Role> availableRoles = roleManager.getAllRoles();
		Map<Role,Map<RoleApprovalType,Integer>> roleApprovalRequirements = roleManager.getAllRoleApprovalRequirements();
		Map<Role,Map<RoleApprovalType,Set<String>>> currentRoleApprovals = getCurrentRoleApprovals(user);
		Set<RoleApprovalType> relevantApprovalTypes = new HashSet<>();
		Scanner.of(roleApprovalRequirements.values())
				.map(Map::keySet)
				.forEach(relevantApprovalTypes::addAll);
		List<RoleApprovalType> prioritizedApprovalTypes =
				roleManager.getPrioritizedRoleApprovalTypes(editor, user, relevantApprovalTypes);
		Map<Role, List<String>> groupsHasByRole = roleManager.getGroupsByRole(user.getSamlGroups());

		return Scanner.of(availableRoles)
				.map(availableRole -> {
					Map<RoleApprovalType,Integer> requirementsOfRole = roleApprovalRequirements
							.getOrDefault(availableRole, new HashMap<>());
					Map<RoleApprovalType,Set<String>> currentApprovalsForRole = currentRoleApprovals
							.getOrDefault(availableRole, new HashMap<>());
					Map<RoleApprovalType,RoleApprovalRequirementStatus> requirementStatusByApprovalType =
							Scanner.of(requirementsOfRole.keySet())
									.toMap(
											Function.identity(),
											roleApprovalType -> new RoleApprovalRequirementStatus(
													requirementsOfRole.get(roleApprovalType),
													currentApprovalsForRole.getOrDefault(
															roleApprovalType, new HashSet<>())));
					boolean rolePrivilegesGranted = currentRoles.contains(availableRole);

					Optional<RoleApprovalType> currentEditorPreviouslyApprovedType = Optional.empty();
					for(RoleApprovalType approvalType : currentApprovalsForRole.keySet()){
						if(currentApprovalsForRole.get(approvalType).contains(editor.getUsername())){
							if(!requirementStatusByApprovalType.containsKey(approvalType)){
								roleApprovalDao.deleteOutstandingApprovalsOfApprovalTypeForRole(
										availableRole.persistentString(),
										approvalType.persistentString());
								continue;
							}
							currentEditorPreviouslyApprovedType = Optional.of(approvalType);
						}
					}

					Optional<RoleApprovalType> prioritizedApprovalType;
					if(!rolePrivilegesGranted){
						if(currentEditorPreviouslyApprovedType.isPresent()){
							prioritizedApprovalType = currentEditorPreviouslyApprovedType;
						}else{
							prioritizedApprovalType = prioritizedApprovalTypes.stream()
									.filter(approvalType -> requirementStatusByApprovalType.containsKey(approvalType)
											&& requirementStatusByApprovalType.get(approvalType)
												.currentApprovers().size()
											< requirementStatusByApprovalType.get(approvalType).requiredApprovals())
									.findFirst();
						}
					}else{
						prioritizedApprovalType = prioritizedApprovalTypes.stream()
								.filter(requirementStatusByApprovalType::containsKey)
								.findFirst();
					}
					boolean canRevoke = !DatarouterUserRoleRegistry.DATAROUTER_ADMIN.equals(availableRole)
							&& isDatarouterAdmin(editor)
							|| user.equals(editor);
					return new UserRoleMetadata(
							availableRole,
							currentRoles.contains(availableRole),
							requirementStatusByApprovalType,
							prioritizedApprovalType,
							Optional.of(canRevoke),
							groupsHasByRole.get(availableRole));
				})
				.list();
	}

}
