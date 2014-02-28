package com.hotpads.handler.user.authenticate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.BaseHandler.Handler.RequestMethod;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUserTokenLookup;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserKey;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.number.RandomTool;

public class AdminEditUserHandler extends BaseHandler{
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterPasswordService passwordService;
	@Inject
	private DatarouterUserNodes userNodes;

	@Override
	protected Mav handleDefault(){
		return viewUsers(null);
	}
	
	@Handler
	private Mav viewUsers(@Nullable String message) {
		Mav mav = new Mav("/jsp/authentication/viewUsers.jsp");
		List<DatarouterUser> userList = IterableTool.createArrayListFromIterable(userNodes.getUserNode().getAll(null));
		
		if(!StringTool.isNullOrEmptyOrWhitespace(message)) {
			mav.put("message", message);
		}
		
		mav.put("userList", userList);
		return mav;
	}
	
	@Handler(method = RequestMethod.GET)
	private Mav createUser() {
		Mav mav = new Mav("/jsp/authentication/createUserForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}
	
	@Handler(method = RequestMethod.POST)
	private Mav createUserSubmit() {
		return editUserSubmit();
	}
	
	@Handler(method = RequestMethod.GET)
	private Mav editUser(){
		Mav mav = new Mav("/jsp/authentication/editUserForm.jsp");
		
		DatarouterUser user = getUserById(params.requiredLong("userId"));
		
		mav.put("user", user);
		mav.put("authenticationConfig", authenticationConfig);
		mav.put("datarouterUserRolesList", DatarouterUserRole.values());

		Map<String, DatarouterUserRole> roleMap = MapTool.create();
		for(DatarouterUserRole role : user.getRoles()) {
			roleMap.put(role.name(), role);
		}
		mav.put("userRoleMap", roleMap);
		
		return mav;
	}

	@Handler(method = RequestMethod.POST)
	private Mav editUserSubmit(){
		String username = params.required(authenticationConfig.getUsernameParam());
		String password = params.required(authenticationConfig.getPasswordParam());
		String[] userRoles = params.getRequest().getParameterValues(authenticationConfig.getUserRolesParam());

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
		assertUserDoesNotExist(id, userToken, username);
		userNodes.getUserNode().put(user, null);
		
		return editUser();
	}
	
	@Handler(method = RequestMethod.GET)
	private Mav resetPassword() {
		Mav mav = new Mav("/jsp/authentication/resetPasswordForm.jsp");
		Long userId = params.requiredLong("userId");
		DatarouterUser user = getUserById(userId);
		
		mav.put("userId", userId);
		mav.put("user", user);
		
		return mav;
	}
	
	@Handler(method = RequestMethod.POST)
	private Mav resetPasswordSubmit() {
		String password = params.required(authenticationConfig.getPasswordParam());
		Long userId = params.requiredLong("userId");
		DatarouterUser user = getUserById(userId);
		
		
		return editUser();
	}
	
	private DatarouterUser getUserById(Long id) {
		return userNodes.getUserNode().get(new DatarouterUserKey(id), null);
	}
	
	private void assertUserDoesNotExist(Long id, String userToken, String username){
		DatarouterUser userWithId = getUserById(id);
		if(userWithId != null){
			throw new IllegalArgumentException("DatarouterUser already exists with id="+id);
		}
		DatarouterUser userWithUserToken = userNodes.getUserNode().lookupUnique(
				new DatarouterUserByUserTokenLookup(userToken), null);
		if(userWithUserToken != null){
			throw new IllegalArgumentException("DatarouterUser already exists with userToken="+userToken);
		}
		DatarouterUser userWithEmail = userNodes.getUserNode().lookupUnique(
				new DatarouterUserByUsernameLookup(username), null);
		if(userWithEmail != null){
			throw new IllegalArgumentException("DatarouterUser already exists with username="+username);
		}
	}
	
}
