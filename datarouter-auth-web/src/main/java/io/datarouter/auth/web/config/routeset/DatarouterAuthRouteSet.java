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
package io.datarouter.auth.web.config.routeset;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.auth.web.web.DatarouterPermissionRequestHandler;
import io.datarouter.auth.web.web.DatarouterSigninHandler;
import io.datarouter.auth.web.web.DatarouterSignoutHandler;
import io.datarouter.auth.web.web.adminedituser.AdminEditUserHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAuthRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterAuthRouteSet(DatarouterAuthPaths paths){
		handle(paths.signin)
				.withHandler(DatarouterSigninHandler.class)
				.allowAnonymous();

		handle(paths.signout)
				.withHandler(DatarouterSignoutHandler.class)
				.allowAnonymous();

		handle(paths.admin.copyUser).withHandler(AdminEditUserHandler.class);
		handle(paths.admin.createUser).withHandler(AdminEditUserHandler.class);
		handle(paths.admin.createUserSubmit).withHandler(AdminEditUserHandler.class);
		handle(paths.admin.editAccounts).withHandler(AdminEditUserHandler.class);
		handle(paths.admin.editRoles)
				.withHandler(AdminEditUserHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.REQUESTOR);
		handle(paths.admin.editUser).withHandler(AdminEditUserHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.REQUESTOR);
		handle(paths.admin.getAllRoles).withHandler(AdminEditUserHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.USER);
		handle(paths.admin.getIsSamlEnabled).withHandler(AdminEditUserHandler.class);
		handle(paths.admin.getUserDetails).withHandler(AdminEditUserHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.REQUESTOR);
		handle(paths.admin.getUserProfileImage).withHandler(AdminEditUserHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.REQUESTOR);
		handle(paths.admin.listUsers).withHandler(AdminEditUserHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.USER);
		handle(paths.admin.updatePassword).withHandler(AdminEditUserHandler.class);
		handle(paths.admin.updateTimeZone).withHandler(AdminEditUserHandler.class);
		handle(paths.admin.viewUsers).withHandler(AdminEditUserHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.USER);

		handleDir(paths.permissionRequest)
				.withHandler(DatarouterPermissionRequestHandler.class)
				.allowRoles(DatarouterUserRoleRegistry.REQUESTOR);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRoleRegistry.DATAROUTER_ADMIN)
				.withTag(Tag.DATAROUTER);
	}

}
