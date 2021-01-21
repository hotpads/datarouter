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

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobSchedulerTestService;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.clazz.AnnotationTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.SingletonTestService;

/**
 * Use this class to check for injection problems
 */
@Singleton
public class DatarouterJobBootstrapIntegrationService implements TestableService{

	private static final List<Pair<Class<?>,Boolean>> SINGLETON_CHECKS = List.of(
			new Pair<>(BaseTriggerGroup.class, true));

	@Inject
	private TriggerGroupClasses triggerGroupClasses;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private JobSchedulerTestService jobSchedulerTestService;
	@Inject
	private SingletonTestService singletonTestService;

	@Override
	public void testAll(){
		injector.getInstancesOfType(BaseJob.class);
		testSingletons();
		testJobs();
	}

	private void testJobs(){
		jobSchedulerTestService.validateCronExpressions();
	}

	private void testSingletons(){
		Scanner.of(triggerGroupClasses.get())
				.map(injector::getInstance)
				.concatIter(BaseTriggerGroup::getJobPackages)
				.map(jobPackage -> jobPackage.jobClass)
				.forEach(clazz -> AnnotationTool.checkSingletonForClass(clazz, false));
		SINGLETON_CHECKS.forEach(pair -> singletonTestService.checkSingletonForSubClasses(pair.getLeft(), pair
				.getRight()));
	}

}
