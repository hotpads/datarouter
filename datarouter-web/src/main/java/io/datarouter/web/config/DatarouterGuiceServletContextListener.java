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
package io.datarouter.web.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

import io.datarouter.inject.guice.GuiceStageFinder;

public class DatarouterGuiceServletContextListener extends GuiceServletContextListener{

	private final Stage stage;
	private final Iterable<Module> modules;

	public DatarouterGuiceServletContextListener(Iterable<Module> modules){
		this.stage = GuiceStageFinder.getGuiceStage();
		this.modules = modules;
	}

	@Override
	protected Injector getInjector(){
		return Guice.createInjector(stage, modules);
	}

}
