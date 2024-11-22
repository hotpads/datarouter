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
package io.datarouter.storage.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Module;

import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.dao.DaosModuleBuilder.SimpleDaosModule;
import io.datarouter.storage.setting.SettingRoot;

/**
 * Plugins are verbose wrappers around GuiceModules for easy installation of datarouter modules and Guice bindings. They
 * use a builder pattern to specify required and optional implementations of classes used by the module. Some modules
 * have their own extensions of BasePlugin which allows more features to get configured easily.
 *
 * This base class auto configures and binds cluster settings and dao registrations.
 *
 * Plugins are extensible. You can add a new {@link PluginConfigKey} which can be bound to PluginValues. A PluginValue
 * can be a single class, collection of classes, a single instance, or a collection of like instances.
 *
 * {@link PluginConfigValue} can be added across multiple modules without explicit inheritance, adding extensibility to
 * the framework.
 *
 * To retrieve what is bound to a PluginConfigKey, you can use the PluginInjector. The PluginInjector is a light weight
 * injector on top of the DatarouterInjector, which holds a map of keys and bound values.
 */
public abstract class BaseStoragePlugin extends BasePlugin{

	private DaosModuleBuilder daosModule = new SimpleDaosModule();

	protected void addSettingRoot(Class<? extends SettingRoot> settingRoot){
		addPluginEntry(SettingRoot.KEY, settingRoot);
	}

	protected void setDaosModule(DaosModuleBuilder daosModule){
		this.daosModule = daosModule;
	}

	public DaosModuleBuilder getDaosModuleBuilder(){
		return daosModule;
	}

	protected void setDaos(List<Class<? extends Dao>> daos){
		var daosModule = new SimpleDaosModule(daos);
		setDaosModule(daosModule);
	}

	/*------------------------- add Storage plugins -------------------------*/

	private final List<BaseStoragePlugin> storagePlugins = new ArrayList<>();

	protected void addStoragePlugin(BaseStoragePlugin storagePlugin){
		storagePlugins.add(storagePlugin);
	}

	public List<BaseStoragePlugin> getStoragePlugins(){
		return storagePlugins;
	}

	/*--------------------------- add test modules --------------------------*/

	private final List<Module> testModules = new ArrayList<>();

	protected void addTestModule(Module testModule){
		testModules.add(testModule);
	}

	public List<Module> getTestModules(){
		return testModules;
	}

	/*-------------------------- plugin configs v2 --------------------------*/

	public final Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> classSingle = new HashMap<>();
	public final Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> classList = new HashMap<>();
	public final Map<PluginConfigKey<?>,PluginConfigValue<?>> instanceSingle = new HashMap<>();
	public final Map<PluginConfigKey<?>,List<PluginConfigValue<?>>> instanceList = new HashMap<>();

	public void addPluginConfig(Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> map){
		map.forEach(this::addPluginEntries);
	}

	protected void addPluginEntries(PluginConfigKey<?> key, Collection<Class<? extends PluginConfigValue<?>>> values){
		values.forEach(value -> addPluginEntryInternal(key, value));
	}

	protected void addPluginEntries(Collection<PluginConfigValue<?>> values){
		values.forEach(this::addPluginEntryInternal);
	}

	protected void addPluginEntry(PluginConfigKey<?> key, Class<? extends PluginConfigValue<?>> value){
		addPluginEntryInternal(key, value);
	}

	protected void addPluginEntry(PluginConfigValue<?> value){
		addPluginEntryInternal(value);
	}

	private void addPluginEntryInternal(PluginConfigKey<?> key, Class<? extends PluginConfigValue<?>> value){
		switch(key.type){
		case CLASS_LIST:
			classList.computeIfAbsent(key, $ -> new ArrayList<>()).add(value);
			break;
		case CLASS_SINGLE:
			if(classSingle.containsKey(key)){
				throw new RuntimeException(key.persistentString + " has already been set");
			}
			classSingle.put(key, value);
			break;
		default:
			break;
		}
	}

	private void addPluginEntryInternal(PluginConfigValue<?> value){
		switch(value.getKey().type){
		case INSTANCE_LIST:
			instanceList.computeIfAbsent(value.getKey(), $ -> new ArrayList<>()).add(value);
			break;
		case INSTANCE_SINGLE:
			if(instanceSingle.containsKey(value.getKey())){
				throw new RuntimeException(value.getKey().persistentString + " has already been set");
			}
			instanceSingle.put(value.getKey(), value);
			break;
		default:
			break;
		}
	}

}
