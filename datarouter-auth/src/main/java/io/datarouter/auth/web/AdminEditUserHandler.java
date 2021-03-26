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
package io.datarouter.auth.web;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.auth.config.DatarouterAuthFiles;
import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterAccountService;
import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.service.DatarouterUserEditService;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.service.UserInfo;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUser;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserDao;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserKey;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.auth.web.DatarouterPermissionRequestHandler.PermissionRequestDto;
import io.datarouter.auth.web.deprovisioning.DeprovisionedUserDto;
import io.datarouter.auth.web.deprovisioning.UserDeprovisioningStatusDto;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.types.RequestBody;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;
import io.datarouter.web.js.DatarouterWebJsTool;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.SessionBasedUser;
import io.datarouter.web.util.http.ResponseTool;

public class AdminEditUserHandler extends BaseHandler{

	//TODO DATAROUTER-2759 data fetching classes that require DatarouterUser or DatarouterSession databeans
	@Inject
	private DatarouterUserCreationService datarouterUserCreationService;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterUserEditService datarouterUserEditService;
	@Inject
	private DatarouterUserHistoryService datarouterUserHistoryService;
	@Inject
	private DatarouterAccountService datarouterAccountService;
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private RoleManager roleManager;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DatarouterAuthFiles files;
	@Inject
	private DatarouterPermissionRequestDao datarouterPermissionRequestDao;
	@Inject
	private DeprovisionedUserDao deprovisionedUserDao;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private Bootstrap4ReactPageFactory reactPageFactory;
	@Inject
	private UserInfo userInfo;
	@Inject
	private DatarouterService datarouterService;

	@Handler
	private Mav viewUsers(){
		return getReactMav("Datarouter - Users", Optional.empty());
	}

	@Handler
	private List<DatarouterUserListEntry> listUsers(){
		Set<Long> userIdsWithPermissionRequests = datarouterPermissionRequestDao.getUserIdsWithPermissionRequests();
		//TODO DATAROUTER-2794 refactor to use UserInfo#scanAllUsers without breaking vacuum job
		return datarouterUserDao.scan()
				.map(user -> new DatarouterUserListEntry(
						user.getId().toString(),
						user.getUsername(),
						user.getToken(),
						userIdsWithPermissionRequests.contains(user.getId())))
				.list();
	}

	//TODO DATAROUTER-2786
	@Handler
	private Mav createUser(){
		if(serverTypeDetector.mightBeProduction()){
			return pageFactory.message(request, "This is not supported on production");
		}
		var template = new CreateUserFormHtml(
				roleToStrings(roleManager.getConferrableRoles(getCurrentUser().getRoles())),
				authenticationConfig,
				paths.admin.createUserSubmit.toSlashedStringAfter(paths.admin, false));
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter - Create User")
				.withContent(template.build())
				.buildMav();
	}

	//TODO DATAROUTER-2786
	@Handler
	private Mav createUserSubmit(){
		if(serverTypeDetector.mightBeProduction()){
			return pageFactory.message(request, "This is not supported on production");
		}
		DatarouterUser currentUser = getCurrentUser();
		if(!roleManager.isAdmin(currentUser.getRoles())){
			handleInvalidRequest();
		}
		String username = params.required(authenticationConfig.getUsernameParam());
		String password = params.required(authenticationConfig.getPasswordParam());
		String[] roleStrings = params.optionalArray(authenticationConfig.getUserRolesParam()).orElse(new String[0]);
		Set<Role> requestedRoles = Arrays.stream(roleStrings)
				.map(roleManager::getRoleFromPersistentString)
				.collect(Collectors.toSet());
		boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), true);

		datarouterUserCreationService.createManualUser(currentUser, username, password, requestedRoles, enabled);
		return new InContextRedirectMav(request, paths.admin.viewUsers);
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private Mav editUser(){
		DatarouterUser currentUser = getCurrentUser();
		DatarouterUser userToEdit = params.optional("username")
				.map(DatarouterUserByUsernameLookup::new)
				.map(datarouterUserDao::getByUsername)
				.orElseGet(() -> {
					Optional<Long> optionalUserId = params.optionalLong("userId");
					if(optionalUserId.isPresent()){
						//TODO DATAROUTER-2788? consider what to display, since this breaks the page
						return optionalUserId.map(datarouterUserService::getUserById).get();
					}
					return currentUser;
				});
		if(!checkEditPermission(currentUser, userToEdit, datarouterUserService::canEditUser)){
			return null;
		}
		return getReactMav("Datarouter - Edit User " + userToEdit.getUsername(), Optional.of(userToEdit.getUsername()));
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private EditUserDetailsDto getUserDetails(String username){
		if(StringTool.isNullOrEmptyOrWhitespace(username)){
			return new EditUserDetailsDto("Invalid username.");
		}
		if(!checkEditPermission(getCurrentUser(), datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(
				username)), datarouterUserService::canEditUser)){
			return null;
		}
		return getEditUserDetailsDto(username);
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private EditUserDetailsDto updateUserDetails(@RequestBody EditUserDetailsDto dto){
		if(dto == null
				|| StringTool.isNullOrEmptyOrWhitespace(dto.username)
				|| dto.currentAccounts == null
				|| dto.currentRoles == null){
			return new EditUserDetailsDto("Invalid request.");
		}
		DatarouterUser currentUser = getCurrentUser();
		DatarouterUser userToEdit = datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(dto.username));
		if(!userToEdit.isEnabled()){
			return new EditUserDetailsDto("This user is not editable.");
		}
		if(!checkEditPermission(currentUser, userToEdit, datarouterUserService::canEditUser)){
			return null;
		}

		Set<Role> userRoles = Scanner.of(dto.currentRoles.entrySet())
				.include(Entry::getValue)
				.map(Entry::getKey)
				.map(roleManager::getRoleFromPersistentString)
				.collect(HashSet::new);
		Set<DatarouterAccountKey> requestedAccounts = Scanner.of(dto.currentAccounts.entrySet())
				.include(Entry::getValue)
				.map(Entry::getKey)
				.map(DatarouterAccountKey::new)
				.collect(HashSet::new);
		datarouterUserEditService.editUser(userToEdit, currentUser, userRoles, null, getSigninUrl(),
				requestedAccounts, dto.currentZoneId);
		return getEditUserDetailsDto(dto.username);
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private EditUserDetailsDto updatePassword(@RequestBody UpdatePasswordRequestDto dto){
		if(dto == null
				|| StringTool.isNullOrEmptyOrWhitespace(dto.username)
				|| StringTool.isNullOrEmptyOrWhitespace(dto.newPassword)){
			return new EditUserDetailsDto("Invalid request.");
		}
		DatarouterUser currentUser = getCurrentUser();
		DatarouterUser userToEdit = datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(dto.username));
		if(!checkEditPermission(currentUser, userToEdit, datarouterUserService::canEditUserPassword)){
			return null;
		}
		if(!datarouterUserService.canHavePassword(userToEdit)){
			return new EditUserDetailsDto("This user is externally authenticated and cannot have a password.");
		}
		datarouterUserEditService.changePassword(userToEdit, currentUser, dto.newPassword, getSigninUrl());
		return getEditUserDetailsDto(userToEdit.getUsername());
	}

	/*----------------- helpers --------------------*/

	private DatarouterUser getCurrentUser(){
		return datarouterUserService.getAndValidateCurrentUser(getSessionInfo().getRequiredSession());
	}

	private Mav getReactMav(String title, Optional<String> initialUsername){
		return reactPageFactory.startBuilder(request)
				.withTitle(title)
				.withReactScript(files.js.viewUsersJsx)
				.withJsRawConstant("PATHS", DatarouterWebJsTool.buildRawJsObject(buildPaths(request.getContextPath())))
				.withJsStringConstant("INITIAL_USERNAME", initialUsername.orElse(""))
				.buildMav();
	}

	private static List<String> roleToStrings(Collection<Role> roles){
		return roles.stream()
				.map(Role::getPersistentString)
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
	}

	private boolean checkEditPermission(
			DatarouterUser currentUser,
			DatarouterUser userToEdit,
			BiFunction<DatarouterUser,DatarouterUser,Boolean> permissionMethod){
		Objects.requireNonNull(currentUser);
		Objects.requireNonNull(userToEdit);
		if(!permissionMethod.apply(currentUser, userToEdit)){
			handleInvalidRequest();
			return false;
		}
		return true;
	}

	private String getSigninUrl(){
		String requestUrlWithoutContext = StringTool.getStringBeforeLastOccurrence(
				request.getRequestURI(),
				request.getRequestURL().toString());
		return requestUrlWithoutContext + request.getContextPath() + paths.signin.toSlashedString();
	}

	//TODO DATAROUTER-2788 this doesn't play nicely with JSON
	private void handleInvalidRequest(){
		ResponseTool.sendError(response, 403, "invalid request");
	}

	//TODO DATAROUTER-2788
	private EditUserDetailsDto getEditUserDetailsDto(String username){
		SessionBasedUser user = userInfo.getUserByUsername(username, false).orElseThrow();
		Set<Role> roles = userInfo.getRolesByUsername(username, false);

		List<PermissionRequestDto> permissionRequests = datarouterPermissionRequestDao
				.scanPermissionRequestsForUser(user.getId())
				.listTo(requests -> Scanner.of(datarouterUserHistoryService.getResolvedRequestToHistoryChangesMap(
						requests).entrySet()))
				.sorted(Comparator.comparing(Entry::getKey, DatarouterPermissionRequest
						.REVERSE_CHRONOLOGICAL_COMPARATOR))
				.map(AdminEditUserHandler::buildPermissionRequestDto)
				.list();

		return new EditUserDetailsDto(
				user.getUsername(),
				user.getId().toString(),
				user.getToken(),
				permissionRequests,
				deprovisionedUserDao.find(new DeprovisionedUserKey(username))
						.map(DeprovisionedUser::toDto)
						.orElseGet(() -> buildDeprovisionedUserDto(user, roles)),
				roleManager.getConferrableRoles(getSessionInfo().getRoles()),
				roles,
				datarouterAccountService.getAllAccountNamesWithUserMappingsEnabled(),
				datarouterAccountService.findAccountNamesForUserWithUserMappingsEnabled(user),
				true,
				"",
				user.getZoneId().map(ZoneId::getId).orElse(datarouterService.getZoneId().getId()));
	}

	//TODO DATAROUTER-2789
	private static PermissionRequestDto buildPermissionRequestDto(Entry<DatarouterPermissionRequest,
			Optional<String>> entry){
		DatarouterPermissionRequest request = entry.getKey();
		return new PermissionRequestDto(request.getKey().getRequestTime(), request.getRequestText(), request
				.getResolutionTime(), entry.getValue().orElse(null));
	}

	private static DeprovisionedUserDto buildDeprovisionedUserDto(SessionBasedUser user, Set<Role> roles){
		UserDeprovisioningStatusDto status = user.isEnabled()
				? UserDeprovisioningStatusDto.PROVISIONED
				: UserDeprovisioningStatusDto.UNRESTORABLE;
		return new DeprovisionedUserDto(user.getUsername(), Scanner.of(roles).map(Role::getPersistentString).list(),
				status);
	}

	private Map<String,String> buildPaths(String contextPath){
		return Map.of(
				"editUser", getPath(contextPath, paths.admin.editUser),
				"getUserDetails", getPath(contextPath, paths.admin.getUserDetails),
				"listUsers", getPath(contextPath, paths.admin.listUsers),
				"viewUsers", getPath(contextPath, paths.admin.viewUsers),
				"updatePassword", getPath(contextPath, paths.admin.updatePassword),
				"updateUserDetails", getPath(contextPath, paths.admin.updateUserDetails),
				"permissionRequest", getPath(contextPath, paths.permissionRequest),
				"declinePermissionRequests", getPath(contextPath, paths.permissionRequest.declinePermissionRequests),
				"deprovisionUsers", getPath(contextPath, paths.userDeprovisioning.deprovisionUsers),
				"restoreUsers", getPath(contextPath, paths.userDeprovisioning.restoreUsers));
	}

	private static String getPath(String contextPath, PathNode pathNode){
		return contextPath + pathNode.toSlashedString();
	}

	public static class DatarouterUserListEntry{

		public final String id;
		public final String username;
		public final String token;
		public final boolean hasPermissionRequest;

		public DatarouterUserListEntry(String id, String username, String token, boolean hasPermissionRequest){
			this.id = id;
			this.username = username;
			this.token = token;
			this.hasPermissionRequest = hasPermissionRequest;
		}

	}

	public static class EditUserDetailsDto{

		public final String username;
		public final String id;
		public final String token;
		public final List<PermissionRequestDto> requests;
		public final DeprovisionedUserDto deprovisionedUserDto;
		public final List<String> availableRoles;
		public final Map<String,Boolean> currentRoles;
		public final List<String> availableAccounts;
		public final Map<String,Boolean> currentAccounts;
		public final List<String> availableZoneIds;
		public final String currentZoneId;

		//TODO DATAROUTER-2788
		public final boolean success;
		public final String message;

		public EditUserDetailsDto(String username, String id, String token, List<PermissionRequestDto> requests,
				DeprovisionedUserDto deprovisionedUserDto, Collection<Role> availableRoles,
				Collection<Role> currentRoles, Collection<String> availableAccounts, Collection<String> currentAccounts,
				boolean success, String message, String currentZoneId){
			this.username = username;
			this.id = id;
			this.token = token;
			this.requests = requests;
			this.deprovisionedUserDto = deprovisionedUserDto;
			this.availableRoles = Scanner.of(availableRoles)
					.map(Role::getPersistentString)
					.sorted(StringTool.COLLATOR_COMPARATOR)
					.deduplicate()
					.list();
			Set<String> currentRolesSet = Scanner.of(currentRoles)
					.map(Role::getPersistentString)
					.collect(HashSet::new);
			this.currentRoles = Scanner.of(availableRoles)
					.map(Role::getPersistentString)
					.toMap(Function.identity(), currentRolesSet::contains);
			this.availableAccounts = Scanner.of(availableAccounts)
					.sorted(StringTool.COLLATOR_COMPARATOR)
					.deduplicate()
					.list();
			Set<String> currentAccountsSet = new HashSet<>(currentAccounts);
			this.currentAccounts = Scanner.of(availableAccounts)
					.toMap(Function.identity(), currentAccountsSet::contains);
			this.success = success;
			this.message = message;
			this.availableZoneIds = Scanner.of(ZoneIds.ZONE_IDS)
					.map(ZoneId::getId)
					.sorted()
					.list();
			this.currentZoneId = currentZoneId;
		}

		public EditUserDetailsDto(String errorMessage){
			this.username = null;
			this.id = null;
			this.token = null;
			this.requests = null;
			this.deprovisionedUserDto = null;
			this.availableRoles = null;
			this.currentRoles = null;
			this.availableAccounts = null;
			this.currentAccounts = null;
			this.success = false;
			this.message = errorMessage;
			this.availableZoneIds = null;
			this.currentZoneId = null;
		}

	}

	public static class UpdatePasswordRequestDto{

		public final String username;
		public final String newPassword;

		public UpdatePasswordRequestDto(String username, String newPassword){
			this.username = username;
			this.newPassword = newPassword;
		}

	}

}
