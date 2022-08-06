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
package io.datarouter.auth.job;

import java.time.Instant;
import java.time.Period;
import java.util.Objects;

import javax.inject.Inject;

import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestKey;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.web.user.databean.DatarouterUserKey;

public class DatarouterPermissionRequestVacuumJob extends BaseJob{

	private static final String CHANGELOG_TYPE = "Permission Request";

	@Inject
	private AdminEmail adminEmail;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DatarouterPermissionRequestDao permissionRequestDao;

	@Override
	public void run(TaskTracker tracker){
		Scanner<DatarouterPermissionRequest> permissionRequests = permissionRequestDao.scanOpenPermissionRequests();
		Instant tooOldCutoff = Instant.now().minus(Period.ofDays(30));
		permissionRequests
			.include(permissionRequest -> permissionRequest.getKey().getRequestTime().isBefore(tooOldCutoff))
			.map(DatarouterPermissionRequest::getKey)
			.map(DatarouterPermissionRequestKey::getUserId)
			.each(permissionRequestDao::declineAll)
			.map(DatarouterUserKey::new)
			.map(datarouterUserDao::get)
			.include(Objects::nonNull)
			.map(user -> new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				"Auto Decline",
				adminEmail.get())
				.withComment(user.getUsername() + "'s permission request expired")
				.build())
			.forEach(changelogRecorder::record);
	}

}
