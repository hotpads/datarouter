package com.hotpads.handler.user.authenticate;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.ResponseTool;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.GlobalRedirectMav;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUserTokenLookup;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserKey;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.number.RandomTool;

public class AdminEditUserHandler extends BaseHandler{
	
	private static final String USER_ROLES = "userRoles";
	private static final String USER_LIST = "userList";
	private static final String USER = "user";
	private static final String DATAROUTER_USER_ROLES = "datarouterUserRoles";
	private static final String AUTHENTICATION_CONFIG = "authenticationConfig";
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterPasswordService passwordService;
	@Inject
	private DatarouterUserNodes userNodes;
	
	@Override
	protected Mav handleDefault() {
		// TODO smarter and more intuitive redirects - redirect to MAV instead of URI
		// Mav.setRedirect(true) does not actually issue a redirect
		return new GlobalRedirectMav(params.getContextPath());
	}
	
	@Handler
	private Mav viewUsers() {
		Mav mav = new Mav("/jsp/authentication/viewUsers.jsp");
		List<DatarouterUser> userList = userNodes.getUserNode().getAll(null);
		mav.put(AUTHENTICATION_CONFIG, authenticationConfig);
		mav.put(USER_LIST, userList);
		return mav;
	}
	
	@Handler
	private Mav createUser() {
		Mav mav = new Mav("/jsp/authentication/createUserForm.jsp");
		mav.put(AUTHENTICATION_CONFIG, authenticationConfig);
		mav.put(DATAROUTER_USER_ROLES, DatarouterUserRole.getPermissibleRolesForUser(getCurrentUser(), false));
		return mav;
	}
	
	@Handler
	private Mav createUserSubmit() {

		DatarouterUser currentUser = getCurrentUser();
		if(!DatarouterUserRole.isUserAdmin(currentUser)) {
			handleInvalidRequest();
		}
		
		String username = params.required(authenticationConfig.getUsernameParam());
		String password = params.required(authenticationConfig.getPasswordParam());
		String[] userRoles = params.getRequest().getParameterValues(authenticationConfig.getUserRolesParam());
		boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), true);
		
		Long id = RandomTool.nextPositiveLong();
		String userToken = DatarouterTokenGenerator.generateRandomToken();
		String passwordSalt = passwordService.generateSaltForNewUser();
		String passwordDigest = passwordService.digest(passwordSalt, password);
		Set<DatarouterUserRole> userRolesSet = getAllowedUserRoles(currentUser, userRoles, false);
		
		String apiKey = passwordService.generateSaltForNewUser();
		boolean apiEnabled = params.optionalBoolean(authenticationConfig.getApiEnabledParam(), true);
		
		DatarouterUser user = DatarouterUser.create(
				id, userToken, username, passwordSalt, passwordDigest, userRolesSet, apiKey);
		user.setEnabled(enabled);
		user.setApiEnabled(apiEnabled);
		
		assertUserDoesNotExist(id, userToken, username, apiKey);
		userNodes.getUserNode().put(user, null);
		
		return handleDefault(); 
	}
	
	@Handler
	private Mav editUser() {
		Mav mav = new Mav("/jsp/authentication/editUserForm.jsp");
		
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);
		boolean isSelf = userToEdit.equals(currentUser);

		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		mav.put(USER, userToEdit);
		mav.put(AUTHENTICATION_CONFIG, authenticationConfig);
		mav.put(DATAROUTER_USER_ROLES, DatarouterUserRole.getPermissibleRolesForUser(currentUser, isSelf));
		mav.put(USER_ROLES, userToEdit.getRoles());
		
		return mav;
	}

	@Handler
	private Mav editUserSubmit() {
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);
		Boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), false);
		Boolean apiEnabled = params.optionalBoolean(authenticationConfig.getApiEnabledParam(), false);
		String[] userRoles = params.getRequest().getParameterValues(authenticationConfig.getUserRolesParam());
		boolean isSelf = currentUser.equals(userToEdit);
		
		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		userToEdit.setEnabled(enabled);
		userToEdit.setApiEnabled(apiEnabled);
		userToEdit.setRoles(getAllowedUserRoles(currentUser, userRoles, isSelf));
		
		userNodes.getUserNode().put(userToEdit, null);
		
		return new GlobalRedirectMav(params.getContextPath());
	}
	
	@Handler
	private Mav resetPassword() {
		Mav mav = new Mav("/jsp/authentication/resetPasswordForm.jsp");
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);
		
		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		mav.put(USER, userToEdit);
		mav.put(AUTHENTICATION_CONFIG, authenticationConfig);
		
		return mav;
	}
	
	@Handler
	private Mav resetPasswordSubmit() {
		String password = params.required(authenticationConfig.getPasswordParam());
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);
		
		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		passwordService.updateUserPassword(userToEdit, password);
		
		return handleDefault();
	}
	
	@Handler
	private Mav resetUserApiKey() {
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);

		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		String newApiKey = passwordService.generateSaltForNewUser();
		userToEdit.setApiKey(newApiKey);
		userNodes.getUserNode().put(userToEdit, null);
		
		return handleDefault();
	}
	
	/***************** helpers **********************/
	
	private void handleInvalidRequest() {
		ResponseTool.sendError(response, 403, "invalid request");
	}
	
	private boolean canEditUser(DatarouterUser userToEdit, DatarouterUser currentUser) {
		return userToEdit.equals(currentUser)
				|| !userToEdit.getRoles().contains(DatarouterUserRole.datarouterAdmin)
				&& DatarouterUserRole.isUserAdmin(currentUser)
				&& currentUser.isEnabled();
	}
	
	private Set<DatarouterUserRole> getAllowedUserRoles(DatarouterUser currentUser, String[] userRoleStrings,
			boolean isSelf) {
		Set<DatarouterUserRole> userRoles = DatarouterUserRole.fromStringArray(userRoleStrings);
		Set<DatarouterUserRole> validRoles = DatarouterUserRole.getPermissibleRolesForUser(currentUser, isSelf);
		userRoles.retainAll(validRoles);
		return userRoles;
	}
	
	private DatarouterUser getUserById(Long id) {
		return userNodes.getUserNode().get(new DatarouterUserKey(id), null);
	}
	
	private DatarouterUser getCurrentUser() {
		DatarouterSession session = params.getSession();
		if (session == null) {
			return null;
		}
		return getUserById(session.getUserId());
	}
	
	private void assertUserDoesNotExist(Long id, String userToken, String username, String apiKey) {
		DatarouterUser userWithId = getUserById(id);
		if (userWithId != null) {
			throw new IllegalArgumentException("DatarouterUser already exists with id=" + id);
		}
		DatarouterUser userWithUserToken = userNodes.getUserNode().lookupUnique(
				new DatarouterUserByUserTokenLookup(userToken), null);
		if (userWithUserToken != null) {
			throw new IllegalArgumentException("DatarouterUser already exists with userToken=" + userToken);
		}
		DatarouterUser userWithEmail = userNodes.getUserNode().lookupUnique(
				new DatarouterUserByUsernameLookup(username), null);
		if (userWithEmail != null) {
			throw new IllegalArgumentException("DatarouterUser already exists with username=" + username);
		}
		DatarouterUser userWithApiKey = userNodes.getUserNode().lookupUnique(
				new DatarouterUserByApiKeyLookup(apiKey), null);
		if (userWithApiKey != null) {
			throw new IllegalArgumentException("DatarouterUser already exists with apiKey=" + apiKey);
		}
	}
}
