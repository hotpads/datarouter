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
package io.datarouter.conveyor.config;

import java.util.List;

import io.datarouter.conveyor.ConveyorAppListener;
import io.datarouter.instrumentation.description.Description;
import io.datarouter.instrumentation.description.Detail;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterConveyorPlugin extends BaseWebPlugin{

	public DatarouterConveyorPlugin(){
		addAppListener(ConveyorAppListener.class);
		addRouteSet(DatarouterConveyorRouteSet.class);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.JOBS,
				new DatarouterConveyorPaths().datarouter.conveyors.list,
				"Conveyors");
		addSettingRoot(DatarouterConveyorSettingRoot.class);
		addTestable(DatarouterConveyorBootstrapIntegrationService.class);
	}

	@Override
	public Description describe(){
		return new Description(
				getName(),
				"Adds Datarouter Conveyor functionality and UI elements into an app",
				List.of(new Detail("Maven", "Requires the datarouter-conveyor Maven module in pom.xml.")));
	}

}
