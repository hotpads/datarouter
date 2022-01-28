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
package io.datarouter.trace.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.instrumentation.trace.TracePublisher.NoOpTracePublisher;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.trace.conveyor.local.LocalTraceConveyors;
import io.datarouter.trace.conveyor.local.Trace2ForLocalHttpRequestRecordQueueDao;
import io.datarouter.trace.conveyor.local.Trace2ForLocalHttpRequestRecordQueueDao.Trace2ForLocalHttpRequestRecordQueueDaoParams;
import io.datarouter.trace.conveyor.local.Trace2ForLocalQueueDao;
import io.datarouter.trace.conveyor.local.Trace2ForLocalQueueDao.Trace2ForLocalQueueDaoParams;
import io.datarouter.trace.conveyor.publisher.Trace2ForPublisherHttpRequestRecordQueueDao;
import io.datarouter.trace.conveyor.publisher.Trace2ForPublisherHttpRequestRecordQueueDao.Trace2ForPublisherHttpRequestRecordQueueDaoParams;
import io.datarouter.trace.conveyor.publisher.Trace2ForPublisherQueueDao;
import io.datarouter.trace.conveyor.publisher.Trace2ForPublisherQueueDao.Trace2ForPublisherQueueDaoParams;
import io.datarouter.trace.conveyor.publisher.TracePublisherConveyors;
import io.datarouter.trace.filter.GuiceTraceFilter;
import io.datarouter.trace.service.TraceUrlBuilder;
import io.datarouter.trace.service.TraceUrlBuilder.LocalTraceUrlBulder;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.trace.settings.DatarouterTraceLocalSettingRoot;
import io.datarouter.trace.settings.DatarouterTracePublisherSettingRoot;
import io.datarouter.trace.storage.Trace2ForLocalDao;
import io.datarouter.trace.storage.Trace2ForLocalDao.Trace2ForLocalDaoParams;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.config.DatarouterServletGuiceModule;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterTracePlugin extends BaseWebPlugin{

	private final Class<? extends TracePublisher> tracePublisher;
	private final Class<? extends TraceUrlBuilder> traceUrlBuilder;

	@SuppressWarnings("unused")
	private final boolean enableLocalTraces;
	private final boolean enablePublisherTraces;

	private DatarouterTracePlugin(
			boolean enableLocalTraces,
			boolean enablePublisherTraces,
			DatarouterTraceDaoModule daosModule,
			Class<? extends TracePublisher> tracePublisher,
			boolean addLocalVacuumJobs,
			Class<? extends TraceUrlBuilder> traceUrlBuilder){
		this.tracePublisher = tracePublisher;
		this.enableLocalTraces = enableLocalTraces;
		this.enablePublisherTraces = enablePublisherTraces;
		this.traceUrlBuilder = traceUrlBuilder;

		addSettingRoot(DatarouterTraceFilterSettingRoot.class);
		if(enableLocalTraces){
			addAppListener(LocalTraceConveyors.class);
			addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING, new DatarouterTracePaths().datarouter.traces,
					"Traces");
			addSettingRoot(DatarouterTraceLocalSettingRoot.class);
			if(addLocalVacuumJobs){
				addPluginEntry(BaseTriggerGroup.KEY, DatarouterLocalTraceTriggerGroup.class);
			}
		}
		if(enablePublisherTraces){
			addAppListener(TracePublisherConveyors.class);
			addSettingRoot(DatarouterTracePublisherSettingRoot.class);
		}
		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, GuiceTraceFilter.class,
				FilterParamGrouping.DATAROUTER));
		setDaosModule(daosModule);
		addDatarouterGithubDocLink("datarouter-trace");
	}

	@Override
	public void configure(){
		if(enablePublisherTraces){
			bind(TracePublisher.class).to(tracePublisher);
		}else{
			bind(TracePublisher.class).to(NoOpTracePublisher.class);
		}

		bind(TraceUrlBuilder.class).to(traceUrlBuilder);
	}

	public static class DatarouterTracePluginBuilder{

		private boolean enableTraceLocal = false;
		private boolean enableTracePublisher = false;

		private DatarouterTraceDaoModule daoModule;

		private ClientId localTraceClientId;
		private List<ClientId> localTraceQueueClientId;

		private List<ClientId> publishingTraceQueueClientId;
		private Class<? extends TracePublisher> tracePublisher;
		private boolean addLocalVacuumJobs = false;

		private Class<? extends TraceUrlBuilder> traceUrlBuilder = LocalTraceUrlBulder.class;

		/**
		 * @param localTraceClientId clientId for trace entities
		 * @param localTraceQueueClientId clientId for buffering
		 * @param addLocalVacuumJobs Traces have a ttl of 30 days. Clients like hbase and bigtable will get the ttl
		 *        from the trace fielder. Other datastores like mysql and spanner will need this enabled to register the
		 *        vacuum jobs.
		 * @return the builder
		 */
		public DatarouterTracePluginBuilder enableTraceLocal(
				ClientId localTraceClientId,
				List<ClientId> localTraceQueueClientId,
				boolean addLocalVacuumJobs){
			this.enableTraceLocal = true;
			this.localTraceClientId = localTraceClientId;
			this.localTraceQueueClientId = localTraceQueueClientId;
			this.addLocalVacuumJobs = addLocalVacuumJobs;
			return this;
		}

		public DatarouterTracePluginBuilder enableTracePublishing(
				List<ClientId> publishingTraceQueueClientId,
				Class<? extends TracePublisher> tracePublisher){
			this.enableTracePublisher = true;
			this.publishingTraceQueueClientId = publishingTraceQueueClientId;
			this.tracePublisher = tracePublisher;
			return this;
		}

		public DatarouterTracePluginBuilder setDaosModule(
				boolean enableLocalTraces,
				boolean enableTracePublisher,
				ClientId localTraceClientId,
				List<ClientId> localTraceQueueClientId,
				List<ClientId> publishingTraceQueueClientId){
			this.daoModule = new DatarouterTraceDaoModule(
					enableLocalTraces,
					enableTracePublisher,
					localTraceClientId,
					localTraceQueueClientId,
					publishingTraceQueueClientId);
			return this;
		}

		public DatarouterTracePluginBuilder setTraceUrlBuilder(Class<? extends TraceUrlBuilder> traceUrlBuilder){
			this.traceUrlBuilder = traceUrlBuilder;
			return this;
		}

		public DatarouterTracePlugin build(){
			DatarouterTraceDaoModule module;
			if(daoModule == null){
				module = new DatarouterTraceDaoModule(
						enableTraceLocal,
						enableTracePublisher,
						localTraceClientId,
						localTraceQueueClientId,
						publishingTraceQueueClientId);
			}else{
				module = daoModule;
			}
			return new DatarouterTracePlugin(enableTraceLocal, enableTracePublisher, module, tracePublisher,
					addLocalVacuumJobs,
					traceUrlBuilder);
		}

	}

	public static class DatarouterTraceDaoModule extends DaosModuleBuilder{

		private final boolean enableLocalTraces;
		private final boolean enableTracePublisher;
		private final ClientId localTraceClientId;
		private final List<ClientId> localTraceQueueClientId;
		private final List<ClientId> publishingTraceQueueClientId;

		public DatarouterTraceDaoModule(
				boolean enableTraceLocal,
				boolean enableTracePublisher,
				ClientId localTraceClientId,
				List<ClientId> localTraceQueueClientId,
				List<ClientId> publishingTraceQueueClientId){
			this.enableLocalTraces = enableTraceLocal;
			this.enableTracePublisher = enableTracePublisher;
			this.localTraceClientId = localTraceClientId;
			this.localTraceQueueClientId = localTraceQueueClientId;
			this.publishingTraceQueueClientId = publishingTraceQueueClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(enableLocalTraces){
				daos.add(Trace2ForLocalQueueDao.class);
				daos.add(Trace2ForLocalDao.class);
				daos.add(Trace2ForLocalHttpRequestRecordQueueDao.class);
			}
			if(enableTracePublisher){
				daos.add(Trace2ForPublisherQueueDao.class);
				daos.add(Trace2ForPublisherHttpRequestRecordQueueDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(enableLocalTraces){
				bind(Trace2ForLocalDaoParams.class)
						.toInstance(new Trace2ForLocalDaoParams(localTraceClientId));
				bind(Trace2ForLocalQueueDaoParams.class)
						.toInstance(new Trace2ForLocalQueueDaoParams(localTraceQueueClientId));
				bind(Trace2ForLocalHttpRequestRecordQueueDaoParams.class)
						.toInstance(new Trace2ForLocalHttpRequestRecordQueueDaoParams(localTraceQueueClientId));
			}
			if(enableTracePublisher){
				bind(Trace2ForPublisherQueueDaoParams.class)
						.toInstance(new Trace2ForPublisherQueueDaoParams(publishingTraceQueueClientId));
				bind(Trace2ForPublisherHttpRequestRecordQueueDaoParams.class)
						.toInstance(new Trace2ForPublisherHttpRequestRecordQueueDaoParams(
								publishingTraceQueueClientId));
			}
		}

	}

}
