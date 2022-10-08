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
package io.datarouter.auth.web;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
import io.datarouter.auth.service.CopyUserListener;
import io.datarouter.auth.service.DatarouterAccountUserService;
import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.service.DatarouterUserEditService;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.service.UserInfo.UserInfoSupplier;
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
import io.datarouter.bytes.EmptyArray;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;
import io.datarouter.web.js.DatarouterWebJsTool;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import io.datarouter.web.user.detail.DatarouterUserExternalDetails;
import io.datarouter.web.user.detail.DatarouterUserProfileLink;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
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
	private DatarouterAccountUserService datarouterAccountUserService;
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
	private UserInfoSupplier userInfo;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;
	@Inject
	private CopyUserListener copyUserListener;
	@Inject
	private DatarouterUserExternalDetailService detailsService;

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
						userIdsWithPermissionRequests.contains(user.getId()),
						detailsService.getUserProfileLink(user.getUsername())
								.map(DatarouterUserProfileLink::url)
								.orElse("")))
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
		String[] roleStrings = params.optionalArray(authenticationConfig.getUserRolesParam()).orElse(EmptyArray.STRING);
		Set<Role> requestedRoles = Arrays.stream(roleStrings)
				.map(roleManager::getRoleFromPersistentString)
				.collect(Collectors.toSet());
		boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), true);

		datarouterUserCreationService.createManualUser(currentUser, username, password, requestedRoles, enabled,
				Optional.empty(), Optional.empty());
		return new InContextRedirectMav(request, paths.admin.viewUsers);
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private Mav editUser(){
		DatarouterUser currentUser = getCurrentUser();
		DatarouterUser userToEdit = params.optional("username")
				.map(DatarouterUserByUsernameLookup::new)
				.map(datarouterUserDao::getByUsername)
				.or(() -> params.optionalLong("userId")
						//TODO DATAROUTER-2788? consider what to display, since this breaks the page
						.map(datarouterUserService::getUserById))
				.orElse(currentUser);

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

	@Handler
	public void getUserProfileImage(String username){
		detailsService.getUserImage(username)
				.map(ByteArrayOutputStream::toByteArray)
				.ifPresent(byteArray -> ResponseTool.writeToOutputStream(response, byteArray));
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

		Set<Role> requestedUserRoles = Scanner.of(dto.currentRoles.entrySet())
				.include(Entry::getValue)
				.map(Entry::getKey)
				.map(roleManager::getRoleFromPersistentString)
				.collect(HashSet::new);
		Set<DatarouterAccountKey> requestedAccounts = Scanner.of(dto.currentAccounts.entrySet())
				.include(Entry::getValue)
				.map(Entry::getKey)
				.map(DatarouterAccountKey::new)
				.collect(HashSet::new);
		datarouterUserEditService.editUser(userToEdit, currentUser, requestedUserRoles, null, getSigninUrl(),
				requestedAccounts, Optional.ofNullable(dto.currentZoneId).map(ZoneId::of), Optional.empty());
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
		DatarouterUser editor = getCurrentUser();
		DatarouterUser userToEdit = datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(dto.username));
		if(!checkEditPermission(editor, userToEdit, datarouterUserService::canEditUserPassword)){
			return null;
		}
		if(!datarouterUserService.canHavePassword(userToEdit)){
			return new EditUserDetailsDto("This user is externally authenticated and cannot have a password.");
		}
		datarouterUserEditService.changePassword(userToEdit, editor, dto.newPassword, getSigninUrl());
		return getEditUserDetailsDto(userToEdit.getUsername());
	}

	@Handler
	private EditUserDetailsDto copyUser(String oldUsername, String newUsername){
		if(StringTool.isNullOrEmptyOrWhitespace(oldUsername)
				|| StringTool.isNullOrEmptyOrWhitespace(newUsername)){
			return new EditUserDetailsDto("Invalid request.");
		}
		DatarouterUser editor = getCurrentUser();
		DatarouterUser oldUser = datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(oldUsername));
		if(editor.getUsername().equals(oldUser.getUsername())){
			return new EditUserDetailsDto("Cannot copy yourself.");
		}
		if(!datarouterUserService.canEditUser(editor, oldUser)){
			return new EditUserDetailsDto("Cannot copy user.");
		}

		Set<Role> requestedRoles;
		if(oldUser.isEnabled()){
			requestedRoles = new HashSet<>(oldUser.getRoles());
		}else{
			//copy roles from deprovisioned user info, if present
			requestedRoles = deprovisionedUserDao.find(new DeprovisionedUserKey(oldUsername))
					.map(DeprovisionedUser::getRoles)
					.orElseGet(HashSet::new);
		}
		Set<DatarouterAccountKey> requestedAccounts = Scanner.of(datarouterAccountUserService.findAccountNamesForUser(
				oldUser))
				.map(DatarouterAccountKey::new)
				.collect(Collectors.toCollection(HashSet::new));
		Optional<ZoneId> zoneId = oldUser.getZoneId();

		//if newUser exists, do an "edit"; else do a "create" then "edit" (since accounts are not set in "create")
		DatarouterUser newUser = datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(newUsername));
		var description = Optional.of("User copied from " + oldUsername + " by " + editor.getUsername());
		if(newUser == null){
			newUser = datarouterUserCreationService.createManualUser(editor, newUsername, null, requestedRoles, true,
					zoneId, description);
		}else{
			//preserve existing roles and accounts that are not present on the source user of the copy
			requestedRoles.addAll(newUser.getRoles());
			Scanner.of(datarouterAccountUserService.findAccountNamesForUser(newUser))
					.map(DatarouterAccountKey::new)
					.forEach(requestedAccounts::add);
		}
		var signinUrl = getSigninUrl();
		datarouterUserEditService.editUser(newUser, editor, requestedRoles, true, signinUrl, requestedAccounts, zoneId,
				description);
		//add history to user that was copied from
		datarouterUserHistoryService.recordMessage(oldUser, editor, "User copied to " + newUsername + " by " + editor
				.getUsername());
		copyUserListener.onCopiedUser(oldUsername, newUsername);
		return getEditUserDetailsDto(oldUsername);
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
		SessionBasedUser user = userInfo.get().getUserByUsername(username, false).orElseThrow();
		Set<Role> roles = userInfo.get().getRolesByUsername(username, false);

		List<PermissionRequestDto> permissionRequests = datarouterPermissionRequestDao
				.scanPermissionRequestsForUser(user.getId())
				.listTo(requests -> Scanner.of(datarouterUserHistoryService.getResolvedRequestToHistoryChangesMap(
						requests).entrySet()))
				.sort(Comparator.comparing(Entry::getKey, DatarouterPermissionRequest
						.REVERSE_CHRONOLOGICAL_COMPARATOR))
				.map(this::buildPermissionRequestDto)
				.list();

		Optional<DatarouterUserExternalDetails> details = detailsService.getUserDetails(username);

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
				datarouterAccountUserService.getAllAccountNamesWithUserMappingsEnabled(),
				datarouterAccountUserService.findAccountNamesForUser(user),
				true,
				"",
				// zoneId can be configured through the UI, fallback to system default
				user.getZoneId().map(ZoneId::getId).orElse(ZoneId.systemDefault().getId()),
				details.map(DatarouterUserExternalDetails::fullName).orElse(null),
				detailsService.userImageSupported(),
				details.map(DatarouterUserExternalDetails::displayDetails).map(Scanner::of).orElseGet(Scanner::empty)
						.map(detail -> new EditUserDetailDto(detail.key(), detail.name(), detail.link().orElse(null)))
						.list());
	}

	//TODO DATAROUTER-2789
	private PermissionRequestDto buildPermissionRequestDto(Entry<DatarouterPermissionRequest,
			Optional<String>> entry){
		ZoneId zoneId = currentUserSessionInfoService.getZoneId(getRequest());
		DatarouterPermissionRequest request = entry.getKey();
		return new PermissionRequestDto(
				request.getKey().getRequestTime(),
				request.getRequestText(),
				request.getResolutionTime(),
				entry.getValue().orElse(null),
				zoneId);
	}

	private static DeprovisionedUserDto buildDeprovisionedUserDto(SessionBasedUser user, Set<Role> roles){
		UserDeprovisioningStatusDto status = user.isEnabled()
				? UserDeprovisioningStatusDto.PROVISIONED
				: UserDeprovisioningStatusDto.NO_RECORD;
		return new DeprovisionedUserDto(user.getUsername(), Scanner.of(roles).map(Role::getPersistentString).list(),
				status);
	}

	private Map<String,String> buildPaths(String contextPath){
		Map<String,String> allPaths = new HashMap<>(Map.of(
				"editUser", getPath(contextPath, paths.admin.editUser),
				"getUserDetails", getPath(contextPath, paths.admin.getUserDetails),
				"getUserProfileImage", getPath(contextPath, paths.admin.getUserProfileImage),
				"listUsers", getPath(contextPath, paths.admin.listUsers),
				"viewUsers", getPath(contextPath, paths.admin.viewUsers),
				"updatePassword", getPath(contextPath, paths.admin.updatePassword),
				"updateUserDetails", getPath(contextPath, paths.admin.updateUserDetails),
				"permissionRequest", getPath(contextPath, paths.permissionRequest),
				"declinePermissionRequests", getPath(contextPath, paths.permissionRequest.declinePermissionRequests),
				"deprovisionUsers", getPath(contextPath, paths.userDeprovisioning.deprovisionUsers)));
		//too many to fit in Map.of anymore
		allPaths.put("copyUser", getPath(contextPath, paths.admin.copyUser));
		allPaths.put("restoreUsers", getPath(contextPath, paths.userDeprovisioning.restoreUsers));
		return allPaths;
	}

	private static String getPath(String contextPath, PathNode pathNode){
		return contextPath + pathNode.toSlashedString();
	}

	public static class DatarouterUserListEntry{

		public final String id;
		public final String username;
		public final String token;
		public final boolean hasPermissionRequest;
		public final String profileLink;
		public final String profileClass;

		public DatarouterUserListEntry(String id, String username, String token, boolean hasPermissionRequest,
				String profileLink){
			this.id = id;
			this.username = username;
			this.token = token;
			this.hasPermissionRequest = hasPermissionRequest;
			this.profileLink = profileLink;
			this.profileClass = profileLink.isEmpty() ? "hidden" : "";
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
		public final String fullName;
		public final Boolean hasProfileImage;
		public final List<EditUserDetailDto> details;

		//TODO DATAROUTER-2788
		public final boolean success;
		public final String message;

		public EditUserDetailsDto(String username, String id, String token, List<PermissionRequestDto> requests,
				DeprovisionedUserDto deprovisionedUserDto, Collection<Role> availableRoles,
				Collection<Role> currentRoles, Collection<String> availableAccounts, Collection<String> currentAccounts,
				boolean success, String message, String currentZoneId, String fullName, boolean hasProfileImage,
				List<EditUserDetailDto> details){
			this.username = username;
			this.id = id;
			this.token = token;
			this.requests = requests;
			this.deprovisionedUserDto = deprovisionedUserDto;
			this.availableRoles = Scanner.of(availableRoles)
					.map(Role::getPersistentString)
					.sort(StringTool.COLLATOR_COMPARATOR)
					.deduplicateConsecutive()
					.list();
			Set<String> currentRolesSet = Scanner.of(currentRoles)
					.map(Role::getPersistentString)
					.collect(HashSet::new);
			this.currentRoles = Scanner.of(availableRoles)
					.map(Role::getPersistentString)
					.toMap(Function.identity(), currentRolesSet::contains);
			this.availableAccounts = Scanner.of(availableAccounts)
					.sort(StringTool.COLLATOR_COMPARATOR)
					.deduplicateConsecutive()
					.list();
			Set<String> currentAccountsSet = new HashSet<>(currentAccounts);
			this.currentAccounts = Scanner.of(availableAccounts)
					.toMap(Function.identity(), currentAccountsSet::contains);
			this.success = success;
			this.message = message;
			this.availableZoneIds = Scanner.of(ZoneIds.ZONE_IDS)
					.map(ZoneId::getId)
					.sort()
					.list();
			this.currentZoneId = currentZoneId;
			this.fullName = fullName;
			this.hasProfileImage = hasProfileImage;
			this.details = details;
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
			this.fullName = null;
			this.hasProfileImage = null;
			this.details = null;
		}

	}

	public static class EditUserDetailDto{

		public final String name;
		public final String value;
		public final String link;

		public EditUserDetailDto(String name, String value, String link){
			this.name = name;
			this.value = value;
			this.link = link;
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
