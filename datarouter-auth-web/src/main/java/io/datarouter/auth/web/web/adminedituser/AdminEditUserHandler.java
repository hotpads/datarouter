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
package io.datarouter.auth.web.web.adminedituser;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.auth.detail.DatarouterUserExternalDetailService;
import io.datarouter.auth.detail.DatarouterUserExternalDetails;
import io.datarouter.auth.detail.DatarouterUserProfileLink;
import io.datarouter.auth.link.EditUserLink;
import io.datarouter.auth.model.dto.PermissionRequestDto;
import io.datarouter.auth.model.dto.UserRoleMetadata;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.CurrentUserSessionInfoService;
import io.datarouter.auth.service.DatarouterAccountUserService;
import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.service.DatarouterUserEditService;
import io.datarouter.auth.service.DatarouterUserEditService.EditRolesResult;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.service.PermissionRequestService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryLog;
import io.datarouter.auth.web.config.DatarouterAuthFiles;
import io.datarouter.auth.web.util.DatarouterAuthPathUtil;
import io.datarouter.auth.web.web.CreateUserFormHtml;
import io.datarouter.auth.web.web.adminedituser.dto.DatarouterUserHistoryDto;
import io.datarouter.auth.web.web.adminedituser.dto.DatarouterUserListEntryDto;
import io.datarouter.auth.web.web.adminedituser.dto.EditAccountsRequestDto;
import io.datarouter.auth.web.web.adminedituser.dto.EditRolesRequestDto;
import io.datarouter.auth.web.web.adminedituser.dto.EditUserDetailDto;
import io.datarouter.auth.web.web.adminedituser.dto.EditUserDetailsDto;
import io.datarouter.auth.web.web.adminedituser.dto.EditUserDetailsDto.PagePermissionType;
import io.datarouter.auth.web.web.adminedituser.dto.GetAllRolesResponseDto;
import io.datarouter.auth.web.web.adminedituser.dto.UpdatePasswordRequestDto;
import io.datarouter.auth.web.web.adminedituser.dto.UpdateTimeZoneRequestDto;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.response.ApiResponseDto;
import io.datarouter.httpclient.response.ApiResponseErrorDto;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;
import io.datarouter.web.js.DatarouterWebJsTool;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;
import io.datarouter.web.util.http.ResponseTool;
import jakarta.inject.Inject;

public class AdminEditUserHandler extends BaseHandler{

	private static final String FAILED_ROLE_UPDATE_MESSAGE_PREFIX = "Failed to update some roles: ";

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
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private Bootstrap4ReactPageFactory reactPageFactory;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;
	@Inject
	private DatarouterUserExternalDetailService detailsService;
	@Inject
	private DatarouterWebFiles datarouterWebFiles;
	@Inject
	private DatarouterSamlSettings samlSettings;
	@Inject
	private PermissionRequestService permissionRequestService;
	@Inject
	private AdminEmail adminEmail;

	@Handler
	private Mav viewUsers(){
		return getReactMav("Datarouter - Users", Optional.empty());
	}

	@Handler
	private ApiResponseDto<List<DatarouterUserListEntryDto>> listUsers(){
		Set<Long> userIdsWithPermissionRequests = datarouterPermissionRequestDao.getUserIdsWithPermissionRequests();
		//TODO DATAROUTER-2794 refactor to use UserInfo#scanAllUsers without breaking vacuum job
		List<DatarouterUserListEntryDto> entries = datarouterUserDao.scan()
				.map(user -> {
					Optional<String> profileLink = detailsService.getUserProfileLink(user.getUsername())
							.map(DatarouterUserProfileLink::url);

					return new DatarouterUserListEntryDto(
							user.getId().toString(),
							user.getUsername(),
							user.getToken(),
							userIdsWithPermissionRequests.contains(user.getId()),
							profileLink.orElse(""),
							profileLink.isPresent() ? "" : "hidden",
							Scanner.of(user.getRolesIgnoreSaml())
									.map(Role::persistentString)
									.list(),
							Scanner.of(datarouterUserService.getUserRolesWithSamlGroups(user))
									.map(Role::persistentString)
									.list(),
							user.getEnabled());
				})
				.list();

		return ApiResponseDto.success(entries);
	}

	@Handler
	private Mav createUser(){
		if(serverTypeDetector.mightBeProduction()){
			return pageFactory.message(request, "This is not supported on production");
		}
		var template = new CreateUserFormHtml(
				roleToStrings(roleManager.getAllRoles()),
				authenticationConfig,
				paths.admin.createUserSubmit.toSlashedStringAfter(paths.admin, false));
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter - Create User")
				.withContent(template.build())
				.buildMav();
	}

	@Handler
	private Mav createUserSubmit(){
		if(serverTypeDetector.mightBeProduction()){
			return pageFactory.message(request, "This is not supported on production");
		}
		DatarouterUser currentUser = getCurrentUser();
		if(!datarouterUserService.isDatarouterAdmin(currentUser)){
			handleInvalidRequest();
		}
		String username = params.required(authenticationConfig.getUsernameParam());
		String password = params.required(authenticationConfig.getPasswordParam());
		String[] roleStrings = params.optionalArray(authenticationConfig.getUserRolesParam()).orElse(EmptyArray.STRING);
		Set<Role> requestedRoles = Scanner.of(roleStrings)
				.map(roleManager::findRoleFromPersistentString)
				.map(optionalRole -> optionalRole.orElseThrow(
						() -> new IllegalArgumentException(
								"Attempt to create user with unknown role(s): " + Arrays.toString(roleStrings))))
				.collect(Collectors.toSet());
		boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), true);

		datarouterUserCreationService.createManualUser(
				currentUser,
				username,
				password,
				requestedRoles,
				enabled,
				Optional.empty(),
				Optional.empty());
		return new InContextRedirectMav(request, paths.admin.viewUsers);
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private Mav editUser(){
		DatarouterUser currentUser = getCurrentUser();
		DatarouterUser userToEdit = params.optional(EditUserLink.P_username)
				.map(DatarouterUserByUsernameLookup::new)
				.map(datarouterUserDao::getByUsername)
				.or(() -> params.optionalLong(EditUserLink.P_userId)
						//TODO DATAROUTER-2788? consider what to display, since this breaks the page
						.map(id -> datarouterUserService.getUserById(id, false)))
				.orElse(currentUser);

		return getReactMav("Datarouter - Edit User " + userToEdit.getUsername(), Optional.of(userToEdit.getUsername()));
	}

	public record IsSamlEnabledResponse(boolean isSamlEnabled){}

	@Handler
	public ApiResponseDto<IsSamlEnabledResponse> getIsSamlEnabled(){
		return ApiResponseDto.success(new IsSamlEnabledResponse(samlSettings.getShouldProcess()));
	}

	@Handler
	public ApiResponseDto<GetAllRolesResponseDto> getAllRoles(){
		return ApiResponseDto.success(new GetAllRolesResponseDto(roleManager.getAllRoles()));
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private EditUserDetailsDto getUserDetails(String username){
		if(StringTool.isNullOrEmptyOrWhitespace(username)){
			return EditUserDetailsDto.error("Invalid username.");
		}
		return getEditUserDetailsDto(username);
	}

	@Handler
	public void getUserProfileImage(String username){
		detailsService.getUserImage(username)
				.map(ByteArrayOutputStream::toByteArray)
				.ifPresent(byteArray -> ResponseTool.writeToOutputStream(response, byteArray));
	}

	@Handler
	public ApiResponseDto<EditUserDetailsDto> editRoles(@RequestBody EditRolesRequestDto request){
		DatarouterUser userToEdit = getUserByUsername(request.username());
		EditRolesResult editRolesResult = datarouterUserEditService.editRoles(getCurrentUser(), userToEdit,
				request.updates(), getSigninUrl());
		return new ApiResponseDto<>(
				getEditUserDetailsDto(userToEdit.getUsername()),
				true,
				!editRolesResult.failedUpdates().isEmpty()
						? new ApiResponseErrorDto<>(
						FAILED_ROLE_UPDATE_MESSAGE_PREFIX + editRolesResult.failedUpdates(), null)
						: null,
				HttpStatus.SC_OK);
	}

	@Handler
	public ApiResponseDto<EditUserDetailsDto> editAccounts(@RequestBody EditAccountsRequestDto request){
		DatarouterUser userToEdit = getUserByUsername(request.username());
		if(!userToEdit.isEnabled()){
			return ApiResponseDto.badRequestError("Cannot edit accounts for deprovisioned user %s".formatted(
					userToEdit.getUsername()));
		}
		DatarouterUser editor = getCurrentUser();
		if(!datarouterUserService.canEditUser(editor, userToEdit)){
			return ApiResponseDto.forbidden("Current user %1$s cannot edit %2$s".formatted(
					editor.getUsername(),
					userToEdit.getUsername()));
		}
		datarouterUserEditService.editAccounts(editor, userToEdit, request.updates(), getSigninUrl());
		return ApiResponseDto.success(getEditUserDetailsDto(userToEdit.getUsername()));
	}

	@Handler
	public ApiResponseDto<EditUserDetailsDto> updateTimeZone(@RequestBody UpdateTimeZoneRequestDto request){
		DatarouterUser userToEdit = getUserByUsername(request.username());
		if(!userToEdit.isEnabled()){
			return ApiResponseDto.badRequestError("Cannot edit time zone for deprovisioned user %s".formatted(
					userToEdit.getUsername()));
		}
		DatarouterUser editor = getCurrentUser();
		if(!datarouterUserService.canEditUser(editor, userToEdit)){
			return ApiResponseDto.forbidden("Current user %1$s cannot edit %2$s".formatted(
					editor.getUsername(),
					userToEdit.getUsername()));
		}
		datarouterUserEditService.updateTimeZone(editor, userToEdit, request.timeZoneId());
		return ApiResponseDto.success(getEditUserDetailsDto(userToEdit.getUsername()));
	}

	//TODO DATAROUTER-2759 make this work without DatarouterUser
	@Handler
	private EditUserDetailsDto updatePassword(@RequestBody UpdatePasswordRequestDto dto){
		if(dto == null
				|| StringTool.isNullOrEmptyOrWhitespace(dto.username())
				|| StringTool.isNullOrEmptyOrWhitespace(dto.newPassword())){
			return EditUserDetailsDto.error("Invalid request.");
		}
		DatarouterUser editor = getCurrentUser();
		DatarouterUser userToEdit = getUserByUsername(dto.username());
		if(!checkEditPermission(editor, userToEdit, datarouterUserService::canEditUserPassword)){
			return null;
		}
		if(!datarouterUserService.canHavePassword(userToEdit)){
			return EditUserDetailsDto.error("This user is externally authenticated and cannot have a password.");
		}
		datarouterUserEditService.changePassword(userToEdit, editor, dto.newPassword(), getSigninUrl());
		return getEditUserDetailsDto(userToEdit.getUsername());
	}

	/*----------------- helpers --------------------*/

	private DatarouterUser getUserByUsername(String username){
		var userByUsernameKey = new DatarouterUserByUsernameLookup(username);
		return Require.notNull(datarouterUserDao.getByUsername(userByUsernameKey),
				"Unable to find user with username=" + username);
	}

	private DatarouterUser getCurrentUser(){
		return datarouterUserService.getAndValidateCurrentUser(getSessionInfo().getRequiredSession());
	}

	private Mav getReactMav(String title, Optional<String> initialUsername){
		return reactPageFactory.startBuilder(request)
				.withTitle(title)
				.withReactScript(files.js.viewUsersJsx)
				.withJsRawConstant("PATHS", DatarouterWebJsTool.buildRawJsObject(buildPaths(request.getContextPath())))
				.withJsStringConstant("INITIAL_USERNAME", initialUsername.orElse(""))
				.withRequires(DatarouterWebRequireJsV2.MULTIPLE_SELECT)
				.withCss(datarouterWebFiles.jeeAssets.multipleSelect.multipleSelectCss)
				.buildMav();
	}

	private static List<String> roleToStrings(Collection<Role> roles){
		return roles.stream()
				.map(Role::persistentString)
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.toList();
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
		return DatarouterAuthPathUtil.getSignInUrl(request, paths);
	}

	//TODO DATAROUTER-2788 this doesn't play nicely with JSON
	private void handleInvalidRequest(){
		ResponseTool.sendError(response, 403, "invalid request");
	}

	private EditUserDetailsDto getEditUserDetailsDto(String username){
		DatarouterUser user = datarouterUserService.getUserByUsername(username, false);

		List<PermissionRequestDto> permissionRequests =
				permissionRequestService.getReverseChronologicalPermissionRequestDtos(
						user,
						currentUserSessionInfoService.getZoneId(getRequest()));

		List<DatarouterUserHistoryLog> userHistory = datarouterUserHistoryService.getHistoryForUser(user.getId());
		List<DatarouterUserHistoryDto> history = Scanner.of(userHistory)
				.map(historyRecord -> {
					String editorUsername = Optional.ofNullable(historyRecord.getEditor())
							.flatMap(id -> datarouterUserService.findUserById(id, false))
							.map(DatarouterUser::getUsername)
							.orElseGet(() -> historyRecord.getEditor() != null
									&& historyRecord.getEditor().equals(DatarouterUserCreationService.ADMIN_ID)
									? adminEmail.get()
									: String.valueOf(historyRecord.getEditor()));
					return new DatarouterUserHistoryDto(
							historyRecord.getKey().getTime().toEpochMilli(),
							editorUsername,
							historyRecord.getChangeType().persistentString,
							historyRecord.getChanges());
				})
				.reverse()
				.list();

		Optional<DatarouterUserExternalDetails> details = detailsService.getUserDetails(username);
		String profileLink = detailsService.getUserProfileLink(user.getUsername())
				.map(DatarouterUserProfileLink::url)
				.orElse("");
		DatarouterUser editor = getCurrentUser();
		List<String> availableAccounts = Scanner.of(datarouterAccountUserService
				.getAllAccountNamesWithUserMappingsEnabled())
				.sort(StringTool.COLLATOR_COMPARATOR)
				.deduplicateConsecutive()
				.list();
		Set<String> activeAccounts = datarouterAccountUserService.findAccountNamesForUser(user);

		return new EditUserDetailsDto(
				datarouterUserService.isDatarouterAdmin(editor)
						? PagePermissionType.ADMIN : PagePermissionType.ROLES_ONLY,
				editor.getUsername(),
				user.getUsername(),
				user.getId().toString(),
				user.getToken(),
				profileLink,
				permissionRequests,
				history,
				user.getEnabled(),
				Scanner.of(datarouterUserService.getRoleMetadataForUser(editor, user))
						.map(UserRoleMetadata::toJsDto)
						.list(),
				availableAccounts,
				Scanner.of(availableAccounts)
						.toMap(Function.identity(), activeAccounts::contains),
				Scanner.of(ZoneIds.ZONE_IDS)
						.map(ZoneId::getId)
						.sort()
						.list(),
				// zoneId can be configured through the UI, fallback to system default
				user.getZoneId().map(ZoneId::getId).orElse(ZoneId.systemDefault().getId()),
				details.map(DatarouterUserExternalDetails::fullName).orElse(null),
				detailsService.userImageSupported(),
				details.map(DatarouterUserExternalDetails::displayDetails)
						.map(Scanner::of)
						.orElseGet(Scanner::empty)
						.map(detail -> new EditUserDetailDto(detail.key(), detail.name(), detail.link().orElse(null)))
						.list(),
				true,
				"");
	}

	private Map<String,String> buildPaths(String contextPath){
		Map<String,String> allPaths = new HashMap<>(Map.of(
				"editUser", getPath(contextPath, paths.admin.editUser),
				"getUserDetails", getPath(contextPath, paths.admin.getUserDetails),
				"getUserProfileImage", getPath(contextPath, paths.admin.getUserProfileImage),
				"listUsers", getPath(contextPath, paths.admin.listUsers),
				"viewUsers", getPath(contextPath, paths.admin.viewUsers),
				"updatePassword", getPath(contextPath, paths.admin.updatePassword),
				"permissionRequest", getPath(contextPath, paths.permissionRequest),
				"declinePermissionRequests", getPath(contextPath, paths.permissionRequest.declinePermissionRequests)));
		//too many to fit in Map.of anymore
		allPaths.put("editRoles", getPath(contextPath, paths.admin.editRoles));
		allPaths.put("editAccounts", getPath(contextPath, paths.admin.editAccounts));
		allPaths.put("updateTimeZone", getPath(contextPath, paths.admin.updateTimeZone));
		allPaths.put("getAllRoles", getPath(contextPath, paths.admin.getAllRoles));
		allPaths.put("getIsSamlEnabled", getPath(contextPath, paths.admin.getIsSamlEnabled));
		return allPaths;
	}

	private static String getPath(String contextPath, PathNode pathNode){
		return contextPath + pathNode.toSlashedString();
	}

}
