/*
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
package io.datarouter.auth.web.service;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.authenticate.DatarouterTokenGenerator;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.util.PasswordTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.types.MilliTime;
import io.datarouter.util.number.RandomTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserCreationService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserCreationService.class);

	public static final long ADMIN_ID = 1L;
	public static final String SAML_USER_CREATION_DESCRIPTION = "SAML User";

	@Inject
	private AdminEmail adminEmail;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterUserHistoryService userHistoryService;
	@Inject
	private RoleManager roleManager;

	/*---------------- creation methods, helpers, and enum ------------------*/

	public void createFirstAdminUser(String defaultPassword){
		var user = new DatarouterUser();
		populateGeneratedFields(user, CreateType.ADMIN, defaultPassword, Optional.of(ZoneId.systemDefault()));
		populateManualFields(user, adminEmail.get(), roleManager.getSuperAdminRoles(), true);
		finishCreate(user, ADMIN_ID, adminEmail.get(), "Automatically created admin user.");
		logger.warn("Created default admin user account");
	}


	public DatarouterUser createAutomaticUser(String username, String description){
		var user = new DatarouterUser();
		populateGeneratedFields(user, CreateType.AUTO, null, Optional.empty());
		populateManualFields(user, username, roleManager.getDefaultRoles(), true);
		return finishCreate(user, ADMIN_ID, adminEmail.get(), description);
	}

	// Only for non-production environments. Bypasses normal approval requirements.
	public DatarouterUser createManualUser(
			DatarouterUser creator,
			String username,
			String password,
			Set<Role> requestedRoles,
			boolean enabled,
			Optional<ZoneId> zoneId,
			Optional<String> description){
		var user = new DatarouterUser();
		populateGeneratedFields(user, CreateType.MANUAL, password, zoneId);
		requestedRoles = Scanner.of(requestedRoles)
				.append(roleManager.getDefaultRoles()) // everyone should have these
				.collect(HashSet::new);
		populateManualFields(user, username, requestedRoles, enabled);
		String roleChangeString = user.getRolesIgnoreSaml().isEmpty()
				? "" : (": roles: " + List.of() + " => " + user.getRolesIgnoreSaml());
		String historyDescription =
				description.orElse("User manually created by " + creator.getUsername()) + roleChangeString;
		return finishCreate(user, creator.getId(), creator.getUsername(), historyDescription);
	}

	private void populateGeneratedFields(
			DatarouterUser user,
			CreateType type,
			String password,
			Optional<ZoneId> zoneId){
		user.getKey().setId(type == CreateType.ADMIN ? ADMIN_ID : RandomTool.nextPositiveLong());
		user.setUserToken(DatarouterTokenGenerator.generateRandomToken());

		user.setCreated(MilliTime.now());
		user.setLastLoggedIn(type == CreateType.ADMIN ? user.getCreated() : null);

		//AUTO users have no passwords. ADMIN and MANUAL users do have passwords.
		user.setPasswordSalt(type == CreateType.AUTO ? null : PasswordTool.generateSalt());
		String digest = type == CreateType.AUTO || password == null
				? null
				: PasswordTool.digest(user.getPasswordSalt(), password);
		user.setPasswordDigest(digest);

		// zoneId can be configured through the UI, fallback to system default
		user.setZoneId(zoneId.orElse(ZoneId.systemDefault()));
	}

	private void populateManualFields(DatarouterUser user, String username, Set<Role> roles, Boolean enabled){
		user.setUsername(username);
		user.setRoles(roles);
		user.setEnabled(enabled);
	}

	private DatarouterUser finishCreate(DatarouterUser user, Long editorId, String editorUsername, String description){
		datarouterUserService.assertUserDoesNotExist(user.getId(), user.getUserToken(), user.getUsername());
		userHistoryService.putAndRecordCreate(user, editorId, editorUsername, description);
		return user;
	}

	private enum CreateType{
		ADMIN, AUTO, MANUAL;
	}

}
