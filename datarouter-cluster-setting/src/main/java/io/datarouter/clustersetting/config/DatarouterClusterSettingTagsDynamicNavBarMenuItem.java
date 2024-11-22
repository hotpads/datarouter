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

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.storage.servertype.DatarouterServerTypeDetector;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemBuilder;
import jakarta.inject.Inject;

public class DatarouterClusterSettingTagsDynamicNavBarMenuItem implements DynamicNavBarItem{

	@Inject
	private DatarouterServerTypeDetector datarouterServerTypeDetector;
	@Inject
	private DatarouterClusterSettingPaths clusterSettingPaths;

	@Override
	public NavBarItem getNavBarItem(){
		return new NavBarItemBuilder(
				DatarouterNavBarCategory.CONFIGURATION,
				clusterSettingPaths.datarouter.settings.tags,
				DatarouterClusterSettingPlugin.NAME + " - Tags")
				.setDispatchRule(new DispatchRule().allowRoles(DatarouterUserRoleRegistry.DATAROUTER_SETTINGS))
				.build();
	}

	@Override
	public Boolean shouldDisplay(){
		return datarouterServerTypeDetector.mightBeDevelopment();
	}

	@Override
	public NavBarItemType getType(){
		return NavBarItemType.DATAROUTER;
	}

}
