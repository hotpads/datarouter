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
import io.datarouter.exception.service.DatarouterDebuggingRecordService;
import io.datarouter.exception.service.DefaultExceptionHandlingConfig;
import io.datarouter.exception.service.DefaultExceptionRecorder;
import io.datarouter.exception.service.ExceptionGraphLink;
import io.datarouter.exception.service.ExceptionGraphLink.NoOpExceptionGraphLink;
import io.datarouter.exception.service.ExceptionRecordAggregationDailyDigest;
import io.datarouter.exception.service.IssueLinkPrefixService;
import io.datarouter.exception.service.IssueLinkPrefixService.NoOpIssueLinkPrefixService;
import io.datarouter.exception.storage.exceptionrecord.DatarouterDebuggingRecordDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterDebuggingRecordDao.DatarouterDebuggingRecordParams;
import io.datarouter.exception.storage.exceptionrecord.DatarouterNonProdDebuggingRecordDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterNonProdDebuggingRecordDao.DatarouterNonProdExceptionRecordParams;
import io.datarouter.exception.utils.nameparser.ExceptionNameParserRegistry;
import io.datarouter.exception.utils.nameparser.ExceptionNameParserRegistry.NoOpExceptionNameParserRegistry;
import io.datarouter.instrumentation.exception.DatarouterDebuggingRecordPublisher;
import io.datarouter.instrumentation.exception.DatarouterDebuggingRecordPublisher.NoOpDebuggingRecordPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryCollector;
import io.datarouter.instrumentation.exception.ExceptionRecordSummaryCollector.NoOpExceptionRecordSummaryCollector;
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

public class DatarouterExceptionPlugin extends BaseWebPlugin{

	private final Class<? extends ExceptionGraphLink> exceptionGraphLinkClass;
	private final Class<? extends ExceptionRecorder> exceptionRecorderClass;
	private final Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass;
	private final Class<? extends ExceptionRecordSummaryCollector> exceptionRecordSummaryCollectorClass;
	private final Class<? extends IssueLinkPrefixService> issueLinkPrefixService;
	private final Class<? extends ExceptionNameParserRegistry> exceptionNameParserRegistryClass;
	private final Class<? extends DatarouterDebuggingRecordPublisher> debuggingRecordPublisher;

	private DatarouterExceptionPlugin(DatarouterExceptionDaoModule daosModuleBuilder,
			Class<? extends ExceptionGraphLink> exceptionGraphLinkClass,
			Class<? extends ExceptionRecorder> exceptionRecorderClass,
			Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass,
			Class<? extends ExceptionRecordSummaryCollector> exceptionRecordSummaryCollectorClass,
			Class<? extends IssueLinkPrefixService> issueLinkPrefixService,
			Class<? extends ExceptionNameParserRegistry> exceptionNameParserRegistryClass,
			Class<? extends DatarouterDebuggingRecordPublisher> debuggingRecordPublisher){
		this.exceptionGraphLinkClass = exceptionGraphLinkClass;
		this.exceptionRecorderClass = exceptionRecorderClass;
		this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
		this.exceptionRecordSummaryCollectorClass = exceptionRecordSummaryCollectorClass;
		this.issueLinkPrefixService = issueLinkPrefixService;
		this.exceptionNameParserRegistryClass = exceptionNameParserRegistryClass;
		this.debuggingRecordPublisher = debuggingRecordPublisher;
		addFilterParamsOrdered(
				new FilterParams(
						false,
						DatarouterServletGuiceModule.ROOT_PATH,
						GuiceExceptionHandlingFilter.class,
						FilterParamGrouping.DATAROUTER),
				DatarouterWebPlugin.REQUEST_CACHING_FILTER_PARAMS);
		addRouteSet(DatarouterExceptionRouteSet.class);
		addSettingRoot(DatarouterExceptionSettingRoot.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-exception");
		if(!debuggingRecordPublisher.isInstance(NoOpDebuggingRecordPublisher.class)){
			addPluginEntry(ConveyorConfigurationGroup.KEY, ExceptionConveyorConfigurationGroup.class);
		}
		addDailyDigest(ExceptionRecordAggregationDailyDigest.class);
	}

	@Override
	protected void configure(){
		bindDefault(ExceptionGraphLink.class, exceptionGraphLinkClass);
		bindActual(ExceptionRecorder.class, exceptionRecorderClass);
		bindActual(ExceptionHandlingConfig.class, exceptionHandlingConfigClass);
		bind(ExceptionRecordSummaryCollector.class).to(exceptionRecordSummaryCollectorClass);
		bind(IssueLinkPrefixService.class).to(issueLinkPrefixService);
		bind(ExceptionNameParserRegistry.class).to(exceptionNameParserRegistryClass);
		bind(DatarouterDebuggingRecordPublisher.class).to(debuggingRecordPublisher);
	}

	public static class DatarouterExceptionPluginBuilder{

		private final ClientId nonProdQueueClientId;

		private List<ClientId> blobQueueClientIds;

		private Class<? extends ExceptionGraphLink> exceptionGraphLinkClass = NoOpExceptionGraphLink.class;
		private Class<? extends ExceptionRecorder> exceptionRecorderClass = DefaultExceptionRecorder.class;
		private Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass
				= DefaultExceptionHandlingConfig.class;
		private Class<? extends ExceptionRecordSummaryCollector> exceptionRecordSummaryCollector
				= NoOpExceptionRecordSummaryCollector.class;
		private Class<? extends IssueLinkPrefixService> issueLinkPrefixService = NoOpIssueLinkPrefixService.class;
		private Class<? extends ExceptionNameParserRegistry> exceptionNameParserRegistryClass
				= NoOpExceptionNameParserRegistry.class;
		private Class<? extends DatarouterDebuggingRecordPublisher> debuggingRecordPublisher
				= NoOpDebuggingRecordPublisher.class;

		public DatarouterExceptionPluginBuilder(ClientId nonProdQueueClientId){
			this.nonProdQueueClientId = nonProdQueueClientId;
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
				List<ClientId> blobQueueClientIds){
			this.debuggingRecordPublisher = DatarouterDebuggingRecordService.class;
			this.blobQueueClientIds = blobQueueClientIds;
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
					new DatarouterExceptionDaoModule(blobQueueClientIds, nonProdQueueClientId),
					exceptionGraphLinkClass,
					exceptionRecorderClass,
					exceptionHandlingConfigClass,
					exceptionRecordSummaryCollector,
					issueLinkPrefixService,
					exceptionNameParserRegistryClass,
					debuggingRecordPublisher);
		}

	}

	public static class DatarouterExceptionDaoModule extends DaosModuleBuilder{

		private final ClientId nonProdQueueClientId;

		private final List<ClientId> queueClientIds;

		public DatarouterExceptionDaoModule(
				List<ClientId> blobQueueClientIds,
				ClientId nonProdQueueClientId){

			this.queueClientIds = blobQueueClientIds;
			this.nonProdQueueClientId = nonProdQueueClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(queueClientIds != null){
				daos.add(DatarouterNonProdDebuggingRecordDao.class);
				daos.add(DatarouterDebuggingRecordDao.class);
			}
			return daos;
		}

		@Override
		public void configure(){
			if(queueClientIds != null){
				bind(DatarouterDebuggingRecordParams.class)
						.toInstance(new DatarouterDebuggingRecordParams(queueClientIds));
			}
			bind(DatarouterNonProdExceptionRecordParams.class)
					.toInstance(new DatarouterNonProdExceptionRecordParams(nonProdQueueClientId));
		}

	}

}