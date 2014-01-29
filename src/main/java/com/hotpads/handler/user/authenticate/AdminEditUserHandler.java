package com.hotpads.handler.user.authenticate;

import java.util.List;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUserTokenLookup;
import com.hotpads.handler.user.DatarouterUserKey;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.util.core.ListTool;
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
		return editUserForm();
	}
	
	@Handler
	private Mav editUserForm(){
		Mav mav = new Mav("/jsp/generic/adminEditUserForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}

	@Handler
	private Mav createUser(){
		Long id = RandomTool.nextPositiveLong();
		String userToken = DatarouterTokenGenerator.generateRandomToken();
		String username = params.required(authenticationConfig.getUsernameParam());
		String password = params.required(authenticationConfig.getPasswordParam());
		String passwordSalt = passwordService.generateSaltForNewUser();
		String passwordDigest = passwordService.digest(passwordSalt, password);
		List<DatarouterUserRole> roles = ListTool.wrap(DatarouterUserRole.user);
		DatarouterUser user = DatarouterUser.create(id, userToken, username, passwordSalt, passwordDigest, roles);
		assertUserDoesNotExist(id, userToken, username);
		userNodes.getUserNode().put(user, null);
		return new MessageMav("created DatarouterUser "+user.getUsername()+", userToken="+user.getUserToken());
	}
	
	private void assertUserDoesNotExist(Long id, String userToken, String username){
		DatarouterUser userWithId = userNodes.getUserNode().get(new DatarouterUserKey(id), null);
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
