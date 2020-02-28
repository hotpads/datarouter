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
package io.datarouter.testng;

import java.util.ArrayList;
import java.util.List;

import org.testng.IModuleFactory;
import org.testng.ITestContext;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class TestNgModuleFactory implements IModuleFactory{

	private final Iterable<? extends Module> modules;

	public TestNgModuleFactory(Iterable<? extends Module> modules){
		this.modules = modules;
	}

	@Override
	public Module createModule(ITestContext context, Class<?> testClass){

		Module globalModule = new AbstractModule(){

			@Override
			protected void configure(){
				for(Module module : modules){
					install(module);
				}
			}

		};
		Iterable<? extends Module> overriders = getOverriders();
		return Modules.override(globalModule).with(overriders);
	}

	protected List<Module> getOverriders(){
		return new ArrayList<>();
	}

}
