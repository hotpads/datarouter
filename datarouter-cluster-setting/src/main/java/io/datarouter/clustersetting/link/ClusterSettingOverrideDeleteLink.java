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

public class ClusterSettingOverrideDeleteLink extends DatarouterLink{

	public static final String
			P_sourceType = "sourceType",
			P_sourceLocation = "sourceLocation",
			P_partialName = "partialName",
			P_name = "name",
			P_serverType = "serverType",
			P_serverName = "serverName",
			P_comment = "comment",
			P_submitButton = "submitButton";

	public Optional<String> name = Optional.empty();
	public Optional<String> sourceType = Optional.empty();
	public Optional<String> sourceLocation = Optional.empty();
	public Optional<String> partialName = Optional.empty();
	public Optional<String> serverType = Optional.empty();
	public Optional<String> serverName = Optional.empty();
	public Optional<String> comment = Optional.empty();
	public Optional<Boolean> submitButton = Optional.empty();

	public ClusterSettingOverrideDeleteLink(){
		super(new DatarouterClusterSettingPaths().datarouter.settings.overrides.delete);
	}

	public ClusterSettingOverrideDeleteLink withName(String name){
		this.name = Optional.of(name);
		return this;
	}

	public ClusterSettingOverrideDeleteLink withSourceType(ClusterSettingEditSource sourceType){
		this.sourceType = Optional.of(sourceType.persistentString);
		return this;
	}

	public ClusterSettingOverrideDeleteLink withSourceLocation(String sourceLocation){
		this.sourceLocation = Optional.of(sourceLocation);
		return this;
	}

	public ClusterSettingOverrideDeleteLink withOptSourceLocation(Optional<String> optSourceLocation){
		this.sourceLocation = optSourceLocation;
		return this;
	}

	public ClusterSettingOverrideDeleteLink withPartialName(String partialName){
		this.partialName = Optional.of(partialName);
		return this;
	}

	public ClusterSettingOverrideDeleteLink withOptPartialName(Optional<String> optPartialName){
		this.partialName = optPartialName;
		return this;
	}

	public ClusterSettingOverrideDeleteLink withServerType(String serverType){
		this.serverType = Optional.of(serverType);
		return this;
	}

	public ClusterSettingOverrideDeleteLink withOptServerType(Optional<String> optServerType){
		this.serverType = optServerType;
		return this;
	}

	public ClusterSettingOverrideDeleteLink withServerName(String serverName){
		this.serverName = Optional.of(serverName);
		return this;
	}

	public ClusterSettingOverrideDeleteLink withOptServerName(Optional<String> optServerName){
		this.serverName = optServerName;
		return this;
	}

}
