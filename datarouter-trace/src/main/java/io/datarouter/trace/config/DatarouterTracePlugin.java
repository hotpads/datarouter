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
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.trace.conveyor.publisher.TraceConveyors;
import io.datarouter.trace.filter.GuiceTraceFilter;
import io.datarouter.trace.service.TraceBlobService;
import io.datarouter.trace.service.TraceUrlBuilder;
import io.datarouter.trace.service.TraceUrlBuilder.NoOpTraceUrlBuilder;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.trace.settings.DatarouterTracePublisherSettingRoot;
import io.datarouter.trace.settings.TraceBlobPublishingSettings;
import io.datarouter.trace.settings.TraceBlobPublishingSettings.NoOpTraceBlobPublishingSettings;
import io.datarouter.trace.storage.trace.TraceBlobDirectorySupplier;
import io.datarouter.trace.storage.trace.TraceBlobDirectorySupplier.NoOpTraceBlobDirectorySupplier;
import io.datarouter.trace.storage.trace.TraceBlobQueueDao;
import io.datarouter.trace.storage.trace.TraceBlobQueueDao.TraceBlobQueueDaoParams;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.config.DatarouterServletGuiceModule;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;

public class DatarouterTracePlugin extends BaseWebPlugin{

	private final Class<? extends TracePublisher> tracePublisher;
	private final Class<? extends TraceUrlBuilder> traceUrlBuilder;
	private final Class<? extends TraceBlobDirectorySupplier> traceBlobDirectorySupplier;
	private final Class<? extends TraceBlobPublishingSettings> traceBlobPublishingSettings;
	private final int maxTraceBlobSize;

	private DatarouterTracePlugin(
			boolean enablePublisherTraces,
			DatarouterTraceDaoModule daosModule,
			Class<? extends TracePublisher> tracePublisher,
			Class<? extends TraceUrlBuilder> traceUrlBuilder,
			Class<? extends TraceBlobDirectorySupplier> traceBlobDirectorySupplier,
			Class<? extends TraceBlobPublishingSettings> traceBlobPublishingSettings,
			int maxTraceBlobSize){
		this.tracePublisher = tracePublisher;
		this.traceUrlBuilder = traceUrlBuilder;
		this.traceBlobDirectorySupplier = traceBlobDirectorySupplier;
		this.traceBlobPublishingSettings = traceBlobPublishingSettings;
		this.maxTraceBlobSize = maxTraceBlobSize;

		addSettingRoot(DatarouterTraceFilterSettingRoot.class);
		if(enablePublisherTraces){
			addAppListener(TraceConveyors.class);
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
		bind(TraceBlobDirectorySupplier.class).to(traceBlobDirectorySupplier);
		bind(TraceBlobPublishingSettings.class).to(traceBlobPublishingSettings);
		bind(MaxTraceBlobSize.class).toInstance(() -> maxTraceBlobSize);
	}

	public static class DatarouterTracePluginBuilder{

		private boolean enableTracePublisher = false;

		private DatarouterTraceDaoModule daoModule;

		private List<ClientId> traceBlobQueueClientId;

		private Class<? extends TracePublisher> tracePublisher = NoOpTracePublisher.class;
		private Class<? extends TraceUrlBuilder> traceUrlBuilder = NoOpTraceUrlBuilder.class;
		private Class<? extends TraceBlobDirectorySupplier> traceBlobDirectorySupplier =
				NoOpTraceBlobDirectorySupplier.class;
		private Class<? extends TraceBlobPublishingSettings> traceBlobPublishingSettings =
				NoOpTraceBlobPublishingSettings.class;
		private int maxTraceBlobSize = 0;

		public DatarouterTracePluginBuilder enableTracePublishing(
				List<ClientId> traceBlobQueueClientId,
				Class<? extends TraceUrlBuilder> traceUrlBuilder,
				Class<? extends TraceBlobDirectorySupplier> traceBlobDirectorySupplier,
				Class<? extends TraceBlobPublishingSettings> traceBlobPublishingSettings,
				int maxTraceBlobSize){
			this.enableTracePublisher = true;
			this.traceBlobQueueClientId = traceBlobQueueClientId;
			this.tracePublisher = TraceBlobService.class;
			this.traceUrlBuilder = traceUrlBuilder;
			this.traceBlobDirectorySupplier = traceBlobDirectorySupplier;
			this.traceBlobPublishingSettings = traceBlobPublishingSettings;
			this.maxTraceBlobSize = maxTraceBlobSize;
			return this;
		}

		public DatarouterTracePlugin build(){
			DatarouterTraceDaoModule module;
			if(daoModule == null){
				module = new DatarouterTraceDaoModule(enableTracePublisher, traceBlobQueueClientId);
			}else{
				module = daoModule;
			}
			return new DatarouterTracePlugin(
					enableTracePublisher,
					module,
					tracePublisher,
					traceUrlBuilder,
					traceBlobDirectorySupplier,
					traceBlobPublishingSettings,
					maxTraceBlobSize);
		}

	}

	public static class DatarouterTraceDaoModule extends DaosModuleBuilder{

		private final boolean enableTracePublisher;
		private final List<ClientId> traceBlobQueueClientId;

		public DatarouterTraceDaoModule(boolean enableTracePublisher, List<ClientId> traceBlobQueueClientId){
			this.enableTracePublisher = enableTracePublisher;
			this.traceBlobQueueClientId = traceBlobQueueClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(enableTracePublisher){
				daos.add(TraceBlobQueueDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(enableTracePublisher){
				bind(TraceBlobQueueDaoParams.class).toInstance(new TraceBlobQueueDaoParams(traceBlobQueueClientId));
			}
		}

	}

}
