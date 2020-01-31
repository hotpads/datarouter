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
package io.datarouter.clustersetting;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.web.app.WebappName;

@Singleton
public class ClusterSettingFinderConfig{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private WebappName webappName;

	public ServerType getServerType(){
		return datarouterProperties.getServerType();
	}

	public String getServerName(){
		return datarouterProperties.getServerName();
	}

	public String getApplication(){
		return webappName.getName();
	}

}
