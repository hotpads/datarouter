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
package io.datarouter.aws.sqs.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.datarouter.aws.sqs.service.SqsQueuesDailyDigest;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterSqsPlugin extends BaseWebPlugin{

	private DatarouterSqsPlugin(DatarouterSqsPluginBuilder builder){
		addSettingRoot(DatarouterSqsSettingsRoot.class);
		addRouteSet(DatarouterSqsRouteSet.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterSqsTriggerGroup.class);
		addPluginEntry(builder.getCatalogServiceNames());
		addDatarouterGithubDocLink("datarouter-aws-sqs");
		addDailyDigest(SqsQueuesDailyDigest.class);

		builder.testableServiceClasses.forEach(this::addTestable);
	}

	public static class DatarouterSqsPluginBuilder{

		private final List<Class<? extends TestableService>> testableServiceClasses = new ArrayList<>();
		private final List<String> catalogServiceNames = new ArrayList<>();

		public DatarouterSqsPluginBuilder addTestableClass(Class<? extends TestableService> testableService){
			testableServiceClasses.add(testableService);
			return this;
		}

		public DatarouterSqsPluginBuilder addServiceNames(Set<String> serviceNames){
			catalogServiceNames.addAll(serviceNames);
			return this;
		}

		public DatarouterSqsPlugin build(){
			return new DatarouterSqsPlugin(this);
		}

		public ServiceNameRegistry getCatalogServiceNames(){
			return new ServiceNameRegistry(catalogServiceNames);
		}

	}

	public static class ServiceNameRegistry implements PluginConfigValue<ServiceNameRegistry>{

		public static final PluginConfigKey<ServiceNameRegistry> KEY = new PluginConfigKey<>(
				"serviceNameRegistry",
				PluginConfigType.INSTANCE_SINGLE);

		public final List<String> serviceNames;

		public ServiceNameRegistry(List<String> serviceNames){
			this.serviceNames = serviceNames;
		}

		@Override
		public PluginConfigKey<ServiceNameRegistry> getKey(){
			return KEY;
		}

	}

}
