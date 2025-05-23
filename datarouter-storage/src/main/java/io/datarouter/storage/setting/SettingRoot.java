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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.storage.setting.SettingCategory.SimpleSettingCategory;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class SettingRoot extends SettingNode implements PluginConfigValue<SettingRoot>{

	public static final PluginConfigKey<SettingRoot> KEY = new PluginConfigKey<>(
			"settingRoots",
			PluginConfigType.CLASS_LIST);

	private final Set<SettingRoot> rootNodes = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Map<SimpleSettingCategory,Set<SettingRoot>> map = Collections.synchronizedMap(new LinkedHashMap<>());
	private final SimpleSettingCategory category;

	public SettingRoot(SettingFinder finder, SettingCategory category, String name){
		super(finder, name);
		this.rootNodes.add(this);
		this.category = category.toSimpleSettingCategory();
	}

	private SettingRoot(
			SettingFinder finder,
			SettingRootsSupplier supplier,
			SettingCategory category,
			String name){
		super(finder, name);
		supplier.get().forEach(this::dependsOn);
		this.category = category.toSimpleSettingCategory();
	}

	private void dependsOn(SettingRoot settingNode){
		rootNodes.add(settingNode);
		settingNode.rootNodes.forEach(rootNodes::add);
		addToMap(settingNode);
	}

	public boolean isRecognized(String settingName){
		String rootName = StringTool.getStringBeforeFirstOccurrence('.', settingName);
		return isRecognizedRootName(rootName);
	}

	private boolean isRecognizedRootName(String rootNameWithoutTrailingDot){
		return rootNodes.stream()
				.map(SettingNode::getShortName)
				.anyMatch(shortName -> shortName.equals(rootNameWithoutTrailingDot));
	}

	private void addToMap(SettingRoot root){
		map.computeIfAbsent(root.category.toSimpleSettingCategory(), _ -> new LinkedHashSet<>()).add(root);
	}

	public Map<SimpleSettingCategory,Set<SettingRoot>> getRootNodesByCategory(){
		return map;
	}

	@Override
	public PluginConfigKey<SettingRoot> getKey(){
		return KEY;
	}

	@Singleton
	public static class SettingRootFinder extends SettingRoot{

		@Inject
		private SettingRootFinder(SettingFinder finder, SettingRootsSupplier settingRootsSupplier){
			super(finder, settingRootsSupplier, DatarouterSettingCategory.DATAROUTER, "datarouter.");
		}

	}

}
