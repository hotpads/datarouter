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
package io.datarouter.joblet.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;

public class BaseDatarouterJobletThreadCountSettings extends SettingNode{

	private final Map<JobletType<?>,CachedSetting<Integer>> settingByJobletType = new HashMap<>();

	public BaseDatarouterJobletThreadCountSettings(
			SettingFinder finder,
			JobletTypeFactory jobletTypeFactory,
			String nodeName,
			int defaultNumThreads){
		super(finder, "datarouterJoblet." + nodeName + ".");

		for(JobletType<?> jobletType : jobletTypeFactory.getAllTypes()){
			CachedSetting<Integer> setting = registerJobletSetting(
					jobletType,
					jobletType.getPersistentString(),
					defaultNumThreads);
			settingByJobletType.put(jobletType, setting);
		}
	}

	public CachedSetting<Integer> registerJobletSetting(JobletType<?> jobletType, String name, Integer defaultValue){
		CachedSetting<Integer> setting = registerInteger(name, defaultValue);
		settingByJobletType.put(jobletType, setting);
		return setting;
	}

	public CachedSetting<Integer> getSettingForJobletType(JobletType<?> type){
		return settingByJobletType.get(type);
	}

	public int getCountForJobletType(JobletType<?> type){
		return Optional.ofNullable(settingByJobletType.get(type).get()).orElse(0);
	}

}
