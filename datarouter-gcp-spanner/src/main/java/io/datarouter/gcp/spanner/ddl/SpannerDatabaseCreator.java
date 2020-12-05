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
package io.datarouter.gcp.spanner.ddl;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.paging.Page;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.Spanner;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.util.concurrent.FutureTool;

@Singleton
public class SpannerDatabaseCreator{
	private static final Logger logger = LoggerFactory.getLogger(SpannerDatabaseCreator.class);

	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	public Database createDatabaseIfNeeded(DatabaseId databaseId, Spanner spanner){
		Page<Database> page = spanner.getDatabaseAdminClient().listDatabases(
				databaseId.getInstanceId().getInstance(),
				Options.pageSize(1));
		Database database = null;
		while(page != null){
			Database current = Scanner.of(page.getValues()).findFirst().orElse(null);
			if(current == null || current.getId().equals(databaseId)){
				database = current;
				break;
			}
			page = page.getNextPage();
		}
		if(database != null){
			return database;
		}
		if(schemaUpdateOptions.getCreateDatabases(false)){
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Creating the Spanner database "
					+ databaseId.getDatabase()));
			OperationFuture<Database,CreateDatabaseMetadata> op = spanner.getDatabaseAdminClient().createDatabase(
					databaseId.getInstanceId().getInstance(),
					databaseId.getDatabase(),
					Collections.emptyList());
			return FutureTool.get(op);
		}else{
			throw new RuntimeException("Must create database before executing updates for database=" + databaseId
					.getDatabase());
		}
	}

}
