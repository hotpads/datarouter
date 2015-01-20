package com.hotpads.handler.user.session;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.DatarouterPasswordService;
import com.hotpads.handler.user.authenticate.DatarouterTokenGenerator;
import com.hotpads.handler.user.role.DatarouterUserRole;

public class DataRouterUserService{
	private static final Logger logger = LoggerFactory.getLogger(DataRouterUserService.class);
	
	private static final String ADMIN_USERNAME =  "admin@hotpads.com";
	private static final String RAW_PW = "tempAdminPassword";
	
	//injected fields	
	private DatarouterUserNodes userNodes;
	private DatarouterPasswordService passwordService;
	
	@Inject
	public DataRouterUserService(DatarouterUserNodes userNodes, DatarouterPasswordService passwordService){
		this.userNodes = userNodes;
		this.passwordService = passwordService;
	}
	
	public void createAdminUser(long admin_id, Collection<DatarouterUserRole> roles){
		
		String salt = passwordService.generateSaltForNewUser();		
		String digest = passwordService.digest(salt, RAW_PW);		
		DatarouterUser user = new DatarouterUser();
		user.setCreated(new Date());
		user.setUsername(ADMIN_USERNAME);
		user.setEnabled(true);
		user.setId(admin_id);
		user.setLastLoggedIn(new Date());
		user.setPasswordDigest(digest);
		user.setPasswordSalt(salt);
		user.setRoles(roles);
		user.setUserToken(DatarouterTokenGenerator.generateRandomToken());		
		userNodes.getUserNode().put(user, null);
		logger.warn("Created default admin user account");		
	}

}
