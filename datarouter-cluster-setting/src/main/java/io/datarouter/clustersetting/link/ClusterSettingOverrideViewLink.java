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

public class ClusterSettingOverrideViewLink extends DatarouterLink{

	public static final String
			P_partialName = "partialName",
			P_suggestionsOnly = "suggestionsOnly";

	public Optional<Boolean> suggestionsOnly = Optional.empty();
	public Optional<String> partialName = Optional.empty();

	public ClusterSettingOverrideViewLink(){
		super(new DatarouterClusterSettingPaths().datarouter.settings.overrides.view);
	}

	public ClusterSettingOverrideViewLink withSuggestionsOnly(boolean suggestionsOnly){
		this.suggestionsOnly = Optional.of(suggestionsOnly);
		return this;
	}

	public ClusterSettingOverrideViewLink withPartialName(String partialName){
		this.partialName = Optional.of(partialName);
		return this;
	}
}
