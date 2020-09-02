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

import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;

public class ClusterSettingJspDto{

	private final String name;
	private final ClusterSettingScope scope;
	private final String serverType;
	private final String serverName;
	private final String value;

	public ClusterSettingJspDto(ClusterSetting setting){
		this.name = setting.getKey().getName();
		this.scope = setting.getKey().getScope();
		this.serverType = setting.getKey().getServerType();
		this.serverName = setting.getKey().getServerName();
		this.value = setting.getValue();
	}

	public String getName(){
		return name;
	}

	public ClusterSettingScope getScope(){
		return scope;
	}

	public String getServerType(){
		return serverType;
	}

	public String getServerName(){
		return serverName;
	}

	public String getValue(){
		return value;
	}

}
