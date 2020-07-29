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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContextListener;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.job.config.DatarouterJobWebappConfigBuilder;
import io.datarouter.joblet.config.DatarouterJobletPlugin.DatarouterJobletPluginBuilder;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder.NoOpJobletExternalLinkBuilder;
import io.datarouter.joblet.queue.JobletRequestSelector;
import io.datarouter.joblet.setting.BaseJobletPlugin;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeGroup;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.DatarouterWebappConfig;

public abstract class DatarouterJobletWebappConfigBuilder<T extends DatarouterJobletWebappConfigBuilder<T>>
extends DatarouterJobWebappConfigBuilder<T>{

	private final ClientId defaultQueueClientId;
	private final List<JobletType<?>> jobletTypes;
	private final List<BaseJobletPlugin> jobletPlugins;
	private final List<Pair<String,Class<? extends JobletRequestSelector>>> selectorTypes;

	private Class<? extends JobletExternalLinkBuilder> jobletExternalLinkBuilder;

	public static class DatarouterJobletWebappBuilderImpl
	extends DatarouterJobletWebappConfigBuilder<DatarouterJobletWebappBuilderImpl>{

		public DatarouterJobletWebappBuilderImpl(
				DatarouterService datarouterService,
				ServerTypes serverTypes,
				DatarouterProperties datarouterProperties,
				ClientId defaultClientId,
				ClientId defaultQueueClientId,
				ServletContextListener log4jServletContextListener){
			super(datarouterService, serverTypes, datarouterProperties, defaultClientId, defaultQueueClientId,
					log4jServletContextListener);
		}

		@Override
		protected DatarouterJobletWebappBuilderImpl getSelf(){
			return this;
		}

	}

	public DatarouterJobletWebappConfigBuilder(
			DatarouterService datarouterService,
			ServerTypes serverTypes,
			DatarouterProperties datarouterProperties,
			ClientId defaultClientId,
			ClientId defaultQueueClientId,
			ServletContextListener log4jServletContextListener){
		super(datarouterService, serverTypes, datarouterProperties, defaultClientId, log4jServletContextListener);
		this.defaultQueueClientId = defaultQueueClientId;
		this.jobletTypes = new ArrayList<>();
		this.jobletExternalLinkBuilder = NoOpJobletExternalLinkBuilder.class;
		this.jobletPlugins = new ArrayList<>();
		this.selectorTypes = new ArrayList<>();
	}

	@Override
	public DatarouterWebappConfig build(){
		jobletPlugins.forEach(this::addJobletPluginWithoutInstalling);
		jobletPlugins.stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		modules.addAll(jobletPlugins);

		DatarouterJobletPluginBuilder jobletPluginBuilder = new DatarouterJobletPluginBuilder(defaultClientId,
				defaultQueueClientId);
		addJobletPluginWithoutInstalling(jobletPluginBuilder.getSimplePluginData());
		DatarouterJobletPlugin jobletPlugin = jobletPluginBuilder
				.setJobletTypes(jobletTypes)
				.setExternalLinkBuilderClass(jobletExternalLinkBuilder)
				.withSelectorTypes(selectorTypes)
				.build();
		modules.add(jobletPlugin);
		return super.build();
	}

	/*------------------------- add joblet plugins --------------------------*/

	public T addJobletPlugin(BaseJobletPlugin jobletPlugin){
		boolean containsPlugin = jobletPlugins.stream()
				.anyMatch(plugin -> plugin.getName().equals(jobletPlugin.getName()));
		if(containsPlugin){
			throw new IllegalStateException(jobletPlugin.getName()
					+ " has already been added. It needs to be overridden");
		}
		jobletPlugins.add(jobletPlugin);
		return getSelf();
	}

	public T overrideJobletPlugin(BaseJobletPlugin jobletPlugin){
		Optional<BaseJobletPlugin> pluginToOverride = jobletPlugins.stream()
				.filter(plugin -> plugin.getName().equals(jobletPlugin.getName()))
				.findFirst();
		if(pluginToOverride.isEmpty()){
			throw new IllegalStateException(jobletPlugin.getName()
					+ " has not been added yet. It cannot be overridden.");
		}
		jobletPlugins.remove(pluginToOverride.get());
		jobletPlugins.add(jobletPlugin);
		return getSelf();
	}

	private T addJobletPluginWithoutInstalling(BaseJobletPlugin plugin){
		addJobPluginWithoutInstalling(plugin);
		jobletTypes.addAll(plugin.getJobletTypes());
		return getSelf();
	}

	/*--------------------------- joblet helpers ----------------------------*/

	public T setJobletExternalLinkBuilder(
			Class<? extends JobletExternalLinkBuilder> jobletExternalLinkBuilder){
		this.jobletExternalLinkBuilder = jobletExternalLinkBuilder;
		return getSelf();
	}

	public T addJobletTypeGroup(JobletTypeGroup jobletTypeGroup){
		this.jobletTypes.addAll(jobletTypeGroup.getAll());
		return getSelf();
	}

	public T addJobletTypes(List<JobletType<?>> jobletTypes){
		this.jobletTypes.addAll(jobletTypes);
		return getSelf();
	}

	public T addJobletSelector(String name, Class<? extends JobletRequestSelector> selectorClass){
		this.selectorTypes.add(new Pair<>(name, selectorClass));
		return getSelf();
	}

}
