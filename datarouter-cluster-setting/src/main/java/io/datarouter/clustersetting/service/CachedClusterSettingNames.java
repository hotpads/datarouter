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
package io.datarouter.clustersetting.service;

import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.SettingRoot.SettingRootFinder;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.cached.Cached;

@Singleton
public class CachedClusterSettingNames extends Cached<TreeSet<String>>{

	private final SettingRootFinder settingRootFinder;

	@Inject
	public CachedClusterSettingNames(SettingRootFinder settingRootFinder){
		super(1, TimeUnit.MINUTES);
		this.settingRootFinder = settingRootFinder;
	}

	@Override
	protected TreeSet<String> reload(){
		return settingRootFinder.getRootNodesSortedByShortName().stream()
				.flatMap(this::getSettingsNames)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	private Stream<String> getSettingsNames(SettingNode settingNode){
		String currentSettingName = settingNode.getName();
		String trimmmedSettingName = currentSettingName.substring(0, currentSettingName.length() - 1);
		Stream<String> currentNodesSettingsNames = settingNode.getSettings().values().stream()
				.map(CachedSetting::getName);
		Stream<String> childrenNodesSettingsNames = settingNode.getChildren().values().stream()
				.flatMap(this::getSettingsNames);
		return Stream.of(Stream.of(trimmmedSettingName), currentNodesSettingsNames, childrenNodesSettingsNames)
				.flatMap(Function.identity());
	}

}
