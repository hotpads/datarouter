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
package io.datarouter.clustersetting.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.web.ClusterSettingsHandler;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler;
import io.datarouter.clustersetting.web.log.ClusterSettingLogHandler;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideCreateHandler;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideDeleteHandler;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideUpdateHandler;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideViewHandler;
import io.datarouter.clustersetting.web.tag.ClusterSettingTagsHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterClusterSettingRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterClusterSettingRouteSet(DatarouterClusterSettingPaths paths){
		handle(paths.datarouter.settings).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.update).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.delete).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.roots).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.create).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.browseSettings).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.isRecognizedRoot).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.updateSettingTags).withHandler(ClusterSettingsHandler.class);
		handle(paths.datarouter.settings.searchSettingNames).withHandler(ClusterSettingsHandler.class);

		//browse
		handle(paths.datarouter.settings.browse.all).withHandler(ClusterSettingBrowseHandler.class);

		//logs
		handle(paths.datarouter.settings.log.all).withHandler(ClusterSettingLogHandler.class);
		handle(paths.datarouter.settings.log.node).withHandler(ClusterSettingLogHandler.class);
		handle(paths.datarouter.settings.log.setting).withHandler(ClusterSettingLogHandler.class);
		handle(paths.datarouter.settings.log.single).withHandler(ClusterSettingLogHandler.class);

		//overrides
		handle(paths.datarouter.settings.overrides.view).withHandler(ClusterSettingOverrideViewHandler.class);
		handle(paths.datarouter.settings.overrides.create).withHandler(ClusterSettingOverrideCreateHandler.class);
		handle(paths.datarouter.settings.overrides.update).withHandler(ClusterSettingOverrideUpdateHandler.class);
		handle(paths.datarouter.settings.overrides.delete).withHandler(ClusterSettingOverrideDeleteHandler.class);

		//tags
		handle(paths.datarouter.settings.tags).withHandler(ClusterSettingTagsHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.DATAROUTER_SETTINGS)
				.withTag(Tag.DATAROUTER);
	}

}
