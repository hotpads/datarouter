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
package io.datarouter.loadtest.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.loadtest.service.LoadTestGetDao;
import io.datarouter.loadtest.service.LoadTestGetDao.NoOpLoadTestGetDao;
import io.datarouter.loadtest.service.LoadTestInsertDao;
import io.datarouter.loadtest.service.LoadTestInsertDao.NoOpLoadTestInsertDao;
import io.datarouter.loadtest.service.LoadTestScanDao;
import io.datarouter.loadtest.service.LoadTestScanDao.NoOpLoadTestScanDao;
import io.datarouter.loadtest.storage.DatarouterLoadTestGetDao;
import io.datarouter.loadtest.storage.DatarouterLoadTestGetDao.DatarouterLoadTestGetDaoParams;
import io.datarouter.loadtest.storage.DatarouterLoadTestInsertDao;
import io.datarouter.loadtest.storage.DatarouterLoadTestInsertDao.DatarouterLoadTestInsertDaoParams;
import io.datarouter.loadtest.storage.DatarouterLoadTestScanDao;
import io.datarouter.loadtest.storage.DatarouterLoadTestScanDao.DatarouterLoadTestScanDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterLoadTestPlugin extends BaseWebPlugin{

	private static final DatarouterLoadTestPaths PATHS = new DatarouterLoadTestPaths();

	private final ClientId loadTestGetClientId;
	private final ClientId loadTestInsertClientId;
	private final ClientId loadTestScanClientId;

	private DatarouterLoadTestPlugin(
			DatarouterLoadTestDaosModule daosModule,
			ClientId loadTestGetClientId,
			ClientId loadTestInsertClientId,
			ClientId loadTestScanClientId){
		this.loadTestGetClientId = loadTestGetClientId;
		this.loadTestInsertClientId = loadTestInsertClientId;
		this.loadTestScanClientId = loadTestScanClientId;

		addRouteSet(DatarouterLoadTestRouteSet.class);
		if(loadTestGetClientId != null){
			addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.loadTest.get, "LoadTest - Get");
		}
		if(loadTestInsertClientId != null){
			addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.loadTest.insert,
					"LoadTest - Insert");
		}
		if(loadTestScanClientId != null){
			addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.loadTest.scan, "LoadTest - Scan");
		}
		setDaosModule(daosModule);
	}

	@Override
	public String getName(){
		return "DatarouterLoadTest";
	}

	@Override
	protected void configure(){
		if(loadTestGetClientId == null){
			bind(LoadTestGetDao.class).to(NoOpLoadTestGetDao.class);
		}else{
			bind(LoadTestGetDao.class).to(DatarouterLoadTestGetDao.class);
		}

		if(loadTestInsertClientId == null){
			bind(LoadTestInsertDao.class).to(NoOpLoadTestInsertDao.class);
		}else{
			bind(LoadTestInsertDao.class).to(DatarouterLoadTestInsertDao.class);
		}

		if(loadTestScanClientId == null){
			bind(LoadTestScanDao.class).to(NoOpLoadTestScanDao.class);
		}else{
			bind(LoadTestScanDao.class).to(DatarouterLoadTestScanDao.class);
		}
	}

	public static class DatarouterLoadTestPluginBuilder{

		private ClientId loadTestGetClientId;
		private ClientId loadTestInsertClientId;
		private ClientId loadTestScanClientId;

		public DatarouterLoadTestPluginBuilder setLoadTestGetClientId(ClientId loadTestGetClientId){
			this.loadTestGetClientId = loadTestGetClientId;
			return this;
		}

		public DatarouterLoadTestPluginBuilder setLoadTestInsertClientId(ClientId loadTestInsertClientId){
			this.loadTestInsertClientId = loadTestInsertClientId;
			return this;
		}

		public DatarouterLoadTestPluginBuilder setLoadTestScanClientId(ClientId loadTestScanClientId){
			this.loadTestScanClientId = loadTestScanClientId;
			return this;
		}

		public DatarouterLoadTestPlugin build(){
			var daosModule = new DatarouterLoadTestDaosModule(
					loadTestGetClientId,
					loadTestInsertClientId,
					loadTestScanClientId);
			return new DatarouterLoadTestPlugin(
					daosModule,
					loadTestGetClientId,
					loadTestInsertClientId,
					loadTestScanClientId);
		}

	}

	private static class DatarouterLoadTestDaosModule extends DaosModuleBuilder{

		private final ClientId loadTestGetClientId;
		private final ClientId loadTestInsertClientId;
		private final ClientId loadTestScanClientId;

		private DatarouterLoadTestDaosModule(
				ClientId loadTestGetClientId,
				ClientId loadTestInsertClientId,
				ClientId loadTestScanClientId){
			this.loadTestGetClientId = loadTestGetClientId;
			this.loadTestInsertClientId = loadTestInsertClientId;
			this.loadTestScanClientId = loadTestScanClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			List<Class<? extends Dao>> daos = new ArrayList<>();
			if(loadTestGetClientId != null){
				daos.add(DatarouterLoadTestGetDao.class);
			}
			if(loadTestInsertClientId != null){
				daos.add(DatarouterLoadTestInsertDao.class);
			}
			if(loadTestScanClientId != null){
				daos.add(DatarouterLoadTestScanDao.class);
			}
			return daos;
		}

		@Override
		protected void configure(){
			if(loadTestGetClientId != null){
				bind(DatarouterLoadTestGetDaoParams.class)
						.toInstance(new DatarouterLoadTestGetDaoParams(loadTestGetClientId));
			}
			if(loadTestInsertClientId != null){
				bind(DatarouterLoadTestInsertDaoParams.class)
						.toInstance(new DatarouterLoadTestInsertDaoParams(loadTestInsertClientId));
			}
			if(loadTestScanClientId != null){
				bind(DatarouterLoadTestScanDaoParams.class)
						.toInstance(new DatarouterLoadTestScanDaoParams(loadTestScanClientId));
			}
		}

	}


}
