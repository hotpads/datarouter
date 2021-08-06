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
package io.datarouter.storage.setting;

import java.util.List;
import java.util.Optional;

import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.setting.cached.CachedSetting;

public interface SettingFinder{

	String getEnvironmentType();
	String getEnvironmentName();
	ServerType getServerType();
	String getServerName();
	List<DatarouterSettingTag> getSettingTags();
	Optional<String> getSettingValue(String name);
	List<String> getAllCustomSettingValues(String name);
	void registerCachedSetting(CachedSetting<?> setting);
	void validateAllCachedSettings();

}
