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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.model.dto.RoleApprovalRequirementStatus;
import io.datarouter.auth.model.dto.UserRoleMetadata;
import io.datarouter.auth.storage.roleapprovals.DatarouterUserRoleApprovalDao;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.role.Role;
import io.datarouter.web.user.role.RoleApprovalType;
import io.datarouter.web.user.role.RoleManager;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.util.PasswordTool;

@Singleton
public class DatarouterUserService{

	@Inject
	private DatarouterUserDao nodes;
	@Inject
	private RoleManager roleManager;
	@Inject
	private DatarouterUserRoleApprovalDao roleApprovalDao;

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
		return nodes.get(new DatarouterUserKey(session.getUserId()));
	}

	public DatarouterUser getUserById(Long id){
		return nodes.get(new DatarouterUserKey(id));
	}

	public boolean canEditUserPassword(DatarouterUser editor, DatarouterUser user){
		return user.equals(editor)
				|| !isDatarouterAdmin(user)
				&& roleManager.isAdmin(editor.getRoles())
				&& editor.getEnabled();
	}

	public boolean canEditUser(DatarouterUser editor, DatarouterUser user){
		return user.equals(editor)
				|| roleManager.isAdmin(editor.getRoles())
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

	public boolean isPasswordCorrect(String email, String rawPassword){
		DatarouterUser user = nodes.getByUsername(new DatarouterUserByUsernameLookup(email));
		return isPasswordCorrect(user, rawPassword);
	}

	public Set<Role> getAllowedUserRoles(DatarouterUser currentUser, Set<Role> userRoles){
		Collection<Role> validRoles = roleManager.getConferrableRoles(currentUser.getRoles());
		userRoles.retainAll(validRoles);
		userRoles.add(DatarouterUserRole.REQUESTOR.getRole());// everyone should have this
		return userRoles;
	}

	public void assertUserDoesNotExist(Long id, String userToken, String username){
		DatarouterUser userWithId = getUserById(id);
		if(userWithId != null){
			throw new IllegalArgumentException("DatarouterUser already exists with id=" + id);
		}
		DatarouterUser userWithUserToken = nodes.getByUserToken(new DatarouterUserByUserTokenLookup(userToken));
		if(userWithUserToken != null){
			throw new IllegalArgumentException("DatarouterUser already exists with userToken=" + userToken);
		}
		DatarouterUser userWithEmail = nodes.getByUsername(new DatarouterUserByUsernameLookup(username));
		if(userWithEmail != null){
			throw new IllegalArgumentException("DatarouterUser already exists with username=" + username);
		}
	}

	public boolean isDatarouterAdmin(DatarouterUser user){
		return user.getRoles().contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole());
	}

	public Map<Role,Map<RoleApprovalType,Set<String>>> getCurrentRoleApprovals(DatarouterUser user){
		return roleApprovalDao.getAllOutstandingApprovalsForUser(user)
				.stream()
				.collect(Collectors.groupingBy(
						roleApproval ->
								roleManager.getRoleFromPersistentString(roleApproval.getKey().getRequestedRole()),
						Collectors.groupingBy(
								roleApproval -> roleManager.getRoleApprovalTypeFromPersistentString(
										roleApproval.getApprovalType()),
								Collectors.mapping(roleApproval -> roleApproval.getKey().getApproverUsername(),
										Collectors.toSet()))));
	}

	public List<UserRoleMetadata> getRoleMetadataForUser(DatarouterUser editor, DatarouterUser user){
		Set<Role> currentRoles = new HashSet<>(user.getRoles());
		Set<Role> availableRoles = roleManager.getConferrableRoles(roleManager.getAllRoles());
		Map<Role,Map<RoleApprovalType,Integer>> roleApprovalRequirements = roleManager.getAllRoleApprovalRequirements();
		Map<Role,Map<RoleApprovalType,Set<String>>> currentRoleApprovals = getCurrentRoleApprovals(user);
		Set<RoleApprovalType> relevantApprovalTypes = new HashSet<>();
		Scanner.of(roleApprovalRequirements.values())
				.map(Map::keySet)
				.forEach(relevantApprovalTypes::addAll);
		List<RoleApprovalType> prioritizedApprovalTypes =
				roleManager.getPrioritizedRoleApprovalTypes(editor, user, relevantApprovalTypes);

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
										availableRole.persistentString,
										approvalType.persistentString);
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
					boolean canRevoke = !DatarouterUserRole.DATAROUTER_ADMIN.getPersistentString().equals(
							availableRole.getPersistentString())
							&& roleManager.isAdmin(editor.getRoles())
							|| user.equals(editor);
					return new UserRoleMetadata(
							availableRole,
							currentRoles.contains(availableRole),
							requirementStatusByApprovalType,
							prioritizedApprovalType,
							Optional.of(canRevoke));
				})
				.collect(Collectors.toList());
	}
}
