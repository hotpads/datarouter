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
package io.datarouter.web.config;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.collection.SetTool;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationSettings;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;

@Singleton
public class DatarouterWebSettingRoot extends SettingRoot{

	private static final int ONE_MB = 1_048_576;

	public final Setting<Integer> maxCacheableContentLength;
	public final CachedSetting<String> shutdownSecret;
	public final CachedSetting<Set<String>> stackTraceHighlights;

	@Inject
	public DatarouterWebSettingRoot(
			SettingFinder finder,
			DatarouterStorageSettingRoot storageSettings,
			DatarouterAuthenticationSettings authenticationSettings,
			DatarouterEmailSettings emailSettings,
			DatarouterSamlSettings samlSettings){
		super(finder, "datarouterWeb.");

		dependsOn(storageSettings);

		registerChild(authenticationSettings);
		registerChild(emailSettings);
		registerChild(samlSettings);

		maxCacheableContentLength = registerInteger("maxCacheableContentLength", ONE_MB);
		shutdownSecret = registerString("shutdownSecret", "");
		stackTraceHighlights = registerCommaSeparatedString("stackTraceHighlights", SetTool.wrap("io.datarouter"));
	}

}