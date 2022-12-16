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
package io.datarouter.clustersetting.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.storage.setting.Setting;

@Singleton
public class ClusterSettingLinkServletService{

	@Inject
	private ServletContext servletContext;
	@Inject
	private DatarouterClusterSettingPaths paths;

	public String browse(String settingName){
		return new URIBuilder()
				.setPath(servletContext.getContextPath() + paths.datarouter.settings.browseSettings.toSlashedString())
				.addParameter("name", settingName)
				.toString();
	}

	public String browse(Setting<?> setting){
		return browse(setting.getName());
	}

}
