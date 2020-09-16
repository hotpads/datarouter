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
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterNodewatchPlugin extends BaseJobletPlugin{

	private static final DatarouterNodewatchPaths PATHS = new DatarouterNodewatchPaths();
	public static final String NODE_WIDGET_TABLE_COUNT_PATH = PATHS.datarouter.nodewatch.tableCount.toSlashedString()
			+ "?submitAction=singleTableWithNodeName";

	private final List<ClientId> nodewatchClientIds;
	private final Class<? extends TableCountPublisher> tableCountPublisher;

	private DatarouterNodewatchPlugin(
			List<ClientId> nodewatchClientIds,
			DatarouterNodewatchDaoModule daosModuleBuilder,
			Class<? extends TableCountPublisher> tableCountPublisher,
			boolean enablePublishing){
		this.nodewatchClientIds = nodewatchClientIds;
		this.tableCountPublisher = tableCountPublisher;

		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS, PATHS.datarouter.nodewatch.threshold,
				"Table Count Thresholds");
		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING, PATHS.datarouter.nodewatch.tableCount,
				"Latest Table Counts");
		addJobletType(TableSpanSamplerJoblet.JOBLET_TYPE);
		addRouteSet(DatarouterNodewatchRouteSet.class);
		addSettingRoot(DatarouterNodewatchSettingRoot.class);
		addTriggerGroup(DatarouterNodewatchTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-nodewatch");
		if(enablePublishing){
			addSettingRoot(DatarouterTableCountPublisherSettingRoot.class);
			addTriggerGroup(DatarouterTableCountPublisherTriggerGroup.class);
		}
	}

	@Override
	public String getName(){
		return "DatarouterNodewatch";
	}

	@Override
	public void configure(){
		bindActualInstance(NodewatchClientConfiguration.class, new GenericNodewatchClientConfiguration(
				nodewatchClientIds));
		bind(TableCountPublisher.class).to(tableCountPublisher);
	}

	public static class DatarouterNodewatchPluginBuilder{

		private final List<ClientId> nodewatchClientIds;
		private final ClientId defaultClientId;

		private Class<? extends TableCountPublisher> tableCountPublisherClass = NoOpTableCountPublisher.class;
		private boolean enablePublishing = false;

		public DatarouterNodewatchPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
			this.nodewatchClientIds = new ArrayList<>();
		}

		public DatarouterNodewatchPluginBuilder addNodewatchClientId(ClientId clientId){
			nodewatchClientIds.add(clientId);
			return this;
		}

		public DatarouterNodewatchPluginBuilder enablePublishing(
				Class<? extends TableCountPublisher> tableCountPublisherClass){
			this.enablePublishing = true;
			this.tableCountPublisherClass = tableCountPublisherClass;
			return this;
		}

		public DatarouterNodewatchPlugin build(){
			return new DatarouterNodewatchPlugin(
					nodewatchClientIds,
					new DatarouterNodewatchDaoModule(defaultClientId, defaultClientId, defaultClientId,
							defaultClientId),
					tableCountPublisherClass,
					enablePublishing);
		}

	}

	public static class DatarouterNodewatchDaoModule extends DaosModuleBuilder{

		private final ClientId latestTableCountClientId;
		private final ClientId tableCountClientId;
		private final ClientId tableSampleClientId;
		private final ClientId tableSizeAlertThresholdClientId;

		public DatarouterNodewatchDaoModule(
				ClientId latestTableCountClientId,
				ClientId tableCountClientId,
				ClientId tableSampleClientId,
				ClientId tableSizeAlertThresholdClientId){
			this.latestTableCountClientId = latestTableCountClientId;
			this.tableCountClientId = tableCountClientId;
			this.tableSampleClientId = tableSampleClientId;
			this.tableSizeAlertThresholdClientId = tableSizeAlertThresholdClientId;
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
					.toInstance(new DatarouterLatestTableCountDaoParams(latestTableCountClientId));
			bind(DatarouterTableCountDaoParams.class).toInstance(new DatarouterTableCountDaoParams(tableCountClientId));
			bind(DatarouterTableSampleDaoParams.class)
					.toInstance(new DatarouterTableSampleDaoParams(tableSampleClientId));
			bind(DatarouterTableSizeAlertThresholdDaoParams.class)
					.toInstance(new DatarouterTableSizeAlertThresholdDaoParams(tableSizeAlertThresholdClientId));
		}

	}

}
