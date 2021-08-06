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
package io.datarouter.clustersetting.web.dto;

import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;

public class ClusterSettingAndValidityJspDto{

	public final ClusterSettingJspDto setting;
	public final ClusterSettingValidity validity;

	public ClusterSettingAndValidityJspDto(ClusterSetting setting, ClusterSettingValidity validity){
		this.setting = new ClusterSettingJspDto(setting);
		this.validity = validity;
	}

	public ClusterSettingJspDto getSetting(){
		return setting;
	}

	public ClusterSettingValidity getValidity(){
		return validity;
	}

}
