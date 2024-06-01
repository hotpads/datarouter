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
package io.datarouter.graphql.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Module;

import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;
import io.datarouter.web.config.RouteSetRegistry;

public class DatarouterGraphQlTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterGraphQlTestNgModuleFactory(){
		super(List.of(new DatarouterWebGuiceModule()));
	}

	@Override
	protected List<Module> getOverriders(){
		return Stream.of(new BaseGuiceModule(){
					@Override
					protected void configure(){
						bind(RouteSetRegistry.class).toInstance(List::of);
					}
				})
				.collect(Collectors.toUnmodifiableList());
	}

}
