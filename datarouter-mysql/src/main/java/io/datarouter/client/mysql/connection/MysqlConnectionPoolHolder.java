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
package io.datarouter.client.mysql.connection;

import java.beans.PropertyVetoException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.mysql.cj.log.Slf4JLogger;

import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.factory.MysqlOptions;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.string.StringTool;
import net.sf.log4jdbc.DriverSpy;

@Singleton
public class MysqlConnectionPoolHolder{
	private static final Logger logger = LoggerFactory.getLogger(MysqlConnectionPoolHolder.class);

	public static final String UTF8MB4_CHARSET = "utf8mb4";
	private static final String UTF8MB4_COLLATION = UTF8MB4_CHARSET + "_bin";
	private static final String UTF8 = StandardCharsets.UTF_8.name();

	public static final MysqlCharacterSet CHARACTER_SET_CONNECTION = MysqlCharacterSet.valueOf(UTF8MB4_CHARSET);
	public static final MysqlCollation COLLATION_CONNECTION = MysqlCollation.valueOf(UTF8MB4_COLLATION);

	public static final String CLIENT_NAME_KEY = "clientName=";

	static{
		System.setProperty("log4jdbc.dump.sql.maxlinelength", "0");
		System.setProperty("log4jdbc.trim.sql.extrablanklines", "false");
		System.setProperty("log4jdbc.auto.load.popular.drivers", "false");
	}

	@Inject
	private MysqlOptions mysqlOptions;

	private final Map<ClientId,MysqlConnectionPool> connectionPools = new ConcurrentHashMap<>();

	public void createConnectionPool(ClientId clientId){
		MysqlConnectionPool connectionPool = new MysqlConnectionPool(clientId);
		try{
			connectionPool.checkOut().close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
		connectionPools.put(clientId, connectionPool);
	}

	public MysqlConnectionPool getConnectionPool(ClientId clientId){
		return connectionPools.get(clientId);
	}

	public class MysqlConnectionPool{

		private final ComboPooledDataSource pool;
		private final String schemaName;

		private MysqlConnectionPool(ClientId clientId){
			String url = mysqlOptions.url(clientId);
			String user = mysqlOptions.user(clientId.getName(), "root");
			String password = mysqlOptions.password(clientId.getName(), "");
			Integer minPoolSize = mysqlOptions.minPoolSize(clientId.getName(), 3);
			Integer maxPoolSize = mysqlOptions.maxPoolSize(clientId.getName(), 50);
			Integer acquireIncrement = mysqlOptions.acquireIncrement(clientId.getName(), 1);
			Integer numHelperThreads = mysqlOptions.numHelperThreads(clientId.getName(), 10);
			Integer maxIdleTime = mysqlOptions.maxIdleTime(clientId.getName(), 300);
			Integer idleConnectionTestPeriod = mysqlOptions.idleConnectionTestPeriod(clientId.getName(), 30);
			Boolean logging = mysqlOptions.logging(clientId.getName(), false);
			List<String> additionalUrlParams = mysqlOptions.urlParams(clientId.getName());

			this.schemaName = StringTool.getStringAfterLastOccurrence('/', url);

			// configurable props
			this.pool = new ComboPooledDataSource();
			this.pool.setInitialPoolSize(minPoolSize);
			this.pool.setMinPoolSize(minPoolSize);
			this.pool.setMaxPoolSize(maxPoolSize);

			List<String> standardUrlParams = new ArrayList<>();
			// avoid extra RPC on readOnly connections:
			// http://dev.mysql.com/doc/relnotes/connector-j/en/news-5-1-23.html
			standardUrlParams.add("useLocalSessionState=true");
			standardUrlParams.add("zeroDateTimeBehavior=convertToNull");
			standardUrlParams.add("connectionCollation=" + UTF8MB4_COLLATION);
			standardUrlParams.add("characterEncoding=" + UTF8);
			standardUrlParams.add("logger=" + Slf4JLogger.class.getName());
			standardUrlParams.add(CLIENT_NAME_KEY + clientId.getName());

			List<String> allUrlParams = ListTool.concatenate(standardUrlParams, additionalUrlParams);
			String urlWithParams = url + "?" + String.join("&", allUrlParams);
			try{
				String jdbcUrl;
				if(logging){
					// log4jdbc - see http://code.google.com/p/log4jdbc/
					this.pool.setDriverClass(DriverSpy.class.getName());
					jdbcUrl = "jdbc:log4jdbc:mysql://" + urlWithParams;
				}else{
					jdbcUrl = "jdbc:mysql://" + urlWithParams;
				}
				this.pool.setJdbcUrl(jdbcUrl);
			}catch(PropertyVetoException pve){
				throw new RuntimeException(pve);
			}

			this.pool.setUser(user);
			this.pool.setPassword(password);
			this.pool.setAcquireIncrement(acquireIncrement);
			this.pool.setNumHelperThreads(numHelperThreads);
			this.pool.setAcquireRetryAttempts(30);
			this.pool.setAcquireRetryDelay(500);
			this.pool.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
			this.pool.setMaxIdleTime(maxIdleTime); // seconds

			Class<?> connectionCustomizer;
			if(clientId.getWritable()){
				connectionCustomizer = Utf8mb4ConnectionCustomizer.class;
			}else{
				connectionCustomizer = ReadOnlyUtf8mb4ConnectionCustomizer.class;
			}
			this.pool.setConnectionCustomizerClassName(connectionCustomizer.getName());

		}

		public Connection checkOut() throws SQLException{
			try{
				return pool.getConnection();
			}catch(SQLException e){
				logger.warn("could not connect jdbcUrl={} error={}", pool.getJdbcUrl(), e);
				throw e;
			}
		}

		public void shutdown(){
			try{
				DataSources.destroy(pool);
			}catch(SQLException e){
				logger.error("", e);
			}
		}

		public String getSchemaName(){
			return schemaName;
		}

		@Override
		public String toString(){
			return schemaName + "@" + pool.getJdbcUrl();
		}

	}

}