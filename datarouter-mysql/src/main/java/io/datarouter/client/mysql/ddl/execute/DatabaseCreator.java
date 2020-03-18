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
package io.datarouter.client.mysql.ddl.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.factory.MysqlOptions;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.util.string.StringTool;

@Singleton
public class DatabaseCreator{
	private static final Logger logger = LoggerFactory.getLogger(DatabaseCreator.class);

	@Inject
	private MysqlOptions mysqlOptions;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	public void createDatabaseIfNeeded(ClientId clientId){
		if(!clientId.getWritable()
				|| !schemaUpdateOptions.getEnabled()
				|| !schemaUpdateOptions.getCreateDatabases(true) && !schemaUpdateOptions.getCreateDatabases(false)){
			return;
		}
		String url = mysqlOptions.url(clientId);
		String user = mysqlOptions.user(clientId.getName(), "root");
		String password = mysqlOptions.password(clientId.getName(), "");
		String hostname = StringTool.getStringBeforeLastOccurrence(':', url);
		String portDatabaseString = StringTool.getStringAfterLastOccurrence(':', url);
		int port = Integer.parseInt(StringTool.getStringBeforeLastOccurrence('/', portDatabaseString));
		String databaseName = StringTool.getStringAfterLastOccurrence('/', portDatabaseString);

		try(Connection connection = MysqlTool.openConnection(hostname, port, user, password)){
			List<String> existingDatabases = MysqlTool.showDatabases(connection);
			if(!existingDatabases.contains(databaseName)){
				generateCreateDatabaseSchema(connection, databaseName);
			}
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

	private void generateCreateDatabaseSchema(Connection connection, String databaseName) throws SQLException{
		logger.info("======================== Creating the database " + databaseName + " ============================");
		String sql = "create database " + databaseName + " ;";
		if(schemaUpdateOptions.getCreateDatabases(false)){
			logger.info("Executing " + sql);
			Statement statement = connection.createStatement();
			statement.execute(sql);
		}else{
			logger.info("Please execute: " + sql);
			// TODO email the admin ?
		}
	}

}
