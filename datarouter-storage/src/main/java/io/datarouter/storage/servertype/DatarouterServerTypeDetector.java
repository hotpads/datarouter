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
package io.datarouter.storage.servertype;

import java.util.Set;

import io.datarouter.storage.config.environment.EnvironmentType;
import io.datarouter.storage.config.properties.DatarouterEnvironmentTypeSupplier;
import io.datarouter.storage.config.properties.DatarouterServerTypeSupplier;
import io.datarouter.storage.config.properties.EnvironmentName;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterServerTypeDetector implements ServerTypeDetector{

	private final String environmentType;
	private final ServerType serverType;
	private final String environment;

	@Inject
	protected DatarouterServerTypeDetector(
			DatarouterServerTypeSupplier serverType,
			DatarouterEnvironmentTypeSupplier environmentType,
			EnvironmentName environmentName){
		this.environmentType = environmentType.get();
		this.serverType = serverType.get();
		this.environment = environmentName.get();
	}

	@Override
	public boolean mightBeProduction(){
		if(serverType.isProduction()){
			return true;
		}
		if(environmentType.equals(EnvironmentType.PRODUCTION.get().getPersistentString())){
			return true;
		}
		return environment.equals(EnvironmentType.PRODUCTION.get().getPersistentString())
				|| getAdditionalProductionEnvironments().contains(environment);
	}

	@Override
	public boolean mightBeDevelopment(){
		return EnvironmentType.DEVELOPMENT.get().getPersistentString().equals(environmentType)
				|| getAdditionalDevelopmentEnvironmentsTypes().contains(environmentType);
	}

	protected Set<String> getAdditionalProductionEnvironments(){
		return Set.of();
	}

	protected Set<String> getAdditionalDevelopmentEnvironmentsTypes(){
		return Set.of();
	}

}
