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
package io.datarouter.job.config;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.test.Testable;
import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.scheduler.JobSchedulerTestService;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.SingletonTestService;

/**
 * Use this class to check for injection problems
 */
@Singleton
public class DatarouterJobBootstrapIntegrationService implements Testable{

	private static final List<Pair<Class<?>,Boolean>> SINGLETON_CHECKS = List.of(
			new Pair<>(BaseJob.class, false),
			new Pair<>(BaseTriggerGroup.class, true));

	@Inject
	private JobSchedulerTestService jobSchedulerTestService;
	@Inject
	private SingletonTestService singletonTestService;

	@Override
	public void testAll(){
		testSingletons();
		testJobs();
	}

	private void testJobs(){
		jobSchedulerTestService.validateCronExpressions();
	}

	private void testSingletons(){
		SINGLETON_CHECKS.forEach(pair -> singletonTestService.checkSingletonForSubClasses(pair.getLeft(), pair
				.getRight()));
	}

}
