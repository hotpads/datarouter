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
package io.datarouter.client.mysql;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import io.datarouter.client.mysql.ddl.generate.DatabaseHostnameUrlSupplier;
import io.datarouter.client.mysql.ddl.generate.DatabaseHostnameUrlSupplier.NoOpDatabaseHostnameUrlSupplier;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.field.codec.factory.StandardMysqlFieldCodecFactory;
import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.secret.config.DatarouterSecretPlugin.DatarouterSecretPluginBuilder.DatarouterSecretPluginBuilderImpl;
import io.datarouter.storage.config.properties.DatarouterTestPropertiesFile;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsBuilder;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsFactory;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao.DatarouterClusterSchemaUpdateLockDaoParams;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;

public class DatarouterMysqlTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterMysqlTestNgModuleFactory(){
		super(List.of(
				new DatarouterWebGuiceModule(),
				new DatarouterSecretPluginBuilderImpl().build(),
				new DatarouterMysqlTestGuiceModule()));
	}


	public static class DatarouterMysqlTestGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bindActualInstance(MysqlFieldCodecFactory.class,
					new StandardMysqlFieldCodecFactory(Collections.emptyMap()));
			bindActualInstance(DatarouterTestPropertiesFile.class,
					new DatarouterTestPropertiesFile("mysql.properties"));
			bindActual(SchemaUpdateOptionsFactory.class, DatarouterMysqlSchemaUpdateOptionsFactory.class);
			bind(DatarouterClusterSchemaUpdateLockDaoParams.class)
					.toInstance(new DatarouterClusterSchemaUpdateLockDaoParams(
							List.of(DatarouterMysqlTestClientids.MYSQL)));
			bind(SchemaUpdatesEmailType.class).toInstance(new SchemaUpdatesEmailType(List.of()));
			bind(DatabaseHostnameUrlSupplier.class).to(NoOpDatabaseHostnameUrlSupplier.class);
		}

	}

	public static class DatarouterMysqlSchemaUpdateOptionsFactory implements SchemaUpdateOptionsFactory{

		@Override
		public Properties getInternalConfigDirectoryTypeSchemaUpdateOptions(String internalConfigDirectoryTypeName){
			return new SchemaUpdateOptionsBuilder(true)
					.enableAllSchemaUpdateExecuteOptions()
					.build();
		}

	}

}
