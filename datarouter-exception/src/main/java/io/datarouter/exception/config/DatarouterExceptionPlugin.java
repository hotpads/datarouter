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
package io.datarouter.exception.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.conveyor.ConveyorConfigurationGroup;
import io.datarouter.exception.conveyors.ExceptionConveyorConfigurationGroup;
import io.datarouter.exception.filter.GuiceExceptionHandlingFilter;
import io.datarouter.exception.service.DatarouterExceptionPublisherService;
import io.datarouter.exception.service.DefaultExceptionHandlingConfig;
import io.datarouter.exception.service.DefaultExceptionRecorder;
import io.datarouter.exception.service.ExceptionGraphLink;
import io.datarouter.exception.service.ExceptionGraphLink.NoOpExceptionGraphLink;
import io.datarouter.exception.service.ExceptionRecordAggregationDailyDigest;
import io.datarouter.exception.service.IssueLinkPrefixService;
import io.datarouter.exception.service.IssueLinkPrefixService.NoOpIssueLinkPrefixService;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao.DatarouterExceptionRecordDaoParams;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordQueueDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordQueueDao.ExceptionRecordQueueDaoParams;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao.DatarouterHttpRequestRecordDaoParams;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordQueueDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordQueueDao.HttpRequestRecordQueueDaoParams;
import io.datarouter.exception.storage.taskexecutorrecord.TaskExecutorRecordDirectorySupplier;
import io.datarouter.exception.storage.taskexecutorrecord.TaskExecutorRecordDirectorySupplier.NoOpTaskExecutorRecordDirectorySupplier;
import io.datarouter.exception.storage.taskexecutorrecord.TaskExecutorRecordQueueDao;
import io.datarouter.exception.storage.taskexecutorrecord.TaskExecutorRecordQueueDao.TaskExecutorRecordQueueDaoParams;
import io.datarouter.exception.utils.nameparser.ExceptionNameParserRegistry;
import io.datarouter.exception.utils.nameparser.ExceptionNameParserRegistry.NoOpExceptionNameParserRegistry;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher.NoOpDatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryCollector;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryCollector.NoOpExceptionRecordSummaryCollector;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.config.DatarouterServletGuiceModule;
import io.datarouter.web.config.DatarouterWebPlugin;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterExceptionPlugin extends BaseWebPlugin{

	private final Class<? extends ExceptionGraphLink> exceptionGraphLinkClass;
	private final Class<? extends ExceptionRecorder> exceptionRecorderClass;
	private final Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass;
	private final Class<? extends DatarouterExceptionPublisher> exceptionRecordPublisher;
	private final Class<? extends TaskExecutorRecordDirectorySupplier> taskExecutorRecordDirectorySupplier;
	private final Class<? extends ExceptionRecordSummaryCollector> exceptionRecordSummaryCollectorClass;
	private final Class<? extends IssueLinkPrefixService> issueLinkPrefixService;
	private final Class<? extends ExceptionNameParserRegistry> exceptionNameParserRegistryClass;

	private DatarouterExceptionPlugin(DatarouterExceptionDaoModule daosModuleBuilder,
			Class<? extends ExceptionGraphLink> exceptionGraphLinkClass,
			Class<? extends ExceptionRecorder> exceptionRecorderClass,
			Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass,
			Class<? extends DatarouterExceptionPublisher> exceptionRecordPublisher,
			Class<? extends TaskExecutorRecordDirectorySupplier> taskExecutorRecordDirectorySupplier,
			Class<? extends ExceptionRecordSummaryCollector> exceptionRecordSummaryCollectorClass,
			Class<? extends IssueLinkPrefixService> issueLinkPrefixService,
			Class<? extends ExceptionNameParserRegistry> exceptionNameParserRegistryClass){
		this.exceptionGraphLinkClass = exceptionGraphLinkClass;
		this.exceptionRecorderClass = exceptionRecorderClass;
		this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
		this.exceptionRecordPublisher = exceptionRecordPublisher;
		this.taskExecutorRecordDirectorySupplier = taskExecutorRecordDirectorySupplier;
		this.exceptionRecordSummaryCollectorClass = exceptionRecordSummaryCollectorClass;
		this.issueLinkPrefixService = issueLinkPrefixService;
		this.exceptionNameParserRegistryClass = exceptionNameParserRegistryClass;
		addFilterParamsOrdered(
				new FilterParams(
						false,
						DatarouterServletGuiceModule.ROOT_PATH,
						GuiceExceptionHandlingFilter.class,
						FilterParamGrouping.DATAROUTER),
				DatarouterWebPlugin.REQUEST_CACHING_FILTER_PARAMS);
		addRouteSet(DatarouterExceptionRouteSet.class);
		addSettingRoot(DatarouterExceptionSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterExceptionTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.MONITORING,
				new DatarouterExceptionPaths().datarouter.exception.details,
				"Exceptions");
		addDatarouterGithubDocLink("datarouter-exception");
		if(!exceptionRecordPublisher.isInstance(NoOpDatarouterExceptionPublisher.class)){
			addPluginEntry(ConveyorConfigurationGroup.KEY, ExceptionConveyorConfigurationGroup.class);
		}
		addDailyDigest(ExceptionRecordAggregationDailyDigest.class);
	}

	@Override
	protected void configure(){
		bindDefault(ExceptionGraphLink.class, exceptionGraphLinkClass);
		bindActual(ExceptionRecorder.class, exceptionRecorderClass);
		bindActual(ExceptionHandlingConfig.class, exceptionHandlingConfigClass);
		bind(DatarouterExceptionPublisher.class).to(exceptionRecordPublisher);
		bind(TaskExecutorRecordDirectorySupplier.class).to(taskExecutorRecordDirectorySupplier);
		bind(ExceptionRecordSummaryCollector.class).to(exceptionRecordSummaryCollectorClass);
		bind(IssueLinkPrefixService.class).to(issueLinkPrefixService);
		bind(ExceptionNameParserRegistry.class).to(exceptionNameParserRegistryClass);
	}

	public static class DatarouterExceptionPluginBuilder{

		private final List<ClientId> defaultClientIds;

		private List<ClientId> blobQueueClientIds;

		private Class<? extends ExceptionGraphLink> exceptionGraphLinkClass = NoOpExceptionGraphLink.class;
		private Class<? extends ExceptionRecorder> exceptionRecorderClass = DefaultExceptionRecorder.class;
		private Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass
				= DefaultExceptionHandlingConfig.class;
		private Class<? extends DatarouterExceptionPublisher> exceptionRecordPublisher
				= NoOpDatarouterExceptionPublisher.class;
		private Class<? extends TaskExecutorRecordDirectorySupplier> taskExecutorRecordDirectorySupplier
				= NoOpTaskExecutorRecordDirectorySupplier.class;
		private Class<? extends ExceptionRecordSummaryCollector> exceptionRecordSummaryCollector
				= NoOpExceptionRecordSummaryCollector.class;
		private Class<? extends IssueLinkPrefixService> issueLinkPrefixService = NoOpIssueLinkPrefixService.class;
		private Class<? extends ExceptionNameParserRegistry> exceptionNameParserRegistryClass
				= NoOpExceptionNameParserRegistry.class;

		public DatarouterExceptionPluginBuilder(List<ClientId> defaultClientIds){
			this.defaultClientIds = defaultClientIds;
		}

		public DatarouterExceptionPluginBuilder setExceptionGraphLinkClass(
				Class<? extends ExceptionGraphLink> exceptionGraphLinkClass){
			this.exceptionGraphLinkClass = exceptionGraphLinkClass;
			return this;
		}

		public DatarouterExceptionPluginBuilder setExceptionRecorderClass(
				Class<? extends ExceptionRecorder> exceptionRecorderClass){
			this.exceptionRecorderClass = exceptionRecorderClass;
			return this;
		}

		public DatarouterExceptionPluginBuilder setExceptionHandlingClass(
				Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass){
			this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
			return this;
		}

		public DatarouterExceptionPluginBuilder setExceptionRecordSummaryCollector(
				Class<? extends ExceptionRecordSummaryCollector> exceptionRecordSummaryCollector){
			this.exceptionRecordSummaryCollector = exceptionRecordSummaryCollector;
			return this;
		}

		public DatarouterExceptionPluginBuilder enablePublishing(
				List<ClientId> blobQueueClientIds,
				Class<? extends TaskExecutorRecordDirectorySupplier> taskExecutorRecordDirectorySupplier){
			this.exceptionRecordPublisher = DatarouterExceptionPublisherService.class;
			this.blobQueueClientIds = blobQueueClientIds;
			this.taskExecutorRecordDirectorySupplier = taskExecutorRecordDirectorySupplier;
			return this;
		}

		public DatarouterExceptionPluginBuilder withIssueLinkPrefixService(
				Class<? extends IssueLinkPrefixService> issueLinkPrefixService){
			this.issueLinkPrefixService = issueLinkPrefixService;
			return this;
		}

		public DatarouterExceptionPluginBuilder withExceptionNameParserRegistryClass(
				Class<? extends ExceptionNameParserRegistry> exceptionNameParserRegistryClass){
			this.exceptionNameParserRegistryClass = exceptionNameParserRegistryClass;
			return this;
		}

		public DatarouterExceptionPlugin build(){
			return new DatarouterExceptionPlugin(
					new DatarouterExceptionDaoModule(defaultClientIds, blobQueueClientIds),
					exceptionGraphLinkClass,
					exceptionRecorderClass,
					exceptionHandlingConfigClass,
					exceptionRecordPublisher,
					taskExecutorRecordDirectorySupplier,
					exceptionRecordSummaryCollector,
					issueLinkPrefixService,
					exceptionNameParserRegistryClass);
		}

	}

	public static class DatarouterExceptionDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterExceptionRecordClientId;
		private final List<ClientId> datarouterHttpRequestRecordClientId;

		private final List<ClientId> queueClientIds;

		public DatarouterExceptionDaoModule(List<ClientId> defaultClientIds, List<ClientId> blobQueueClientIds){
			this(defaultClientIds, defaultClientIds, blobQueueClientIds);
		}

		public DatarouterExceptionDaoModule(
				List<ClientId> datarouterExceptionRecordClientId,
				List<ClientId> datarouterHttpRequestRecordClientId,
				List<ClientId> blobQueueClientIds){
			this.datarouterExceptionRecordClientId = datarouterExceptionRecordClientId;
			this.datarouterHttpRequestRecordClientId = datarouterHttpRequestRecordClientId;

			this.queueClientIds = blobQueueClientIds;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			daos.add(DatarouterExceptionRecordDao.class);
			daos.add(DatarouterHttpRequestRecordDao.class);
			if(queueClientIds != null){
				daos.add(ExceptionRecordQueueDao.class);
				daos.add(HttpRequestRecordQueueDao.class);
				daos.add(TaskExecutorRecordQueueDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			bind(DatarouterHttpRequestRecordDaoParams.class)
					.toInstance(new DatarouterHttpRequestRecordDaoParams(datarouterHttpRequestRecordClientId));
			bind(DatarouterExceptionRecordDaoParams.class)
					.toInstance(new DatarouterExceptionRecordDaoParams(datarouterExceptionRecordClientId));

			if(queueClientIds != null){
				bind(ExceptionRecordQueueDaoParams.class)
						.toInstance(new ExceptionRecordQueueDaoParams(queueClientIds));
				bind(HttpRequestRecordQueueDaoParams.class)
						.toInstance(new HttpRequestRecordQueueDaoParams(queueClientIds));
				bind(TaskExecutorRecordQueueDaoParams.class)
						.toInstance(new TaskExecutorRecordQueueDaoParams(queueClientIds));
			}
		}

	}

}