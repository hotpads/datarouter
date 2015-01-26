package com.hotpads.handler.user.session;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.DatarouterPasswordService;
import com.hotpads.handler.user.authenticate.DatarouterTokenGenerator;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.util.core.ListTool;

public class DatarouterUserService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserService.class);
	
	public static final long ADMIN_ID = 1L;
	private static final String ADMIN_USERNAME =  "admin@hotpads.com";
	private static final String DEFAULT_PASSWORD = "tempAdminPassword";	
	private static final List<DatarouterUserRole> DEFAULT_ADMIN_ROLES = ListTool.create(
			DatarouterUserRole.datarouterAdmin, DatarouterUserRole.admin, DatarouterUserRole.user, 
			DatarouterUserRole.apiUser);
	
	//injected fields	
	private DatarouterUserNodes userNodes;
	private DatarouterPasswordService passwordService;
	
	@Inject
	public DatarouterUserService(DatarouterUserNodes userNodes, DatarouterPasswordService passwordService){
		this.userNodes = userNodes;
		this.passwordService = passwordService;
	}
	
	public void createAdminUser(){		
		String salt = passwordService.generateSaltForNewUser();		
		String digest = passwordService.digest(salt, DEFAULT_PASSWORD);		
		DatarouterUser user = new DatarouterUser();
		user.setCreated(new Date());
		user.setUsername(ADMIN_USERNAME);
		user.setEnabled(true);
		user.setId(ADMIN_ID);
		user.setLastLoggedIn(new Date());
		user.setPasswordDigest(digest);
		user.setPasswordSalt(salt);
		user.setRoles(DEFAULT_ADMIN_ROLES);
		user.setUserToken(DatarouterTokenGenerator.generateRandomToken());		
		userNodes.getUserNode().put(user, null);
		logger.warn("Created default admin user account");		
	}

}
