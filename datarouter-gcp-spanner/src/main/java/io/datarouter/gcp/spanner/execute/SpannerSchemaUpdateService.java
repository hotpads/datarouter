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
package io.datarouter.gcp.spanner.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.email.StandardDatarouterEmailHeaderService;
import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.ddl.SpannerSingleTableSchemaUpdateService;
import io.datarouter.gcp.spanner.ddl.SpannerTableOperationsGenerator;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.settings.DatarouterSchemaUpdateEmailSettings;
import io.datarouter.web.handler.EmailingSchemaUpdateService;
import io.datarouter.web.monitoring.BuildProperties;

@Singleton
public class SpannerSchemaUpdateService extends EmailingSchemaUpdateService{

	private final SpannerSingleTableSchemaUpdateService singleTableSchemaUpdateFactory;
	private final SpannerTableOperationsGenerator tableOperationsGenerator;
	private final SpannerDatabaseClientsHolder clientPoolHolder;

	@Inject
	public SpannerSchemaUpdateService(
			DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService adminEmailService,
			SpannerSingleTableSchemaUpdateService singleTableSchemaUpdateFactory,
			SpannerTableOperationsGenerator tableOperationsGenerator,
			DatarouterSchemaUpdateScheduler executor,
			SpannerDatabaseClientsHolder clientPoolHolder,
			DatarouterHtmlEmailService htmlEmailService,
			DatarouterWebPaths datarouterWebPaths,
			Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao,
			Provider<ChangelogRecorder> changelogRecorder,
			BuildProperties buildProperties,
			StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService,
			SchemaUpdatesEmailType schemaUpdatesEmailType,
			DatarouterSchemaUpdateEmailSettings schemaUpdateEmailSettings,
			AdminEmail adminEmail){
		super(datarouterProperties,
				adminEmailService,
				executor,
				schemaUpdateLockDao,
				changelogRecorder,
				buildProperties.getBuildId(),
				htmlEmailService,
				datarouterWebPaths,
				standardDatarouterEmailHeaderService,
				schemaUpdatesEmailType,
				schemaUpdateEmailSettings,
				adminEmail);
		this.singleTableSchemaUpdateFactory = singleTableSchemaUpdateFactory;
		this.tableOperationsGenerator = tableOperationsGenerator;
		this.clientPoolHolder = clientPoolHolder;
	}

	@Override
	protected Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node){
		return () -> singleTableSchemaUpdateFactory.performSchemaUpdate(clientId, existingTableNames, node);
	}

	@Override
	protected List<String> fetchExistingTables(ClientId clientId){
		List<String> existingTableNames = new ArrayList<>();
		DatabaseClient dbClient = clientPoolHolder.getDatabaseClient(clientId);
		ResultSet resultSet = dbClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
				.getListOfTables()));
		while(resultSet.next()){
			existingTableNames.add(resultSet.getString("table_name"));
		}
		return existingTableNames;
	}

}
