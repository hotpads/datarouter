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

import java.util.List;

import io.datarouter.loadtest.service.LoadTestGetDao;
import io.datarouter.loadtest.service.LoadTestGetDao.NoOpLoadTestGetDao;
import io.datarouter.loadtest.service.LoadTestInsertDao;
import io.datarouter.loadtest.service.LoadTestInsertDao.NoOpLoadTestInsertDao;
import io.datarouter.loadtest.service.LoadTestScanDao;
import io.datarouter.loadtest.service.LoadTestScanDao.NoOpLoadTestScanDao;
import io.datarouter.loadtest.storage.DatarouterLoadTestDao;
import io.datarouter.loadtest.storage.DatarouterLoadTestDao.LoadTestDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterLoadTestPlugin extends BaseWebPlugin{

	private static final DatarouterLoadTestPaths PATHS = new DatarouterLoadTestPaths();

	private final boolean enableInsert;
	private final boolean enableGet;
	private final boolean enableScan;

	private DatarouterLoadTestPlugin(
			DatarouterLoadTestDaosModule daosModule,
			boolean enableInsert,
			boolean enableGet,
			boolean enableScan){
		this.enableInsert = enableInsert;
		this.enableGet = enableGet;
		this.enableScan = enableScan;

		addRouteSet(DatarouterLoadTestRouteSet.class);
		if(enableInsert){
			addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.loadTest.insert,
					"LoadTest - Insert");
		}
		if(enableGet){
			addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.loadTest.get, "LoadTest - Get");
		}
		if(enableScan){
			addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.loadTest.scan, "LoadTest - Scan");
		}
		setDaosModule(daosModule);
		addDatarouterGithubDocLink("datarouter-load-test");
	}

	@Override
	public String getName(){
		return "DatarouterLoadTest";
	}

	@Override
	protected void configure(){
		if(enableGet){
			bind(LoadTestGetDao.class).to(DatarouterLoadTestDao.class);
		}else{
			bind(LoadTestGetDao.class).to(NoOpLoadTestGetDao.class);
		}
		if(enableInsert){
			bind(LoadTestInsertDao.class).to(DatarouterLoadTestDao.class);
		}else{
			bind(LoadTestInsertDao.class).to(NoOpLoadTestInsertDao.class);
		}
		if(enableScan){
			bind(LoadTestScanDao.class).to(DatarouterLoadTestDao.class);
		}else{
			bind(LoadTestScanDao.class).to(NoOpLoadTestScanDao.class);
		}
	}

	public static class DatarouterLoadTestPluginBuilder{

		private final ClientId defaultClientId;
		private boolean enableInsert = false;
		private boolean enableGet = false;
		private boolean enableScan = false;

		public DatarouterLoadTestPluginBuilder(ClientId clientId){
			this.defaultClientId = clientId;
		}

		public DatarouterLoadTestPluginBuilder enableInsert(){
			this.enableInsert = true;
			return this;
		}

		public DatarouterLoadTestPluginBuilder enableGet(){
			this.enableGet = true;
			return this;
		}

		public DatarouterLoadTestPluginBuilder enableScan(){
			this.enableScan = true;
			return this;
		}

		public DatarouterLoadTestPlugin build(){
			var daosModule = new DatarouterLoadTestDaosModule(defaultClientId);
			return new DatarouterLoadTestPlugin(daosModule, enableInsert, enableGet, enableScan);
		}

	}

	private static class DatarouterLoadTestDaosModule extends DaosModuleBuilder{

		private final ClientId defaultClientId;

		private DatarouterLoadTestDaosModule(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(DatarouterLoadTestDao.class);
		}

		@Override
		protected void configure(){
			bind(LoadTestDaoParams.class).toInstance(new LoadTestDaoParams(defaultClientId));
		}

	}

}
