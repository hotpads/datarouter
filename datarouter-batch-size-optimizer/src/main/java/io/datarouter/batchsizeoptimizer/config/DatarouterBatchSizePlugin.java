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
package io.datarouter.batchsizeoptimizer.config;

import java.util.List;

import io.datarouter.batchsizeoptimizer.job.DatarouterBatchSizeOptimizerTriggerGroup;
import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.DatarouterOpOptimizedBatchSizeDao;
import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.DatarouterOpOptimizedBatchSizeDao.DatarouterOpOptimizedBatchSizeDaoParams;
import io.datarouter.batchsizeoptimizer.storage.performancerecord.DatarouterOpPerformanceRecordDao;
import io.datarouter.batchsizeoptimizer.storage.performancerecord.DatarouterOpPerformanceRecordDao.DatarouterOpPerformanceRecordDaoParams;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterBatchSizePlugin extends BaseJobPlugin{

	private DatarouterBatchSizePlugin(DatarouterBatchSizeOptimizerDaoModule daosModuleBuilder){
		addRouteSet(DatarouterBatchSizeOptimizerRouteSet.class);
		addSettingRoot(DatarouterBatchSizeOptimizerSettings.class);
		addTriggerGroup(DatarouterBatchSizeOptimizerTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS,
				new DatarouterBatchSizeOptimizerPaths().datarouter.batchSizeOptimizer, "Batch Size Optimizer");
	}

	@Override
	public String getName(){
		return "DatarouterBatchSizeOptimizer";
	}

	public static class DatarouterBatchSizePluginBuilder{

		private final ClientId defaultClientId;

		public DatarouterBatchSizePluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}


		public DatarouterBatchSizePlugin build(){
			return new DatarouterBatchSizePlugin(
					new DatarouterBatchSizeOptimizerDaoModule(defaultClientId, defaultClientId));
		}

	}

	public static class DatarouterBatchSizeOptimizerDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterOpOptimizedBatchSizeClientId;
		private final ClientId datarouterOpPerformanceRecordClientId;

		public DatarouterBatchSizeOptimizerDaoModule(
				ClientId datarouterOpOptimizedBatchSizeClientId,
				ClientId datarouterOpPerformanceRecordClientId){
			this.datarouterOpOptimizedBatchSizeClientId = datarouterOpOptimizedBatchSizeClientId;
			this.datarouterOpPerformanceRecordClientId = datarouterOpPerformanceRecordClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterOpPerformanceRecordDao.class,
					DatarouterOpOptimizedBatchSizeDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterOpOptimizedBatchSizeDaoParams.class)
					.toInstance(new DatarouterOpOptimizedBatchSizeDaoParams(datarouterOpOptimizedBatchSizeClientId));
			bind(DatarouterOpPerformanceRecordDaoParams.class)
					.toInstance(new DatarouterOpPerformanceRecordDaoParams(datarouterOpPerformanceRecordClientId));
		}

	}

}
