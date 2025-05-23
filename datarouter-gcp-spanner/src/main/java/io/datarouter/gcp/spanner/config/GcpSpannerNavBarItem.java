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
package io.datarouter.gcp.spanner.config;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemBuilder;
import jakarta.inject.Inject;

public class GcpSpannerNavBarItem implements DynamicNavBarItem{

	@Inject
	private SpannerProjectIdAndInstanceIdSupplier projectIdAndInstanceIdSupplier;

	@Override
	public NavBarItem getNavBarItem(){
		String projectId = projectIdAndInstanceIdSupplier.getSpannerProjectIdAndInstanceId().projectId;
		String instanceId = projectIdAndInstanceIdSupplier.getSpannerProjectIdAndInstanceId().instanceId;
		String link = "https://console.cloud.google.com/spanner/instances/" + instanceId + "/details/databases?project="
				+ projectId;
		return new NavBarItemBuilder(DatarouterNavBarCategory.EXTERNAL, link, "GCP Spanner")
				.openInNewTab()
				.setDispatchRule(new DispatchRule().allowRoles(DatarouterUserRoleRegistry.DATAROUTER_MONITORING))
				.build();
	}

	@Override
	public Boolean shouldDisplay(){
		String projectId = projectIdAndInstanceIdSupplier.getSpannerProjectIdAndInstanceId().projectId;
		String instanceId = projectIdAndInstanceIdSupplier.getSpannerProjectIdAndInstanceId().instanceId;
		return !projectId.isEmpty() && !instanceId.isEmpty();
	}

	@Override
	public NavBarItemType getType(){
		return NavBarItemType.DATAROUTER;
	}

}
