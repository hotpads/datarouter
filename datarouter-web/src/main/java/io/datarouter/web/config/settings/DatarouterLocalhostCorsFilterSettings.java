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
package io.datarouter.web.config.settings;

import java.util.Set;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.impl.BooleanCachedSetting;
import io.datarouter.storage.setting.cached.impl.CommaSeparatedStringCachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterLocalhostCorsFilterSettings extends SettingNode{

	public final BooleanCachedSetting allowed;
	public final CommaSeparatedStringCachedSetting methods;
	public final CommaSeparatedStringCachedSetting headers;

	@Inject
	public DatarouterLocalhostCorsFilterSettings(SettingFinder finder){
		super(finder, "datarouterWeb.localhostCorsFilter.");

		allowed = registerBoolean("allowed", true);
		methods = registerCommaSeparatedString("methods", Set.of("POST", "GET", "OPTIONS", "DELETE"));
		headers = registerCommaSeparatedString("headers", Set.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
				HttpHeaders.ACCOUNT_NAME_HEADER));
	}

}
