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
package io.datarouter.client.mysql.factory;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.Driver;

import io.datarouter.client.mysql.MysqlClient;
import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.connection.MysqlConnectionPoolFactory;
import io.datarouter.client.mysql.connection.MysqlConnectionPoolFactory.MysqlConnectionPool;
import io.datarouter.client.mysql.ddl.execute.DatabaseCreator;
import io.datarouter.client.mysql.ddl.execute.MysqlSchemaUpdateServiceFactory;
import io.datarouter.client.mysql.ddl.execute.MysqlSchemaUpdateServiceFactory.MysqlSchemaUpdateService;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.ClientFactory;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.util.timer.PhaseTimer;

@Singleton
public class MysqlClientFactory implements ClientFactory{
	private static final Logger logger = LoggerFactory.getLogger(MysqlClientFactory.class);

	public static final String POOL_DEFAULT = "default";

	private final MysqlSchemaUpdateServiceFactory mysqlSchemaUpdateServiceFactory;
	private final SchemaUpdateOptions schemaUpdateOptions;
	private final MysqlClientType clientType;
	private final DatabaseCreator databaseCreator;
	private final MysqlConnectionPoolFactory mysqlConnectionPoolFactory;

	@Inject
	public MysqlClientFactory(MysqlSchemaUpdateServiceFactory mysqlSchemaUpdateServiceFactory,
			SchemaUpdateOptions schemaUpdateOptions, MysqlClientType clientType, DatabaseCreator databaseCreator,
			MysqlConnectionPoolFactory mysqlConnectionPoolFactory){
		this.mysqlSchemaUpdateServiceFactory = mysqlSchemaUpdateServiceFactory;
		this.schemaUpdateOptions = schemaUpdateOptions;
		this.clientType = clientType;
		this.databaseCreator = databaseCreator;
		this.mysqlConnectionPoolFactory = mysqlConnectionPoolFactory;
	}

	@Override
	public Client createClient(ClientId clientId){
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		loadDriver();
		databaseCreator.createDatabaseIfNeeded(clientId);
		timer.add("databaseCreation");
		MysqlConnectionPool connectionPool = mysqlConnectionPoolFactory.new MysqlConnectionPool(clientId);
		timer.add("pool");
		MysqlSchemaUpdateService schemaUpdateService = mysqlSchemaUpdateServiceFactory.create(connectionPool);
		MysqlClient client = new MysqlClient(clientId.getName(), connectionPool, schemaUpdateService,
				schemaUpdateOptions, clientType);
		timer.add("client");
		logger.warn(timer.toString());
		return client;
	}

	/**
	 * We need to reload the drivers when using Tomcat because it tries to register them too early.
	 * http://tomcat.apache.org/tomcat-9.0-doc/jndi-datasource-examples-howto.html#DriverManager,_the_service_provider_mechanism_and_memory_leaks
	 * Loading the class is enough to register the driver, so this method is creating an instance to load the class.
	 */
	private void loadDriver(){
		try{
			new Driver();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

}
