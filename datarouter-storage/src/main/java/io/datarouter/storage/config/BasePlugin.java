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

import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.dao.DaosModuleBuilder.EmptyDaosModuleBuilder;
import io.datarouter.storage.setting.SettingRoot;

public abstract class BasePlugin extends BaseGuiceModule{

	private final List<Class<? extends SettingRoot>> settingRoots = new ArrayList<>();
	private DaosModuleBuilder daosModuleBuilder = new EmptyDaosModuleBuilder();

	protected void addSettingRoot(Class<? extends SettingRoot> settingRoot){
		settingRoots.add(settingRoot);
	}

	protected void setDaosModuleBuilder(DaosModuleBuilder daosModuleBuilder){
		this.daosModuleBuilder = daosModuleBuilder;
	}

	public DaosModuleBuilder getDaosModuleBuilder(){
		return daosModuleBuilder;
	}

	public List<Class<? extends SettingRoot>> getSettingRoots(){
		return settingRoots;
	}

}
