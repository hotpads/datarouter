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

import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemBuilder;
import io.datarouter.web.user.role.DatarouterUserRole;
import jakarta.inject.Inject;

public class MetricLinksNavBarItem implements DynamicNavBarItem{

	@Inject
	private DatarouterMetricPaths paths;

	@Override
	public NavBarItem getNavBarItem(){
		return new NavBarItemBuilder(
				DatarouterNavBarCategory.EXTERNAL,
				paths.datarouter.metric.metricLinks.view.toSlashedString(),
				"Metric Links")
				.setDispatchRule(new DispatchRule()
						.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.USER))
				.build();
	}

	@Override
	public Boolean shouldDisplay(){
		return true;
	}

	@Override
	public NavBarItemType getType(){
		return NavBarItemType.DATAROUTER;
	}

}
