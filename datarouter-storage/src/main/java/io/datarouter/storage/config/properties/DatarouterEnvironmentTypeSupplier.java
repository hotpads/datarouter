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
package io.datarouter.storage.config.properties;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.ComputedPropertiesFinder;
import io.datarouter.storage.config.environment.DatarouterEnvironmentType;
import io.datarouter.storage.config.environment.EnvironmentType;

@Singleton
public class DatarouterEnvironmentTypeSupplier implements Supplier<String>{

	public static final String ENVIRONMENT_TYPE = "environmentType";

	private final String environmentType;

	@Inject
	private DatarouterEnvironmentTypeSupplier(ComputedPropertiesFinder finder){
		this.environmentType = finder.findProperty(
				ENVIRONMENT_TYPE,
				() -> EnvironmentType.DEVELOPMENT.get().getPersistentString());
	}

	@Override
	public String get(){
		return environmentType;
	}

	public DatarouterEnvironmentType getDatarouterEnvironmentType(){
		return new DatarouterEnvironmentType(environmentType);
	}

}
