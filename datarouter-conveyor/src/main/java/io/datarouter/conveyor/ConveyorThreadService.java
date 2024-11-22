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
package io.datarouter.conveyor;

import io.datarouter.conveyor.ConveyorConfigurationGroup.ConveyorPackage;
import io.datarouter.conveyor.config.DatarouterConveyorClusterThreadCountSettings;
import io.datarouter.conveyor.config.DatarouterConveyorThreadCountSettings;
import io.datarouter.webappinstance.service.ClusterThreadCountService;
import io.datarouter.webappinstance.service.ClusterThreadCountService.InstanceThreadCounts;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ConveyorThreadService{

	@Inject
	private ClusterThreadCountService clusterThreadCountService;
	@Inject
	private DatarouterConveyorThreadCountSettings threadCountSettings;
	@Inject
	private DatarouterConveyorClusterThreadCountSettings clusterThreadCountSettings;

	public InstanceThreadCounts getThreads(ConveyorPackage conveyorPackage){
		return clusterThreadCountService.getThreadCountInfoForThisInstance(
				conveyorPackage.name(),
				clusterThreadCountSettings.getSettingForConveyorPackage(conveyorPackage).get(),
				threadCountSettings.getSettingForConveyorPackage(conveyorPackage).get());
	}

}
