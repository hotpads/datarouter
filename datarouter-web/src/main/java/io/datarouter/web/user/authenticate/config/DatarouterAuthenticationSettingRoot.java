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
package io.datarouter.web.user.authenticate.config;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.profile.ConfigProfile;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.util.duration.DatarouterDuration;

@Singleton
public class DatarouterAuthenticationSettingRoot extends SettingRoot{

	public final Setting<DatarouterDuration> userTokenTimeoutDuration;
	public final Setting<DatarouterDuration> sessionTokenTimeoutDuration;

	@Inject
	public DatarouterAuthenticationSettingRoot(SettingFinder finder){
		super(finder, "datarouterAuth.");

		userTokenTimeoutDuration = registerDuration("userTokenTimeoutDuration", new DatarouterDuration(365,
				TimeUnit.DAYS));
		sessionTokenTimeoutDuration = registerDurations("sessionTokenTimeoutDuration",
				defaultTo(new DatarouterDuration(30, TimeUnit.MINUTES))
				.with(ConfigProfile.DEVELOPMENT, new DatarouterDuration(Long.MAX_VALUE, TimeUnit.HOURS)));
	}

}
