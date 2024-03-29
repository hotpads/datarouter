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
package io.datarouter.conveyor.trace;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.conveyor.ConveyorConfigurationGroup;
import io.datarouter.conveyor.trace.conveyor.TraceConveyorConfigurationGroup;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.instrumentation.trace.TracePublisher.NoOpTracePublisher;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.trace.filter.GuiceTraceFilter;
import io.datarouter.trace.service.TracePublisherService;
import io.datarouter.trace.service.TraceUrlBuilder;
import io.datarouter.trace.service.TraceUrlBuilder.NoOpTraceUrlBuilder;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.trace.settings.DatarouterTracePublisherSettingRoot;
import io.datarouter.trace.storage.TraceQueueDao;
import io.datarouter.trace.storage.TraceQueueDao.TraceQueueDaoParams;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.config.DatarouterServletGuiceModule;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;

public class DatarouterTracePlugin extends BaseWebPlugin{

	private final Class<? extends TracePublisher> tracePublisher;
	private final Class<? extends TraceUrlBuilder> traceUrlBuilder;

	private DatarouterTracePlugin(
			boolean enablePublisherTraces,
			DatarouterTraceDaoModule daosModule,
			Class<? extends TracePublisher> tracePublisher,
			Class<? extends TraceUrlBuilder> traceUrlBuilder){
		this.tracePublisher = tracePublisher;
		this.traceUrlBuilder = traceUrlBuilder;

		addSettingRoot(DatarouterTraceFilterSettingRoot.class);
		if(enablePublisherTraces){
			addPluginEntry(ConveyorConfigurationGroup.KEY, TraceConveyorConfigurationGroup.class);
			addSettingRoot(DatarouterTracePublisherSettingRoot.class);
		}
		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, GuiceTraceFilter.class,
				FilterParamGrouping.DATAROUTER));
		setDaosModule(daosModule);
		addDatarouterGithubDocLink("datarouter-trace");
	}

	@Override
	public void configure(){
		bind(TracePublisher.class).to(tracePublisher);
		bind(TraceUrlBuilder.class).to(traceUrlBuilder);
	}

	public static class DatarouterTracePluginBuilder{

		private boolean enableTracePublisher = false;

		private DatarouterTraceDaoModule daoModule;

		private List<ClientId> traceQueueClientId;

		private Class<? extends TracePublisher> tracePublisher = NoOpTracePublisher.class;
		private Class<? extends TraceUrlBuilder> traceUrlBuilder = NoOpTraceUrlBuilder.class;

		public DatarouterTracePluginBuilder enableTracePublishing(
				List<ClientId> traceQueueClientId,
				Class<? extends TraceUrlBuilder> traceUrlBuilder){
			this.enableTracePublisher = true;
			this.traceQueueClientId = traceQueueClientId;
			this.tracePublisher = TracePublisherService.class;
			this.traceUrlBuilder = traceUrlBuilder;
			return this;
		}

		public DatarouterTracePlugin build(){
			DatarouterTraceDaoModule module;
			if(daoModule == null){
				module = new DatarouterTraceDaoModule(enableTracePublisher, traceQueueClientId);
			}else{
				module = daoModule;
			}
			return new DatarouterTracePlugin(
					enableTracePublisher,
					module,
					tracePublisher,
					traceUrlBuilder);
		}

	}

	public static class DatarouterTraceDaoModule extends DaosModuleBuilder{

		private final boolean enableTracePublisher;
		private final List<ClientId> traceQueueClientId;

		public DatarouterTraceDaoModule(boolean enableTracePublisher, List<ClientId> traceQueueClientId){
			this.enableTracePublisher = enableTracePublisher;
			this.traceQueueClientId = traceQueueClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(enableTracePublisher){
				daos.add(TraceQueueDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(enableTracePublisher){
				bind(TraceQueueDaoParams.class).toInstance(new TraceQueueDaoParams(traceQueueClientId));
			}
		}

	}

}
