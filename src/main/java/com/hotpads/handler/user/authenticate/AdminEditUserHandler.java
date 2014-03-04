package com.hotpads.handler.user.authenticate;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUserTokenLookup;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserKey;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.number.RandomTool;

public class AdminEditUserHandler extends BaseHandler{
	
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
		List<DatarouterUser> userList = IterableTool.createArrayListFromIterable(userNodes.getUserNode().getAll(null));
		mav.put("authenticationConfig", authenticationConfig);
		mav.put("userList", userList);
		return mav;
	}
	
	@Handler
	private Mav createUser() {
		Mav mav = new Mav("/jsp/authentication/createUserForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		mav.put("datarouterUserRoles", getDatarouterUserRoleNames());
		return mav;
	}
	
	@Handler
	private Mav createUserSubmit() {
		String username = params.required(authenticationConfig.getUsernameParam());
		String password = params.required(authenticationConfig.getPasswordParam());
		String[] userRoles = params.getRequest().getParameterValues(authenticationConfig.getUserRolesParam());
		boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), true);

		Set<DatarouterUserRole> userRolesSet = SetTool.wrap(DatarouterUserRole.user);
		for(String s : userRoles) {
			userRolesSet.add(DataRouterEnumTool.getEnumFromString(DatarouterUserRole.values(), s, DatarouterUserRole.user));
		}
		
		Long id = RandomTool.nextPositiveLong();
		String userToken = DatarouterTokenGenerator.generateRandomToken();
		String passwordSalt = passwordService.generateSaltForNewUser();
		String passwordDigest = passwordService.digest(passwordSalt, password);
		
		DatarouterUser user = DatarouterUser
				.create(id, userToken, username, passwordSalt, passwordDigest, userRolesSet);
		user.setEnabled(enabled);
		
		assertUserDoesNotExist(id, userToken, username);
		userNodes.getUserNode().put(user, null);
		
		return viewUsers();
	}
	
	@Handler
	private Mav editUser() {
		Mav mav = new Mav("/jsp/authentication/editUserForm.jsp");
		
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser user = getUserById(userId);
		
		mav.put("user", user);
		mav.put("authenticationConfig", authenticationConfig);
		mav.put("datarouterUserRoles", getDatarouterUserRoleNames());

		Set<String> userRoleSet = SetTool.create();
		for(DatarouterUserRole role : user.getRoles()) {
			userRoleSet.add(role.name());
		}
		mav.put("userRoleSet", userRoleSet);
		
		return mav;
	}

	@Handler
	private Mav editUserSubmit() {
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser user = getUserById(userId);
		Boolean enabled = params.optionalBoolean(authenticationConfig.getEnabledParam(), false);
		
		String[] userRoles = params.getRequest().getParameterValues(authenticationConfig.getUserRolesParam());
		Set<DatarouterUserRole> userRolesSet = SetTool.wrap(DatarouterUserRole.user);
		for(String s : userRoles) {
			userRolesSet.add(DataRouterEnumTool.getEnumFromString(DatarouterUserRole.values(), s, DatarouterUserRole.user));
		}
		
		user.setRoles(userRolesSet);
		user.setEnabled(enabled);
		
		userNodes.getUserNode().put(user, null);
		
		return viewUsers();
	}
	
	@Handler
	private Mav resetPassword() {
		Mav mav = new Mav("/jsp/authentication/resetPasswordForm.jsp");
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser user = getUserById(userId);
		
		mav.put("user", user);
		mav.put("authenticationConfig", authenticationConfig);
		
		return mav;
	}
	
	@Handler
	private Mav resetPasswordSubmit() {
		String password = params.required(authenticationConfig.getPasswordParam());
		Long userId = params.requiredLong(authenticationConfig.getUserIdParam());
		DatarouterUser user = getUserById(userId);

		passwordService.updateUserPassword(user, password);
		
		return editUser();
	}
	
	/***************** helpers **********************/
	
	private Set<String> getDatarouterUserRoleNames() {
		// TODO disable setting users of higher permissions than oneself ... cannot set datarouterAdmin
		Set<String> datarouterUserRolesSet = SetTool.create();
		for(DatarouterUserRole role : DatarouterUserRole.values()) {
			datarouterUserRolesSet.add(role.name());
		}
		return datarouterUserRolesSet;
	}
	
	private DatarouterUser getUserById(Long id) {
		return userNodes.getUserNode().get(new DatarouterUserKey(id), null);
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
