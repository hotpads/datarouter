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
package io.datarouter.auth.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.DatarouterWebPaths.PermissionRequestPaths;
import io.datarouter.web.config.DatarouterWebPaths.SigninPaths;

@Singleton
public class DatarouterAuthPaths extends PathNode implements PathsRoot{

	private static final DatarouterWebPaths WEB_PATHS = new DatarouterWebPaths();

	public final AdminPaths admin = branch(AdminPaths::new, "admin");
	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public final UserDeprovisioningPaths userDeprovisioning = branch(UserDeprovisioningPaths::new,
			"userDeprovisioning");
	public final PermissionRequestPaths permissionRequest = WEB_PATHS.permissionRequest;
	public final SigninPaths signin = WEB_PATHS.signin;
	public final SignupPaths signup = branch(SignupPaths::new, "signup");

	public final PathNode deprovisionedUsers = leaf("deprovisionedUsers");

	public final PathNode docs = leaf("docs");
	public final PathNode home = leaf("");
	public final PathNode signout = leaf("signout");

	public static class DatarouterPaths extends PathNode{
		public final AccountsPaths accounts = branch(AccountsPaths::new, "accounts");
		public final AccountManagerPaths accountManager = branch(AccountManagerPaths::new, "accountManager");
	}

	public static class AccountsPaths extends PathNode{
		public final PathNode renameAccounts = leaf("renameAccounts");
		public final PathNode updateCallerType = leaf("updateCallerType");
	}

	public static class AccountManagerPaths extends PathNode{
		public final PathNode index = leaf("index");
		public final PathNode list = leaf("list");
		public final PathNode getDetails = leaf("getDetails");
		public final PathNode add = leaf("add");
		public final PathNode toggleUserMappings = leaf("toggleUserMappings");
		public final PathNode delete = leaf("delete");
		public final PathNode lookupAccount = leaf("lookupAccount");
		public final PathNode addCredential = leaf("addCredential");
		public final PathNode deleteCredential = leaf("deleteCredential");
		public final PathNode addSecretCredential = leaf("addSecretCredential");
		public final PathNode deleteSecretCredential = leaf("deleteSecretCredential");
		public final PathNode setCredentialActivation = leaf("setCredentialActivation");
		public final PathNode getAvailableEndpoints = leaf("getAvailableEndpoints");
		public final PathNode addPermission = leaf("addPermission");
		public final PathNode deletePermission = leaf("deletePermission");
		public final PathNode isServerTypeDev = leaf("isServerTypeDev");
		public final PathNode updateReferrer = leaf("updateReferrer");
	}

	public static class AdminPaths extends PathNode{
		public final PathNode createUser = leaf("createUser");
		public final PathNode createUserSubmit = leaf("createUserSubmit");
		public final PathNode editUser = leaf("editUser");
		public final PathNode getUserDetails = leaf("getUserDetails");
		public final PathNode getUserProfileImage = leaf("getUserProfileImage");
		public final PathNode listUsers = leaf("listUsers");
		public final PathNode updateUserDetails = leaf("updateUserDetails");
		public final PathNode updatePassword = leaf("updatePassword");
		public final PathNode copyUser = leaf("copyUser");
		public final PathNode viewUsers = leaf("viewUsers");
	}

	public static class SignupPaths extends PathNode{
		public final PathNode submit = leaf("submit");
	}

	public static class UserDeprovisioningPaths extends PathNode{
		public final PathNode deprovisionUsers = leaf("deprovisionUsers");
		public final PathNode restoreUsers = leaf("restoreUsers");
	}

}
