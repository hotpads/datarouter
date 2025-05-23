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
package io.datarouter.metric.config;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemBuilder;
import jakarta.inject.Inject;

public class IndexUsageNavBarItem implements DynamicNavBarItem{

	@Inject
	private DatarouterMetricPaths paths;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	@Override
	public NavBarItem getNavBarItem(){
		return new NavBarItemBuilder(
				DatarouterNavBarCategory.EXTERNAL,
				paths.datarouter.metric.indexUsage.view.toSlashedString(),
				"Index Usage")
				.setDispatchRule(new DispatchRule()
						.allowRoles(DatarouterUserRoleRegistry.DATAROUTER_MONITORING))
				.build();
	}

	@Override
	public Boolean shouldDisplay(){
		return serverTypeDetector.mightBeProduction();
	}

	@Override
	public NavBarItemType getType(){
		return NavBarItemType.DATAROUTER;
	}

}
