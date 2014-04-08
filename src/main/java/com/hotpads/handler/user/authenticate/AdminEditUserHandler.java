package com.hotpads.handler.user.authenticate;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.DatarouterUser;
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
		return viewUsers();
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
		mav.put(DATAROUTER_USER_ROLES, DatarouterUserRole.getPermissibleRolesForUser(getCurrentUser()));
		return mav;
	}
	
	@Handler
	private Mav createUserSubmit() throws Exception {

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
		Set<DatarouterUserRole> userRolesSet = getAllowedUserRoles(currentUser, userRoles);
		
		String apiKey = passwordService.generateSaltForNewUser();
		boolean apiEnabled = params.optionalBoolean(authenticationConfig.getApiEnabledParam(), true);
		
		DatarouterUser user = DatarouterUser.create(
				id, userToken, username, passwordSalt, passwordDigest, userRolesSet, apiKey);
		user.setEnabled(enabled);
		user.setApiEnabled(apiEnabled);
		
		
		assertUserDoesNotExist(id, userToken, username);
		userNodes.getUserNode().put(user, null);
		
		return redirectMav(viewUsers());
	}
	
	@Handler
	private Mav editUser() throws Exception {
		Mav mav = new Mav("/jsp/authentication/editUserForm.jsp");
		
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);

		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		mav.put(USER, userToEdit);
		mav.put(AUTHENTICATION_CONFIG, authenticationConfig);
		mav.put(DATAROUTER_USER_ROLES, DatarouterUserRole.getPermissibleRolesForUser(currentUser));
		mav.put(USER_ROLES, userToEdit.getRoles());
		
		return mav;
	}

	@Handler
	private Mav editUserSubmit() throws Exception {
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);
		Boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), false);
		String[] userRoles = params.getRequest().getParameterValues(authenticationConfig.getUserRolesParam());

		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		userToEdit.setEnabled(enabled);
		userToEdit.setRoles(getAllowedUserRoles(currentUser, userRoles));
		
		userNodes.getUserNode().put(userToEdit, null);

		return redirectMav(viewUsers());
	}
	
	@Handler
	private Mav resetPassword() throws Exception {
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
	private Mav resetPasswordSubmit() throws Exception {
		String password = params.required(authenticationConfig.getPasswordParam());
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser currentUser = getCurrentUser(), userToEdit = getUserById(userId);
		
		if(!canEditUser(userToEdit, currentUser)) {
			handleInvalidRequest();
		}
		
		passwordService.updateUserPassword(userToEdit, password);
		
		return redirectMav(editUser());
	}
	
	/***************** helpers **********************/

	private Mav redirectMav(Mav mav) {
		//TODO redirect to desired view, mav.setRedirect expects a URL, not view
		//mav.setRedirect(true);
		return mav;
	}
	
	private void handleInvalidRequest() throws Exception {
		//TODO add a service or authenticator to handle this automatically
		params.getResponse().sendError(403);
	}
	
	private boolean canEditUser(DatarouterUser userToEdit, DatarouterUser currentUser) {
		// datarouterAdmins can only be added/edited directly from the database
		// current user must be either admin or datarouterAdmin and enabled
		return !userToEdit.getRoles().contains(DatarouterUserRole.datarouterAdmin)
				&& DatarouterUserRole.isUserAdmin(currentUser)
				&& currentUser.isEnabled();
	}
	
	private Set<DatarouterUserRole> getAllowedUserRoles(DatarouterUser currentUser, String[] userRoleStrings) {
		Set<DatarouterUserRole> userRoles = DatarouterUserRole.fromStringArray(userRoleStrings);
		Set<DatarouterUserRole> validRoles = DatarouterUserRole.getPermissibleRolesForUser(currentUser);
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
	
	private void assertUserDoesNotExist(Long id, String userToken, String username) {
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
	}
}
