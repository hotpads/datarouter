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

import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao;
import io.datarouter.auth.storage.user.session.DatarouterSession;
import io.datarouter.auth.storage.user.session.DatarouterSessionKey;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.vacuum.DatabeanVacuum;
import io.datarouter.storage.vacuum.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import jakarta.inject.Inject;

public class DatarouterSessionVacuumJob extends BaseJob{

	@Inject
	private DatarouterAuthenticationConfig datarouterAuthenticationConfig;
	@Inject
	private BaseDatarouterSessionDao dao;

	@Override
	public void run(TaskTracker tracker){
		makeVacuum().run(tracker);
	}

	private DatabeanVacuum<DatarouterSessionKey,DatarouterSession> makeVacuum(){
		return new DatabeanVacuumBuilder<>(
				dao.scan(),
				datarouterAuthenticationConfig::isSessionExpired,
				dao::deleteMulti)
				.build();
	}

}
