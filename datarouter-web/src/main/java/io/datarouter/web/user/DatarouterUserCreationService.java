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
package io.datarouter.web.user;

import java.util.Date;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.collection.SetTool;
import io.datarouter.util.number.RandomTool;
import io.datarouter.web.user.authenticate.DatarouterTokenGenerator;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.util.PasswordTool;

@Singleton
public class DatarouterUserCreationService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserCreationService.class);

	public static final long ADMIN_ID = 1L;

	private static final Set<Role> DEFAULT_ADMIN_ROLES = SetTool.of(
			DatarouterUserRole.DATAROUTER_ADMIN.getRole(),
			DatarouterUserRole.ADMIN.getRole(),
			DatarouterUserRole.USER.getRole(),
			DatarouterUserRole.API_USER.getRole());

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterUserHistoryService userHistoryService;

	/*---------------- creation methods, helpers, and enum ------------------*/

	public void createFirstAdminUser(String defaultPassword){
		DatarouterUser user = new DatarouterUser();
		populateGeneratedFields(user, CreateType.ADMIN, defaultPassword);
		populateManualFields(user, datarouterProperties.getAdministratorEmail(), DEFAULT_ADMIN_ROLES, true);
		finishCreate(user, ADMIN_ID, "Automatically created admin user.");
		logger.warn("Created default admin user account");
	}

	/**
	 * This should be used for tests where particular persistence timing and no history are required
	 */
	public DatarouterUser createAutomaticUserWithoutPersist(String username, String description, Set<Role> roles){
		return createAutomaticUser(username, description, roles, false);
	}

	public DatarouterUser createAutomaticUser(String username, String description){
		return createAutomaticUser(username, description, Set.of(DatarouterUserRole.REQUESTOR.getRole()));
	}

	public DatarouterUser createAutomaticUser(String username, String description, Set<Role> roles){
		return createAutomaticUser(username, description, roles, true);
	}

	private DatarouterUser createAutomaticUser(String username, String description, Set<Role> roles,
			boolean shouldPersist){
		roles.add(DatarouterUserRole.REQUESTOR.getRole());
		DatarouterUser user = new DatarouterUser();
		populateGeneratedFields(user, CreateType.AUTO, null);
		populateManualFields(user, username, roles, true);
		if(shouldPersist){
			return finishCreate(user, ADMIN_ID, description);
		}
		return user;
	}

	public DatarouterUser createManualUser(DatarouterUser creator, String username, String password,
			String[] requestedRoles, boolean enabled){
		DatarouterUser user = new DatarouterUser();
		populateGeneratedFields(user, CreateType.MANUAL, password);
		populateManualFields(user, username, datarouterUserService.getAllowedUserRoles(creator, requestedRoles),
				enabled);
		return finishCreate(user, creator.getId(), "User manually created by " + creator.getUsername());
	}

	private void populateGeneratedFields(DatarouterUser user, CreateType type, String password){
		user.getKey().setId(type == CreateType.ADMIN ? ADMIN_ID : RandomTool.nextPositiveLong());
		user.setUserToken(DatarouterTokenGenerator.generateRandomToken());

		user.setCreated(new Date());
		user.setLastLoggedIn(type == CreateType.ADMIN ? user.getCreated() : null);

		//AUTO users have no passwords. ADMIN and MANUAL users do have passwords.
		user.setPasswordSalt(type == CreateType.AUTO ? null : PasswordTool.generateSalt());
		user.setPasswordDigest(type == CreateType.AUTO ? null : PasswordTool.digest(user.getPasswordSalt(), password));
	}

	private void populateManualFields(DatarouterUser user, String username, Set<Role> roles, Boolean enabled){
		user.setUsername(username);
		user.setRoles(roles);
		user.setEnabled(enabled);
	}

	private DatarouterUser finishCreate(DatarouterUser user, Long editorId, String description){
		datarouterUserService.assertUserDoesNotExist(user.getId(), user.getUserToken(), user.getUsername());
		userHistoryService.recordCreate(user, editorId, description);
		return user;
	}

	private static enum CreateType{
		ADMIN, AUTO, MANUAL;
	}

}
