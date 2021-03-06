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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.config.DatarouterJobPlugin.DatarouterJobPluginBuilder;
import io.datarouter.job.detached.DefaultDetachedJobExecutor;
import io.datarouter.job.detached.DetachedJobExecutor;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.web.config.DatarouterWebWebappConfigBuilder;
import io.datarouter.web.config.DatarouterWebappConfig;

public abstract class DatarouterJobWebappConfigBuilder<T extends DatarouterJobWebappConfigBuilder<T>>
extends DatarouterWebWebappConfigBuilder<T>{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterJobWebappConfigBuilder.class);

	private final List<Class<? extends BaseTriggerGroup>> triggerGroups;
	private final List<BaseJobPlugin> jobPlugins;

	private Class<? extends DetachedJobExecutor> detachedJobExecutorClass = DefaultDetachedJobExecutor.class;

	public static class DatarouterJobWebappBuilderImpl
	extends DatarouterJobWebappConfigBuilder<DatarouterJobWebappBuilderImpl>{

		public DatarouterJobWebappBuilderImpl(
				DatarouterService datarouterService,
				ServerTypes serverTypes,
				DatarouterProperties datarouterProperties,
				List<ClientId> defaultClientIds,
				ServletContextListener log4jServletContextListener){
			super(datarouterService, serverTypes, datarouterProperties, defaultClientIds, log4jServletContextListener);
		}

		@Override
		protected DatarouterJobWebappBuilderImpl getSelf(){
			return this;
		}

	}

	protected DatarouterJobWebappConfigBuilder(
			DatarouterService datarouterService,
			ServerTypes serverTypes,
			DatarouterProperties datarouterProperties,
			List<ClientId> defaultClientIds,
			ServletContextListener log4jServletContextListener){
		super(datarouterService, serverTypes, datarouterProperties, defaultClientIds, log4jServletContextListener);
		this.triggerGroups = new ArrayList<>();
		this.jobPlugins = new ArrayList<>();
	}

	@Override
	public DatarouterWebappConfig build(){
		jobPlugins.forEach(this::addJobPluginWithoutInstalling);
		jobPlugins.stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		modules.addAll(jobPlugins);

		DatarouterJobPluginBuilder jobPluginBuilder = new DatarouterJobPluginBuilder(defaultClientIds);
		addJobPluginWithoutInstalling(jobPluginBuilder.getSimplePluginData());
		DatarouterJobPlugin jobPlugin = jobPluginBuilder
				.setTriggerGroupClasses(triggerGroups)
				.withDetachedJobExecutorClass(detachedJobExecutorClass)
				.build();

		modules.add(jobPlugin);
		logger.warn("done building " + getClass().getSimpleName());
		return super.build();
	}

	public T withDetachedJobExecutorClass(Class<? extends DetachedJobExecutor> detachedJobExecutorClass){
		this.detachedJobExecutorClass = detachedJobExecutorClass;
		return getSelf();
	}

	/*-------------------------- add job plugins ----------------------------*/

	public T addJobPlugin(BaseJobPlugin jobPlugin){
		addJobPluginInternal(jobPlugin);
		jobPlugin.getStoragePlugins().forEach(this::addStoragePluginInternal);
		jobPlugin.getWebPlugins().forEach(this::addWebPluginInternal);
		jobPlugin.getJobPlugins().forEach(this::addJobPluginInternal);
		return getSelf();
	}

	protected void addJobPluginInternal(BaseJobPlugin jobPlugin){
		boolean containsPlugin = jobPlugins.stream()
				.anyMatch(plugin -> plugin.getName().equals(jobPlugin.getName()));
		if(containsPlugin){
			throw new IllegalStateException(jobPlugin.getName() + " has already been added. It needs to be overridden");
		}
		jobPlugins.add(jobPlugin);
	}

	public T overrideJobPlugin(BaseJobPlugin jobPlugin){
		Optional<BaseJobPlugin> pluginToOverride = jobPlugins.stream()
				.filter(plugin -> plugin.getName().equals(jobPlugin.getName()))
				.findFirst();
		if(pluginToOverride.isEmpty()){
			throw new IllegalStateException(jobPlugin.getName() + " has not been added yet. It cannot be overridden.");
		}
		jobPlugins.remove(pluginToOverride.get());
		jobPlugins.add(jobPlugin);
		return getSelf();
	}

	protected T addJobPluginWithoutInstalling(BaseJobPlugin plugin){
		addWebPluginWithoutInstalling(plugin);
		triggerGroups.addAll(plugin.getTriggerGroups());
		return getSelf();
	}

	/*---------------------------- job helpers ------------------------------*/

	public T addTriggerGroup(Class<? extends BaseTriggerGroup> triggerGroup){
		this.triggerGroups.add(triggerGroup);
		return getSelf();
	}

}
