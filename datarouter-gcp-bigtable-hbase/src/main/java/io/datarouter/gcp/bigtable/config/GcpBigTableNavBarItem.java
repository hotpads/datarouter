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
package io.datarouter.gcp.bigtable.config;

import javax.inject.Inject;

import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemBuilder;
import io.datarouter.web.user.role.DatarouterUserRole;

public class GcpBigTableNavBarItem implements DynamicNavBarItem{

	@Inject
	private BigtableProjectIdAndInstanceIdSupplier supplier;

	@Override
	public NavBarItem getNavBarItem(){
		String projectId = supplier.getBigtableProjectId();
		String instanceId = supplier.getBigtableInstanceId();
		String link = "https://console.cloud.google.com/bigtable/instances/" + instanceId + "/overview?project="
				+ projectId;
		return new NavBarItemBuilder(DatarouterNavBarCategory.EXTERNAL, link, "GCP Bigtable")
				.openInNewTab()
				.setDispatchRule(new DispatchRule().allowRoles(DatarouterUserRole.DATAROUTER_MONITORING))
				.build();
	}

	@Override
	public Boolean shouldDisplay(){
		String projectId = supplier.getBigtableProjectId();
		String instanceId = supplier.getBigtableInstanceId();
		return !projectId.isEmpty() && !instanceId.isEmpty();
	}

	@Override
	public NavBarItemType getType(){
		return NavBarItemType.DATAROUTER;
	}

}
