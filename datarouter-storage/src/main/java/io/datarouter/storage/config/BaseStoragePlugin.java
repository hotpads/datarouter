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

import io.datarouter.inject.InjectionTool;
import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.dao.DaosModuleBuilder.EmptyDaosModuleBuilder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.tuple.Pair;

/**
 * Plugins are verbose wrappers around GuiceModules for easy installation of datarouter modules. They use a builder
 * pattern to specify required and optional implementations of classes used by the module. Some modules have their own
 * extensions of BasePlugin which allows more features to get configured easily.
 *
 * BasePlugin auto configures and binds cluster settings and dao registration.
 */
public abstract class BaseStoragePlugin extends BasePlugin{

	private DaosModuleBuilder daosModule = new EmptyDaosModuleBuilder();

	protected void addSettingRoot(Class<? extends SettingRoot> settingRoot){
		addPluginEntry(SettingRoot.KEY, settingRoot);
	}

	protected void setDaosModule(DaosModuleBuilder daosModule){
		this.daosModule = daosModule;
	}

	protected void setDaosModule(List<Pair<Class<? extends Dao>,List<ClientId>>> daosAndClients){
		this.daosModule = new DaosModuleBuilder(){

			@Override
			public List<Class<? extends Dao>> getDaoClasses(){
				return Scanner.of(daosAndClients)
						.<Class<? extends Dao>>map(Pair::getLeft)
						.list();
			}

			@Override
			protected void configure(){
				daosAndClients.forEach(this::buildAndBindDaoParam);
			}

			private <T> void buildAndBindDaoParam(Pair<Class<? extends Dao>,List<ClientId>> pair){
				@SuppressWarnings("unchecked")
				Class<T> daoParamsClass = (Class<T>)InjectionTool.findInjectableClasses(pair.getLeft())
						.include(BaseRedundantDaoParams.class::isAssignableFrom)
						.findFirst()
						.orElseThrow(() ->
								new RuntimeException("no injected BaseDaoParams found for " + pair.getLeft()));
				T daoParamInstance = ReflectionTool.createWithParameters(
						daoParamsClass,
						List.of(pair.getRight()));//need a List<List<ClientIds>>
				bind(daoParamsClass).toInstance(daoParamInstance);
			}

		};
	}

	public DaosModuleBuilder getDaosModuleBuilder(){
		return daosModule;
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
