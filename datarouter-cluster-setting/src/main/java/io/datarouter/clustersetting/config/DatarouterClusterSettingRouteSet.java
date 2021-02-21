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
package io.datarouter.clustersetting.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.web.ClusterSettingsHandler;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterClusterSettingRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterClusterSettingRouteSet(DatarouterClusterSettingPaths paths){
		super(paths.datarouter);
		handle(paths.datarouter.settings).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.update).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.delete).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.roots).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.create).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.browseSettings).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.isRecognizedRoot).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.updateSettingTags).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.logsForName).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.customSettings).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.logsForAll).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.searchSettingNames).withHandler(ClusterSettingsHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.DATAROUTER_SETTINGS)
				.withIsSystemDispatchRule(true);
	}

}
