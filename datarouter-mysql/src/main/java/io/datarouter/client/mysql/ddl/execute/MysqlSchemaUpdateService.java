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
package io.datarouter.client.mysql.ddl.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.settings.DatarouterSchemaUpdateEmailSettings;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.EmailingSchemaUpdateService;
import io.datarouter.web.monitoring.BuildProperties;

@Singleton
public class MysqlSchemaUpdateService extends EmailingSchemaUpdateService{

	private final MysqlSingleTableSchemaUpdateService mysqlSingleTableSchemaUpdateService;
	private final MysqlConnectionPoolHolder mysqlConnectionPoolHolder;

	@Inject
	public MysqlSchemaUpdateService(
			ServerName serverName,
			EnvironmentName environmentName,
			AdminEmail adminEmail,
			MysqlSingleTableSchemaUpdateService mysqlSingleTableSchemaUpdateService,
			DatarouterSchemaUpdateScheduler executor,
			DatarouterHtmlEmailService htmlEmailService,
			MysqlConnectionPoolHolder mysqlConnectionPoolHolder,
			DatarouterWebPaths datarouterWebPaths,
			Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao,
			Provider<ChangelogRecorder> changelogRecorder,
			BuildProperties buildProperties,
			StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService,
			SchemaUpdatesEmailType schemaUpdatesEmailType,
			DatarouterSchemaUpdateEmailSettings schemaUpdateEmailSettings){
		super(
				serverName,
				environmentName,
				adminEmail,
				executor,
				schemaUpdateLockDao,
				changelogRecorder,
				buildProperties.getBuildId(),
				htmlEmailService,
				datarouterWebPaths,
				standardDatarouterEmailHeaderService,
				schemaUpdatesEmailType,
				schemaUpdateEmailSettings);
		this.mysqlSingleTableSchemaUpdateService = mysqlSingleTableSchemaUpdateService;
		this.mysqlConnectionPoolHolder = mysqlConnectionPoolHolder;
	}

	@Override
	protected Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node){
		return () -> mysqlSingleTableSchemaUpdateService.performSchemaUpdate(clientId, existingTableNames, node);
	}

	@Override
	protected List<String> fetchExistingTables(ClientId clientId){
		try(Connection connection = mysqlConnectionPoolHolder.getConnectionPool(clientId).checkOut()){
			return MysqlTool.showTables(connection, mysqlConnectionPoolHolder.getConnectionPool(clientId)
					.getSchemaName());
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

}
