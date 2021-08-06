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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.environment.EnvironmentType;

@Singleton
public class DatarouterServerTypeDetector implements ServerTypeDetector{

	private final String environmentType;
	private final ServerType serverType;
	private final String environment;

	@Inject
	protected DatarouterServerTypeDetector(DatarouterProperties datarouterProperties){
		this.environmentType = datarouterProperties.getEnvironmentType();
		this.serverType = datarouterProperties.getServerType();
		this.environment = datarouterProperties.getEnvironment();
	}

	@Override
	public boolean mightBeProduction(){
		if(serverType.isProduction()){
			return true;
		}
		if(environmentType.equals(EnvironmentType.PRODUCTION.get().getPersistentString())){
			return true;
		}
		if(environment.equals(EnvironmentType.PRODUCTION.get().getPersistentString())
				|| getAdditionalProductionEnvironments().contains(environment)){
			return true;
		}
		return false;
	}

	@Override
	public boolean mightBeDevelopment(){
		return EnvironmentType.DEVELOPMENT.get().getPersistentString().equals(environmentType);
	}

	protected Set<String> getAdditionalProductionEnvironments(){
		return Set.of();
	}

}
