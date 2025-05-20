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
import io.datarouter.clustersetting.web.override.ClusterSettingEditSource;
import io.datarouter.httpclient.endpoint.link.DatarouterLink;

public class ClusterSettingOverrideCreateLink extends DatarouterLink{

	public static final String
			P_source = "source",
			P_partialName = "partialName",
			P_scope = "scope",
			P_name = "name",
			P_serverType = "serverType",
			P_serverName = "serverName",
			P_value = "value",
			P_comment = "comment",
			P_submitButton = "submitButton";

	public Optional<String> source = Optional.empty();
	public Optional<String> partialName = Optional.empty();
	public Optional<String> scope = Optional.empty();
	public Optional<String> name = Optional.empty();
	public Optional<String> serverType = Optional.empty();
	public Optional<String> serverName = Optional.empty();
	public Optional<String> value = Optional.empty();
	public Optional<String> comment = Optional.empty();
	public Optional<Boolean> submitButton = Optional.empty();

	public ClusterSettingOverrideCreateLink(){
		super(new DatarouterClusterSettingPaths().datarouter.settings.overrides.create);
	}

	public ClusterSettingOverrideCreateLink withSource(ClusterSettingEditSource source){
		this.source = Optional.of(source.persistentString);
		return this;
	}

	public ClusterSettingOverrideCreateLink withPartialName(String partialName){
		this.partialName = Optional.of(partialName);
		return this;
	}

	public ClusterSettingOverrideCreateLink withOptPartialName(Optional<String> optPartialName){
		this.partialName = optPartialName;
		return this;
	}

	public ClusterSettingOverrideCreateLink withName(String name){
		this.name = Optional.of(name);
		return this;
	}

	public ClusterSettingOverrideCreateLink withOptName(Optional<String> optName){
		this.name = optName;
		return this;
	}
}
