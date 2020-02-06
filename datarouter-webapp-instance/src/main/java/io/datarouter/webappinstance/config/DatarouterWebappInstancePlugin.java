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
package io.datarouter.webappinstance.config;

import java.util.List;

import io.datarouter.instrumentation.webappinstance.WebappInstancePublisher;
import io.datarouter.instrumentation.webappinstance.WebappInstancePublisher.NoOpWebappInstancePublisher;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.webappinstance.WebappInstanceAppListener;
import io.datarouter.webappinstance.storage.onetimelogintoken.DatarouterOneTimeLoginTokenDao;
import io.datarouter.webappinstance.storage.onetimelogintoken.DatarouterOneTimeLoginTokenDao.DatarouterOneTimeLoginTokenDaoParams;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao.DatarouterWebappInstanceDaoParams;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao.DatarouterWebappInstanceLogDaoParams;

public class DatarouterWebappInstancePlugin extends BaseJobPlugin{

	private final Class<? extends WebappInstancePublisher> webappInstancePublisher;

	private DatarouterWebappInstancePlugin(
			DatarouterWebappInstanceDaoModule daosModuleBuilder,
			Class<? extends WebappInstancePublisher> webappInstancePublisher){
		this.webappInstancePublisher = webappInstancePublisher;
		addUnorderedAppListener(WebappInstanceAppListener.class);
		addUnorderedRouteSet(DatarouterWebappInstanceRouteSet.class);
		addSettingRoot(DatarouterWebappInstanceSettingRoot.class);
		addTriggerGroup(DatarouterWebappInstanceTriggerGroup.class);
		setDaosModuleBuilder(daosModuleBuilder);
		addDatarouterNavBarItem(new NavBarItem(DatarouterNavBarCategory.MONITORING,
				new DatarouterWebappInstancePaths().datarouter.webappInstances, "Webapp Instances"));
	}

	@Override
	public String getName(){
		return "DatarouterWebappInstance";
	}

	@Override
	protected void configure(){
		bind(WebappInstancePublisher.class).to(webappInstancePublisher);
	}

	public static class DatarouterWebappInstancePluginBuilder{

		private final ClientId defaultClientId;
		private DatarouterWebappInstanceDaoModule daoModule;
		private Class<? extends WebappInstancePublisher> webappInstancePublisher = NoOpWebappInstancePublisher.class;

		public DatarouterWebappInstancePluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterWebappInstancePluginBuilder setDaoModule(DatarouterWebappInstanceDaoModule module){
			this.daoModule = module;
			return this;
		}

		public DatarouterWebappInstancePluginBuilder withWebappInstancePublisher(
				Class<? extends WebappInstancePublisher> webappInstancePublisher){
			this.webappInstancePublisher = webappInstancePublisher;
			return this;
		}

		public DatarouterWebappInstancePlugin build(){
			return new DatarouterWebappInstancePlugin(
					daoModule == null
							? new DatarouterWebappInstanceDaoModule(defaultClientId, defaultClientId, defaultClientId)
							: daoModule,
					webappInstancePublisher);
		}

	}

	public static class DatarouterWebappInstanceDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterOneTimeLoginTokenClientId;
		private final ClientId datarouterWebappInstanceClientId;
		private final ClientId datarouterWebappInstanceLogClientId;

		public DatarouterWebappInstanceDaoModule(
				ClientId datarouterOneTimeLoginTokenClientId,
				ClientId datarouterWebappInstanceClientId,
				ClientId datarouterWebappInstanceLogClientId){
			this.datarouterOneTimeLoginTokenClientId = datarouterOneTimeLoginTokenClientId;
			this.datarouterWebappInstanceClientId = datarouterWebappInstanceClientId;
			this.datarouterWebappInstanceLogClientId = datarouterWebappInstanceLogClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterOneTimeLoginTokenDao.class,
					DatarouterWebappInstanceDao.class,
					DatarouterWebappInstanceLogDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterWebappInstanceDaoParams.class)
					.toInstance(new DatarouterWebappInstanceDaoParams(datarouterWebappInstanceClientId));
			bind(DatarouterWebappInstanceLogDaoParams.class)
					.toInstance(new DatarouterWebappInstanceLogDaoParams(datarouterWebappInstanceLogClientId));
			bind(DatarouterOneTimeLoginTokenDaoParams.class)
					.toInstance(new DatarouterOneTimeLoginTokenDaoParams(datarouterOneTimeLoginTokenClientId));
		}

	}

}
