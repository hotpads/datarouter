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

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAuthPaths extends PathNode implements PathsRoot{

	public final AdminPaths admin = branch(AdminPaths::new, "admin");
	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public final UserDeprovisioningPaths userDeprovisioning = branch(UserDeprovisioningPaths::new,
			"userDeprovisioning");
	public final PermissionRequestPaths permissionRequest = branch(PermissionRequestPaths::new, "permissionRequest");
	public final SigninPaths signin = branch(SigninPaths::new, "signin");

	public final DocsPaths docs = branch(DocsPaths::new, "docs");
	public final PathNode home = leaf("");
	public final PathNode signout = leaf("signout");

	public static class DatarouterPaths extends PathNode{
		public final AccountsPaths accounts = branch(AccountsPaths::new, "accounts");
		public final AccountManagerPaths accountManager = branch(AccountManagerPaths::new, "accountManager");
		public final RoleRequirements roleRequirements = branch(RoleRequirements::new, "roleRequirements");
	}

	public static class DocsPaths extends PathNode{
		public final PathNode getCsrfIv = leaf("getCsrfIv");
		public final PathNode getSignature = leaf("getSignature");
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
		public final PathNode delete = leaf("delete");
	}

	public static class AdminPaths extends PathNode{
		public final PathNode copyUser = leaf("copyUser");
		public final PathNode createUser = leaf("createUser");
		public final PathNode createUserSubmit = leaf("createUserSubmit");
		public final PathNode editAccounts = leaf("editAccounts");
		public final PathNode editRoles = leaf("editRoles");
		public final PathNode editUser = leaf("editUser");
		public final PathNode getAllRoles = leaf("getAllRoles");
		public final PathNode getIsSamlEnabled = leaf("getIsSamlEnabled");
		public final PathNode getUserDetails = leaf("getUserDetails");
		public final PathNode getUserProfileImage = leaf("getUserProfileImage");
		public final PathNode listUsers = leaf("listUsers");
		public final PathNode updatePassword = leaf("updatePassword");
		public final PathNode updateTimeZone = leaf("updateTimeZone");
		public final PathNode viewUsers = leaf("viewUsers");
	}

	public static class UserDeprovisioningPaths extends PathNode{
		public final PathNode deprovisionUsers = leaf("deprovisionUsers");
		public final PathNode restoreUsers = leaf("restoreUsers");
	}

	public static class PermissionRequestPaths extends PathNode{
		public final PathNode createCustomPermissionRequest = leaf("createCustomPermissionRequest");
		public final PathNode declineAll = leaf("declineAll");
		public final PathNode declinePermissionRequests = leaf("declinePermissionRequests");
		public final PathNode showForm = leaf("showForm");
		public final PathNode submit = leaf("submit");
	}

	public static class SigninPaths extends PathNode{
		public final PathNode submit = leaf("submit");
	}

	public static class RoleRequirements extends PathNode{
		public final PathNode getRequiredRolesByAfterContextPath = leaf("getRequiredRolesByAfterContextPath");
	}

}
