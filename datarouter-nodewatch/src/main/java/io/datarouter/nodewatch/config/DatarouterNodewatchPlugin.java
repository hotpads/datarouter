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
package io.datarouter.nodewatch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.datarouter.instrumentation.tablecount.TableCountPublisher;
import io.datarouter.instrumentation.tablecount.TableCountPublisher.NoOpTableCountPublisher;
import io.datarouter.joblet.setting.BaseJobletPlugin;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet;
import io.datarouter.nodewatch.service.GenericNodewatchClientConfiguration;
import io.datarouter.nodewatch.service.NodewatchClientConfiguration;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao.DatarouterTableSizeAlertThresholdDaoParams;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao.DatarouterLatestTableCountDaoParams;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao.DatarouterTableCountDaoParams;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao.DatarouterTableSampleDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.util.lang.ClassTool;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterNodewatchPlugin extends BaseJobletPlugin{

	private static final DatarouterNodewatchPaths PATHS = new DatarouterNodewatchPaths();

	private final Class<? extends NodewatchClientConfiguration> nodewatchClientConfigurationClass;
	private final List<ClientId> nodewatchClientIds;
	private final Class<? extends TableCountPublisher> tableCountPublisherClass;

	private DatarouterNodewatchPlugin(
			Class<? extends NodewatchClientConfiguration> nodewatchClientConfigurationClass,
			List<ClientId> nodewatchClientIds,
			DatarouterNodewatchDaoModule daosModuleBuilder,
			Class<? extends TableCountPublisher> tableCountPublisherClass){
		this.nodewatchClientConfigurationClass = nodewatchClientConfigurationClass;
		this.nodewatchClientIds = nodewatchClientIds;
		this.tableCountPublisherClass = tableCountPublisherClass;
		addRouteSet(DatarouterNodewatchRouteSet.class);
		addTriggerGroup(DatarouterNodewatchTriggerGroup.class);
		addSettingRoot(DatarouterNodewatchSettingRoot.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS, PATHS.datarouter.nodewatch.threshold,
				"Table Count Thresholds");
		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING, PATHS.datarouter.nodewatch.tableCount,
				"Latest Table Counts");
		addJobletType(TableSpanSamplerJoblet.JOBLET_TYPE);
		if(ClassTool.differentClass(tableCountPublisherClass, NoOpTableCountPublisher.class)){
			addAppListener(DatarouterTableCountPublisherAppListener.class);
		}
	}

	@Override
	public String getName(){
		return "DatarouterNodewatch";
	}

	@Override
	public void configure(){
		if(nodewatchClientConfigurationClass != null){
			bindActual(NodewatchClientConfiguration.class, nodewatchClientConfigurationClass);
		}else{
			bindActualInstance(NodewatchClientConfiguration.class, new GenericNodewatchClientConfiguration(
					nodewatchClientIds));
		}
		bind(TableCountPublisher.class).to(tableCountPublisherClass);
	}

	public static class DatarouterNodewatchPluginBuilder{

		private Class<? extends NodewatchClientConfiguration> nodewatchClientConfigurationClass;
		private Class<? extends TableCountPublisher> tableCountPublisherClass = NoOpTableCountPublisher.class;
		private final List<ClientId> nodewatchClientIds;

		private final ClientId defaultClientId;
		private DatarouterNodewatchDaoModule daoModule;

		public DatarouterNodewatchPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
			this.nodewatchClientIds = new ArrayList<>();
		}

		public DatarouterNodewatchPluginBuilder setDaoModule(DatarouterNodewatchDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterNodewatchPluginBuilder withNodewatchClientConfiguration(
				Class<? extends NodewatchClientConfiguration> nodewatchClientConfigurationClass){
			this.nodewatchClientConfigurationClass = nodewatchClientConfigurationClass;
			return this;
		}

		/*
		 * This does not include the default clientId
		 */
		public DatarouterNodewatchPluginBuilder withNodewatchClientIds(ClientId...clientIds){
			Stream.of(clientIds).forEach(this.nodewatchClientIds::add);
			return this;
		}

		public DatarouterNodewatchPluginBuilder addNodewatchClientId(ClientId clientId){
			nodewatchClientIds.add(clientId);
			return this;
		}

		public DatarouterNodewatchPluginBuilder withTableCountPublisher(
				Class<? extends TableCountPublisher> tableCountPublisherClass){
			this.tableCountPublisherClass = tableCountPublisherClass;
			return this;
		}

		public DatarouterNodewatchPlugin build(){
			return new DatarouterNodewatchPlugin(
					nodewatchClientConfigurationClass,
					nodewatchClientIds,
					daoModule == null
							? new DatarouterNodewatchDaoModule(defaultClientId, defaultClientId, defaultClientId,
									defaultClientId)
							: daoModule,
					tableCountPublisherClass);
		}

	}

	public static class DatarouterNodewatchDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterLatestTableCountClientId;
		private final ClientId datarouterTableCountClientId;
		private final ClientId datarouterTableSampleClientId;
		private final ClientId datarouterTableSizeAlertThresholdClientId;

		public DatarouterNodewatchDaoModule(
				ClientId datarouterLatestTableCountClientId,
				ClientId datarouterTableCountClientId,
				ClientId datarouterTableSampleClientId,
				ClientId datarouterTableSizeAlertThresholdClientId){
			this.datarouterLatestTableCountClientId = datarouterLatestTableCountClientId;
			this.datarouterTableCountClientId = datarouterTableCountClientId;
			this.datarouterTableSampleClientId = datarouterTableSampleClientId;
			this.datarouterTableSizeAlertThresholdClientId = datarouterTableSizeAlertThresholdClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterLatestTableCountDao.class,
					DatarouterTableCountDao.class,
					DatarouterTableSampleDao.class,
					DatarouterTableSizeAlertThresholdDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterLatestTableCountDaoParams.class)
					.toInstance(new DatarouterLatestTableCountDaoParams(datarouterLatestTableCountClientId));
			bind(DatarouterTableCountDaoParams.class)
					.toInstance(new DatarouterTableCountDaoParams(datarouterTableCountClientId));
			bind(DatarouterTableSampleDaoParams.class)
					.toInstance(new DatarouterTableSampleDaoParams(datarouterTableSampleClientId));
			bind(DatarouterTableSizeAlertThresholdDaoParams.class)
					.toInstance(new DatarouterTableSizeAlertThresholdDaoParams(
							datarouterTableSizeAlertThresholdClientId));
		}

	}

}
