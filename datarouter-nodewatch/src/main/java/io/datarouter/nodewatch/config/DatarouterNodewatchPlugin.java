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
package io.datarouter.nodewatch.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.instrumentation.tablecount.TableCountPublisher;
import io.datarouter.instrumentation.tablecount.TableCountPublisher.NoOpTableCountPublisher;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet;
import io.datarouter.nodewatch.metriclink.AppNodewatchMetricLinkPage;
import io.datarouter.nodewatch.metriclink.DatarouterNodewatchMetricLinkPage;
import io.datarouter.nodewatch.service.GenericNodewatchClientConfiguration;
import io.datarouter.nodewatch.service.NodewatchAboveThresholdsDailyDigest;
import io.datarouter.nodewatch.service.NodewatchClientConfiguration;
import io.datarouter.nodewatch.service.StaleTablesDailyDigest;
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
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterNodewatchPlugin extends BaseWebPlugin{

	public static final String NAME = "Nodewatch";
	private static final DatarouterNodewatchPaths PATHS = new DatarouterNodewatchPaths();
	public static final String NODE_WIDGET_TABLE_COUNT_PATH = PATHS.datarouter.nodewatch.table.nodeName
			.toSlashedString();

	private final List<ClientId> nodewatchClientIds;
	private final Class<? extends TableCountPublisher> tableCountPublisher;

	private DatarouterNodewatchPlugin(
			List<ClientId> nodewatchClientIds,
			DatarouterNodewatchDaoModule daosModuleBuilder,
			Class<? extends TableCountPublisher> tableCountPublisher,
			boolean enablePublishing){
		this.nodewatchClientIds = nodewatchClientIds;
		this.tableCountPublisher = tableCountPublisher;

		addDatarouterNavBarItem(
				DatarouterNavBarCategory.DATA,
				PATHS.datarouter.nodewatch.tables,
				"Nodewatch");
		addPluginEntry(TableSpanSamplerJoblet.JOBLET_TYPE);
		addRouteSet(DatarouterNodewatchRouteSet.class);
		addSettingRoot(DatarouterNodewatchSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterNodewatchTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-nodewatch");
		if(enablePublishing){
			addSettingRoot(DatarouterTableCountPublisherSettingRoot.class);
			addPluginEntry(BaseTriggerGroup.KEY, DatarouterTableCountPublisherTriggerGroup.class);
		}
		addDailyDigest(StaleTablesDailyDigest.class);
		addDailyDigest(NodewatchAboveThresholdsDailyDigest.class);

		addMetricLinkPages(AppNodewatchMetricLinkPage.class);
		addMetricLinkPages(DatarouterNodewatchMetricLinkPage.class);
	}

	@Override
	public void configure(){
		bindActualInstance(
				NodewatchClientConfiguration.class,
				new GenericNodewatchClientConfiguration(nodewatchClientIds));
		bind(TableCountPublisher.class).to(tableCountPublisher);
	}

	public static class DatarouterNodewatchPluginBuilder{

		private final List<ClientId> nodewatchClientIds;
		private final List<ClientId> defaultClientId;

		private Class<? extends TableCountPublisher> tableCountPublisherClass = NoOpTableCountPublisher.class;
		private boolean enablePublishing = false;

		public DatarouterNodewatchPluginBuilder(List<ClientId> defaultClientId){
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

		private final List<ClientId> latestTableCountClientIds;
		private final List<ClientId> tableCountClientIds;
		private final List<ClientId> tableSampleClientIds;
		private final List<ClientId> tableSizeAlertThresholdClientIds;

		public DatarouterNodewatchDaoModule(
				List<ClientId> latestTableCountClientIds,
				List<ClientId> tableCountClientIds,
				List<ClientId> tableSampleClientIds,
				List<ClientId> tableSizeAlertThresholdClientIds){
			this.latestTableCountClientIds = latestTableCountClientIds;
			this.tableCountClientIds = tableCountClientIds;
			this.tableSampleClientIds = tableSampleClientIds;
			this.tableSizeAlertThresholdClientIds = tableSizeAlertThresholdClientIds;
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
					.toInstance(new DatarouterLatestTableCountDaoParams(latestTableCountClientIds));
			bind(DatarouterTableCountDaoParams.class)
					.toInstance(new DatarouterTableCountDaoParams(tableCountClientIds));
			bind(DatarouterTableSampleDaoParams.class)
					.toInstance(new DatarouterTableSampleDaoParams(tableSampleClientIds));
			bind(DatarouterTableSizeAlertThresholdDaoParams.class)
					.toInstance(new DatarouterTableSizeAlertThresholdDaoParams(tableSizeAlertThresholdClientIds));
		}

	}

}
