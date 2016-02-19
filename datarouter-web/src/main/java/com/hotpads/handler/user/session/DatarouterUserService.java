package com.hotpads.handler.user.session;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.DatarouterPasswordService;
import com.hotpads.handler.user.authenticate.DatarouterTokenGenerator;
import com.hotpads.handler.user.role.DatarouterUserRole;

public class DatarouterUserService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserService.class);

	public static final long ADMIN_ID = 1L;
	private static final String DEFAULT_PASSWORD = "tempAdminPassword";
	private static final List<DatarouterUserRole> DEFAULT_ADMIN_ROLES = DrListTool.create(
			DatarouterUserRole.datarouterAdmin, DatarouterUserRole.admin, DatarouterUserRole.user,
			DatarouterUserRole.apiUser);

	//injected fields
	private final DatarouterUserNodes userNodes;
	private final DatarouterPasswordService passwordService;
	private final Datarouter datarouter;

	@Inject
	public DatarouterUserService(DatarouterUserNodes userNodes, DatarouterPasswordService passwordService,
			Datarouter datarouter){
		this.userNodes = userNodes;
		this.passwordService = passwordService;
		this.datarouter = datarouter;
	}

	public void createAdminUser(){
		String salt = passwordService.generateSaltForNewUser();
		String digest = passwordService.digest(salt, DEFAULT_PASSWORD);
		DatarouterUser user = new DatarouterUser();
		user.setCreated(new Date());
		user.setUsername(datarouter.getAdministratorEmail());
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
