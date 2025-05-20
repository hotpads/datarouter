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
package io.datarouter.clustersetting.link;

import java.util.Optional;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.httpclient.endpoint.link.DatarouterLink;

public class ClusterSettingBrowseLink extends DatarouterLink{

	public static final String
			P_location = "location",
			P_partialName = "partialName";

	public Optional<String> location = Optional.empty();
	public Optional<String> partialName = Optional.empty();

	public ClusterSettingBrowseLink(){
		super(new DatarouterClusterSettingPaths().datarouter.settings.browse.all);
	}

	public ClusterSettingBrowseLink withLocation(String location){
		this.location = Optional.of(location);
		return this;
	}

	public ClusterSettingBrowseLink withOptLocation(Optional<String> optLocation){
		this.location = optLocation;
		return this;
	}

	public ClusterSettingBrowseLink withOptPartialName(Optional<String> optPartialName){
		this.partialName = optPartialName;
		return this;
	}

}
