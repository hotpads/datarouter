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
package io.datarouter.clustersetting.web;

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.web.config.ServletContextSupplier;

public class ClusterSettingLinks{

	@Inject
	private ServletContextSupplier contextSupplier;
	@Inject
	private DatarouterClusterSettingPaths paths;

	public String browseSettings(String name){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath() + paths.datarouter.settings.toSlashedString())
				.addParameter("submitAction", "browseSettings")
				.addParameter("name", name);
		return uriBuilder.toString();
	}

}
