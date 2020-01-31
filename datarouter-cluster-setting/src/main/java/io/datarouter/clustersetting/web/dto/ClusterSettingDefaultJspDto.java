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
package io.datarouter.clustersetting.web.dto;

public class ClusterSettingDefaultJspDto{

	private final boolean isGlobalDefault;
	private final String environmentType;
	private final String environment;
	private final String serverType;
	private final String serverName;
	private final Object value;

	public ClusterSettingDefaultJspDto(boolean isGlobalDefault, String environmentType, String environment,
			String serverType, String serverName, Object value){
		this.isGlobalDefault = isGlobalDefault;
		this.environmentType = environmentType;
		this.environment = environment;
		this.serverType = serverType;
		this.serverName = serverName;
		this.value = value;
	}

	public boolean isGlobalDefault(){
		return isGlobalDefault;
	}

	public String getEnvironmentType(){
		return environmentType;
	}

	public String getEnvironment(){
		return environment;
	}

	public String getServerType(){
		return serverType;
	}

	public String getServerName(){
		return serverName;
	}

	public Object getValue(){
		return value;
	}

	public String getGlobalOrProfile(){
		return isGlobalDefault ? "global" : environmentType;
	}

}
