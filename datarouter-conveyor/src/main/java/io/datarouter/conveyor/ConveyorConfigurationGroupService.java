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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.ConveyorConfigurationGroup.ConveyorPackage;
import io.datarouter.scanner.Scanner;

@Singleton
public class ConveyorConfigurationGroupService{

	@Inject
	private ConveyorConfigurationGroupSupplier configurationGroupSupplier;

	public Scanner<ConveyorPackage> getAllPackages(){
		return configurationGroupSupplier.get()
				.concatIter(ConveyorConfigurationGroup::getConveyorPackages);
	}

	public ConveyorPackage getConveyorPackage(
			Class<? extends ConveyorConfiguration> configurationClass){
		return configurationGroupSupplier.get()
				.map(group -> group.getPackageFromConfigurationClass(configurationClass))
				.findFirst()
				.get();
	}

}
