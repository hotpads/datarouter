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
package io.datarouter.aws.rds.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.aws.rds.service.AuroraClientIdProvider;
import io.datarouter.aws.rds.service.AuroraClientIdProvider.AuroraClientDto;
import io.datarouter.aws.rds.service.AuroraClientIdProvider.GenericAuroraClientIdProvider;
import io.datarouter.aws.rds.service.AwsTags;
import io.datarouter.aws.rds.service.AwsTags.NoOpAwsTags;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration.NoOpDatabaseAdministrationConfiguration;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterAwsRdsPlugin extends BaseWebPlugin{

	private final Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration;
	private final Class<? extends AwsTags> awsTags;
	private final List<AuroraClientDto> auroraClientDtos;
	private final Class<? extends DatarouterSettingOverrides> settingOverridesClass;

	private DatarouterAwsRdsPlugin(
			Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration,
			Class<? extends AwsTags> awsTags,
			List<AuroraClientDto> auroraClientDtos,
			Class<? extends DatarouterSettingOverrides> settingOverridesClass){
		this.databaseAdministrationConfiguration = databaseAdministrationConfiguration;
		this.awsTags = awsTags;
		this.auroraClientDtos = auroraClientDtos;

		this.settingOverridesClass = settingOverridesClass;
		addRouteSet(DatarouterAwsRdsRouteSet.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterAwsRdsTriggerGroup.class);
		addSettingRoot(DatarouterAwsRdsSettingRoot.class);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.DATA,
				new DatarouterAwsPaths().datarouter.auroraInstances.inspectClientUrl,
				"Aurora Clients");
		addDatarouterGithubDocLink("datarouter-aws-rds");
	}

	@Override
	public void configure(){
		bind(DatabaseAdministrationConfiguration.class).to(databaseAdministrationConfiguration);
		bind(AwsTags.class).to(awsTags);
		bind(AuroraClientIdProvider.class).toInstance(new GenericAuroraClientIdProvider(auroraClientDtos));
		bind(settingOverridesClass).asEagerSingleton(); // allow overriders in tests;
	}

	public static class DatarouterAwsRdsPluginBuilder{

		private final Class<? extends DatarouterSettingOverrides> settingOverridesClass;

		private Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration
				= NoOpDatabaseAdministrationConfiguration.class;
		private Class<? extends AwsTags> awsTags = NoOpAwsTags.class;
		private List<AuroraClientDto> auroraClientDtos = new ArrayList<>();

		public DatarouterAwsRdsPluginBuilder(Class<? extends DatarouterSettingOverrides> settingOverridesClass){
			this.settingOverridesClass = settingOverridesClass;
		}

		public DatarouterAwsRdsPluginBuilder withDatabaseAdministratorConfigurationClass(
				Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration){
			this.databaseAdministrationConfiguration = databaseAdministrationConfiguration;
			return this;
		}

		public DatarouterAwsRdsPluginBuilder withAwsTagClass(Class<? extends AwsTags> awsTags){
			this.awsTags = awsTags;
			return this;
		}

		public DatarouterAwsRdsPluginBuilder addAuroraClientDto(AuroraClientDto auroraClientDto){
			this.auroraClientDtos.add(auroraClientDto);
			return this;
		}

		public DatarouterAwsRdsPlugin build(){
			return new DatarouterAwsRdsPlugin(
					databaseAdministrationConfiguration,
					awsTags,
					auroraClientDtos,
					settingOverridesClass);
		}

	}

}
