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
package io.datarouter.auth.config;

import javax.inject.Singleton;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.httpclient.path.PathsRoot;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.DatarouterWebPaths.PermissionRequestPaths;
import io.datarouter.web.config.DatarouterWebPaths.SigninPaths;

@Singleton
public class DatarouterAuthPaths extends PathNode implements PathsRoot{

	private static final DatarouterWebPaths WEB_PATHS = new DatarouterWebPaths();

	public final AdminPaths admin = branch(AdminPaths::new, "admin");
	public final PermissionRequestPaths permissionRequest = WEB_PATHS.permissionRequest;
	public final SigninPaths signin = WEB_PATHS.signin;
	public final SignupPaths signup = branch(SignupPaths::new, "signup");

	public final PathNode docs = leaf("docs");
	public final PathNode home = leaf("");
	public final PathNode resetPassword = WEB_PATHS.resetPassword;
	public final PathNode resetPasswordSubmit = WEB_PATHS.resetPasswordSubmit;
	public final PathNode signout = leaf("signout");

	public static class AdminPaths extends PathNode{
		public final PathNode accounts = leaf("accounts");
		public final PathNode createUser = leaf("createUser");
		public final PathNode createUserSubmit = leaf("createUserSubmit");
		public final PathNode editUser = leaf("editUser");
		public final PathNode editUserSubmit = leaf("editUserSubmit");
		public final PathNode listUsers = leaf("listUsers");
		public final PathNode viewUsers = leaf("viewUsers");
	}

	public static class SignupPaths extends PathNode{
		public final PathNode submit = leaf("submit");
	}

}
