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

import io.datarouter.conveyor.ConveyorConfigurationGroup;
import io.datarouter.conveyor.ConveyorConfigurationGroup.ConveyorPackage;
import io.datarouter.conveyor.ConveyorConfigurationGroupSupplier;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.test.TestableService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Use this class to check for injection problems
 */
@Singleton
public class DatarouterConveyorBootstrapIntegrationService implements TestableService{

	@Inject
	private ConveyorConfigurationGroupSupplier configurationGroupSupplier;
	@Inject
	private DatarouterInjector injector;

	@Override
	public void testAll(){
		testInjection();
	}

	private void testInjection(){
		configurationGroupSupplier.get()
				.concatIter(ConveyorConfigurationGroup::getConveyorPackages)
				.map(ConveyorPackage::configurationClass)
				.forEach(injector::getInstance);
	}

}
