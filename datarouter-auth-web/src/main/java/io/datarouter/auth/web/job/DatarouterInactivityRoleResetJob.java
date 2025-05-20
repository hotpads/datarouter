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
package io.datarouter.auth.web.job;

import java.util.Set;

import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.DatarouterUserEditService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.web.config.DatarouterAuthSettingRoot;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.types.MilliTime;
import jakarta.inject.Inject;

public class DatarouterInactivityRoleResetJob extends BaseJob{

	@Inject
	private DatarouterAuthSettingRoot settings;
	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private DatarouterUserEditService userEditService;
	@Inject
	private RoleManager roleManager;

	@Override
	public void run(TaskTracker tracker){
		userDao.scan()
				.include(DatarouterUser::isEnabled)
				.include(user -> user.getLastLoggedInMs() == null
						|| user.getLastLoggedInMs().isBefore(
								MilliTime.now().minus(settings.inactivityRoleResetDuration.get().toJavaDuration())))
				.exclude(user -> roleManager.getDefaultRoles().equals(Set.copyOf(user.getRolesIgnoreSaml())))
				.forEach(userEditService::resetRoles);
	}

}
