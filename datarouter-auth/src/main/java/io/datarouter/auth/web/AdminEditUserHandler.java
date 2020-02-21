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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.datarouter.auth.config.DatarouterAuthFiles;
import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterAccountService;
import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.service.DatarouterUserEditService;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMap;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.util.http.ResponseTool;

public class AdminEditUserHandler extends BaseHandler{

	private static final String AUTHENTICATION_CONFIG = "authenticationConfig";
	private static final String DATAROUTER_USER_ROLES = "datarouterUserRoles";
	private static final String USER = "user";
	private static final String USER_ROLES = "userRoles";

	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private BaseDatarouterAccountDao datarouterAccountDao;
	@Inject
	private DatarouterAccountService datarouterAccountService;
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterPermissionRequestDao datarouterPermissionRequestDao;
	@Inject
	private DatarouterUserCreationService datarouterUserCreationService;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private RoleManager roleManager;
	@Inject
	private DatarouterUserEditService datarouterUserEditService;
	@Inject
	private DatarouterUserHistoryService datarouterUserHistoryService;
	@Inject
	private BaseDatarouterUserAccountMapDao userAccountMapDao;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DatarouterAuthFiles files;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private Bootstrap4ReactPageFactory reactPageFactory;

	@Handler
	private Mav viewUsers(){
		return reactPageFactory.startBuilder(request)
				.withTitle("Datarouter - Users")
				.withReactScript(files.js.viewUsersJsx)
				.buildMav();
	}

	@Handler
	private List<DatarouterUserListEntry> listUsers(){
		Set<DatarouterUserKey> userKeysWithPermissionRequests = datarouterPermissionRequestDao
				.getUserKeysWithPermissionRequests();
		return datarouterUserDao.scan()
				.map(user -> new DatarouterUserListEntry(
						user.getKey().getId().toString(),
						user.getUsername(),
						user.getUserToken(),
						userKeysWithPermissionRequests.contains(user.getKey())))
				.list();
	}

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
		String[] requestedRoles = params.getRequest().getParameterValues(authenticationConfig.getUserRolesParam());
		boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), true);

		datarouterUserCreationService.createManualUser(currentUser, username, password, requestedRoles, enabled);
		return new InContextRedirectMav(request, paths.admin.viewUsers);
	}

	@Handler
	private Mav editUser(){
		DatarouterUser currentUser = getCurrentUser();
		Long userId = params.optionalLong(authenticationConfig.getUserIdParam(), currentUser.getId());
		DatarouterUser userToEdit = datarouterUserService.getUserById(userId);
		checkEditPermission(currentUser, userToEdit);

		Mav mav = new Mav(files.jsp.authentication.editUserFormJsp);
		mav.put(USER, userToEdit);

		List<DatarouterPermissionRequest> currentRequests = new ArrayList<>();
		List<DatarouterPermissionRequest> pastRequests = new ArrayList<>();
		datarouterPermissionRequestDao.scanPermissionRequestsForUser(userId).forEach(request -> {
			if(request.getResolution() == null){
				currentRequests.add(request);
			}else{
				pastRequests.add(request);
			}
		});
		currentRequests.sort(DatarouterPermissionRequest.REVERSE_CHRONOLOGICAL_COMPARATOR);
		Map<DatarouterPermissionRequest, String> resolvedRequests = new TreeMap<>(DatarouterPermissionRequest
				.REVERSE_CHRONOLOGICAL_COMPARATOR);
		resolvedRequests.putAll(datarouterUserHistoryService.getResolvedRequestToHistoryChangesMap(pastRequests));
		mav.put("currentRequests", currentRequests);
		mav.put("resolvedRequests", resolvedRequests);

		mav.put(AUTHENTICATION_CONFIG, authenticationConfig);
		addPaths(mav);
		mav.put(DATAROUTER_USER_ROLES, roleToStrings(roleManager.getConferrableRoles(currentUser.getRoles())));
		mav.put(USER_ROLES, roleToStrings(userToEdit.getRoles()));
		mav.put("datarouterAccounts", datarouterAccountDao.scan()
				.sorted(Comparator.comparing(accnt -> accnt.getKey().getAccountName(), String.CASE_INSENSITIVE_ORDER))
				.include(DatarouterAccount::getEnableUserMappings)
				.list());
		mav.put("userAccounts", datarouterAccountService.findAccountNamesForUser(new DatarouterUserKey(userId)));
		mav.put("permissionRequestPage", request.getContextPath() + paths.permissionRequest.toSlashedString());
		mav.put("thisPagePath", request.getRequestURI() + (request.getQueryString() == null ? "" : "?" + request
				.getQueryString()));
		mav.put("declinePath", request.getContextPath() + paths.permissionRequest.declineAll.toSlashedString());
		return mav;
	}

	@Handler
	private Mav editUserSubmit(){
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		Boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), false);
		String[] userRoles = params.optionalArray(authenticationConfig.getUserRolesParam()).orElse(new String[0]);
		DatarouterUser currentUser = getCurrentUser();
		DatarouterUser userToEdit = datarouterUserService.getUserById(userId);
		checkEditPermission(currentUser, userToEdit);

		Set<DatarouterAccountKey> requestedAccounts = params.optionalArray("accounts")
				.map(Arrays::stream)
				.orElseGet(Stream::empty)
				.map(DatarouterAccountKey::new)
				.collect(Collectors.toSet());
		List<DatarouterUserAccountMapKey> accountsToDelete = userAccountMapDao
				.scanKeysWithPrefix(new DatarouterUserAccountMapKey(userId, null))
				.include(userAccountKey -> !requestedAccounts.contains(userAccountKey.getDatarouterAccountKey()))
				.list();
		userAccountMapDao.deleteMulti(accountsToDelete);
		List<DatarouterUserAccountMap> accountsToAdd = requestedAccounts.stream()
				.map(accountKey -> new DatarouterUserAccountMap(userId, accountKey.getAccountName()))
				.collect(Collectors.toList());
		userAccountMapDao.putMulti(accountsToAdd);

		List<String> accountsAdded = accountsToAdd.stream()
				.map(DatarouterUserAccountMap::getKey)
				.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(Collectors.toList());
		List<String> currentAccounts = accountsToDelete.stream()
				.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(Collectors.toList());
		datarouterUserEditService.editUser(userToEdit, currentUser, userRoles, enabled, getSigninUrl(), accountsAdded,
				currentAccounts);

		//display all users if userToEdit is no longer editable by currentUser after this edit
		if(datarouterUserService.canEditUser(userToEdit, currentUser)){
			return new InContextRedirectMav(request, paths.admin.editUser.toSlashedString() + "?userId=" + userId);
		}
		return new InContextRedirectMav(request, paths.admin.viewUsers.toSlashedString());
	}

	@Handler
	private Mav resetPassword(){
		DatarouterUser currentUser = getCurrentUser();
		Long userId = params.optionalLong(authenticationConfig.getUserIdParam(), currentUser.getId());
		DatarouterUser userToEdit = datarouterUserService.getUserById(userId);

		checkEditPermission(currentUser, userToEdit);
		Mav mav = new Mav(files.jsp.authentication.resetPasswordFormJsp);
		mav.put("enabled", datarouterUserService.canHavePassword(userToEdit));
		mav.put(USER, userToEdit);
		mav.put(AUTHENTICATION_CONFIG, authenticationConfig);
		addPaths(mav);
		return mav;
	}

	@Handler
	private Mav resetPasswordSubmit(){
		String password = params.required(authenticationConfig.getPasswordParam());
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser();
		DatarouterUser userToEdit = datarouterUserService.getUserById(userId);
		checkEditPermission(currentUser, userToEdit);
		if(!datarouterUserService.canHavePassword(userToEdit)){
			return new MessageMav("This user is externally authenticated and cannot have a password.");
		}
		datarouterUserEditService.changePassword(userToEdit, currentUser, password, getSigninUrl());
		String path = pathBuilder(paths.admin.editUser.toSlashedString(), authenticationConfig.getUserIdParam(),
				userId.toString());
		return new InContextRedirectMav(request, path);
	}

	/*----------------- helpers --------------------*/

	private DatarouterUser getCurrentUser(){
		return datarouterUserService.getAndValidateCurrentUser(params.getSession());
	}

	private static List<String> roleToStrings(Collection<Role> roles){
		return roles.stream()
				.map(Role::getPersistentString)
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
	}

	private String pathBuilder(String path, String param, String value){
		return path + "?" + param + "=" + value;
	}

	private void checkEditPermission(DatarouterUser currentUser, DatarouterUser userToEdit){
		Objects.requireNonNull(currentUser);
		Objects.requireNonNull(userToEdit);
		if(!datarouterUserService.canEditUser(userToEdit, currentUser)){
			handleInvalidRequest();
		}
	}

	private String getSigninUrl(){
		String requestUrlWithoutContext = StringTool.getStringBeforeLastOccurrence(request.getRequestURI(), request
				.getRequestURL().toString());
		return requestUrlWithoutContext + request.getContextPath() + paths.signin.toSlashedString();
	}

	private void handleInvalidRequest(){
		ResponseTool.sendError(response, 403, "invalid request");
	}

	private void addPaths(Mav mav){
		mav.put("createUserSubmitPath", paths.admin.createUserSubmit.toSlashedString());
		mav.put("resetPasswordSubmitPath", paths.resetPasswordSubmit.toSlashedString());
		mav.put("resetPasswordPath", paths.resetPassword.toSlashedString());
		mav.put("editUserSubmitPath", paths.admin.editUserSubmit.toSlashedString());
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

}
