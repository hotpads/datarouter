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
package io.datarouter.storage.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.inject.InjectionTool;
import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDaoParams;
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

	private final List<Class<? extends SettingRoot>> settingRoots = new ArrayList<>();
	private DaosModuleBuilder daosModule = new EmptyDaosModuleBuilder();

	protected void addSettingRoot(Class<? extends SettingRoot> settingRoot){
		settingRoots.add(settingRoot);
	}

	protected void setDaosModule(DaosModuleBuilder daosModule){
		this.daosModule = daosModule;
	}

	protected void setDaosModule(List<Pair<Class<? extends Dao>,ClientId>> daosAndClients){
		this.daosModule = new DaosModuleBuilder(){

			@Override
			public List<Class<? extends Dao>> getDaoClasses(){
				return Scanner.of(daosAndClients).<Class<? extends Dao>>map(Pair::getLeft).list();
			}

			@Override
			protected void configure(){
				for(Pair<Class<? extends Dao>,ClientId> pair : daosAndClients){
					buildAndBindDaoParam(pair);
				}
			}

			private <T> void buildAndBindDaoParam(Pair<Class<? extends Dao>,ClientId> pair){
				@SuppressWarnings("unchecked")
				Class<T> daoParamsClass = (Class<T>)InjectionTool.findInjectableClasses(pair.getLeft())
						.include(clazz -> BaseDaoParams.class.isAssignableFrom(clazz))
						.findFirst()
						.orElseThrow(() ->
								new RuntimeException("no injected BaseDaoParams found for " + pair.getLeft()));
				T daoParamInstance = ReflectionTool.createWithParameters(daoParamsClass, List.of(pair.getRight()));
				bind(daoParamsClass).toInstance(daoParamInstance);
			}

		};
	}

	public DaosModuleBuilder getDaosModuleBuilder(){
		return daosModule;
	}

	public List<Class<? extends SettingRoot>> getSettingRoots(){
		return settingRoots;
	}

}
