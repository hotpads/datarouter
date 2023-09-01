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
package io.datarouter.web.config;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.datarouter.auth.config.DatarouterAuthenticationSettings;
import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.config.settings.DatarouterLocalhostCorsFilterSettings;
import io.datarouter.web.config.settings.DatarouterSchemaUpdateEmailSettings;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebSettingRoot extends SettingRoot{

	private static final int ONE_MB = 1_048_576;

	public final Setting<Integer> maxCacheableContentLength;
	public final CachedSetting<String> shutdownSecret;
	public final CachedSetting<Set<String>> stackTraceHighlights;
	public final Setting<Boolean> saveLatencyGauges;
	public final Setting<DatarouterDuration> keepAliveTimeout;
	public final CachedSetting<Boolean> httpWarmup;
	public final Setting<Integer> httpWarmupIteration;

	@Inject
	public DatarouterWebSettingRoot(
			SettingFinder finder,
			DatarouterAuthenticationSettings authenticationSettings,
			DatarouterSamlSettings samlSettings,
			DatarouterSchemaUpdateEmailSettings schemaUpdateEmailSettings,
			DatarouterLocalhostCorsFilterSettings localhostCorsFilterSettings){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterWeb.");

		registerChild(authenticationSettings);
		registerChild(samlSettings);
		registerChild(schemaUpdateEmailSettings);
		registerChild(localhostCorsFilterSettings);

		maxCacheableContentLength = registerInteger("maxCacheableContentLength", ONE_MB);
		shutdownSecret = registerString("shutdownSecret", "");
		stackTraceHighlights = registerCommaSeparatedString("stackTraceHighlights", Set.of("io.datarouter"));
		saveLatencyGauges = registerBoolean("saveLatencyGauges", true);
		keepAliveTimeout = registerDuration("keepAliveTimeout", new DatarouterDuration(0, TimeUnit.MINUTES));
		httpWarmup = registerBoolean("httpWarmup", false);
		httpWarmupIteration = registerInteger("httpWarmupIteration", 5_000);
	}

}
