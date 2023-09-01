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
package io.datarouter.webappinstance.config;

import java.util.concurrent.TimeUnit;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebappInstanceSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> alertOnStaleWebappInstance;
	public final CachedSetting<DatarouterDuration> staleWebappInstanceThreshold;

	public final CachedSetting<Boolean> runWebappInstanceVacuumJob;
	public final CachedSetting<Boolean> runDeadClusterJobLockVacuumJob;

	public final CachedSetting<Boolean> webappInstancePublisher;

	@Inject
	public DatarouterWebappInstanceSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterWebappInstance.");

		alertOnStaleWebappInstance = registerBoolean("alertOnStaleWebappInstance", false);
		staleWebappInstanceThreshold = registerDuration("staleWebappInstanceThreshold",
				new DatarouterDuration(7, TimeUnit.DAYS));

		runWebappInstanceVacuumJob = registerBoolean("runWebappInstanceVacuumJob", false);
		runDeadClusterJobLockVacuumJob = registerBoolean("runDeadClusterJobLockVacuumJob", false);

		webappInstancePublisher = registerBoolean("webappInstancePublisher", false);
	}

}
