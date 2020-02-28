/**
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
package io.datarouter.joblet.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.test.Testable;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;

/**
 * Use this class to check for injection problems
 */
@Singleton
public class DatarouterJobletBootstrapIntegrationService implements Testable{

	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterInjector injector;

	@Override
	public void testAll(){
		testJoblets();
	}

	private void testJoblets(){
		jobletTypeFactory.getAllTypes().stream()
				.map(JobletType::getAssociatedClass)
				.forEach(injector::getInstance);
	}

}
