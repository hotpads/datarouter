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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.factory.MysqlClientFactory;
import io.datarouter.client.mysql.factory.MysqlOptionsFactory;
import io.datarouter.client.mysql.factory.MysqlOptionsFactory.MysqlOptions;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.string.StringTool;
import net.sf.log4jdbc.DriverSpy;

@Singleton
public class MysqlConnectionPoolFactory{
	private static final Logger logger = LoggerFactory.getLogger(MysqlConnectionPoolFactory.class);

	public static final String UTF8MB4_CHARSET = "utf8mb4";
	private static final String UTF8MB4_COLLATION = UTF8MB4_CHARSET + "_bin";
	private static final String UTF8 = StandardCharsets.UTF_8.name();

	public static final MysqlCharacterSet CHARACTER_SET_CONNECTION = MysqlCharacterSet.valueOf(UTF8MB4_CHARSET);
	public static final MysqlCollation COLLATION_CONNECTION = MysqlCollation.valueOf(UTF8MB4_COLLATION);

	@Inject
	private MysqlOptionsFactory mysqlOptionsFactory;

	public class MysqlConnectionPool{

		private final ComboPooledDataSource pool;
		private final ClientId clientId;
		private final String schemaName;

		public MysqlConnectionPool(ClientId clientId){
			this.clientId = clientId;

			MysqlOptions clientOptions = mysqlOptionsFactory.new MysqlOptions(clientId.getName());
			MysqlOptions defaultOptions = mysqlOptionsFactory.new MysqlOptions(MysqlClientFactory.POOL_DEFAULT);
			String url = clientOptions.url();
			String user = clientOptions.user(defaultOptions.user("root"));
			String password = clientOptions.password(defaultOptions.password(""));
			Integer minPoolSize = clientOptions.minPoolSize(defaultOptions.minPoolSize(3));
			Integer maxPoolSize = clientOptions.maxPoolSize(defaultOptions.maxPoolSize(50));
			Integer acquireIncrement = clientOptions.acquireIncrement(defaultOptions.acquireIncrement(1));
			Integer numHelperThreads = clientOptions.numHelperThreads(defaultOptions.numHelperThreads(10));
			Integer maxIdleTime = clientOptions.maxIdleTime(defaultOptions.maxIdleTime(300));
			Integer idleConnectionTestPeriod = clientOptions.idleConnectionTestPeriod(defaultOptions
					.idleConnectionTestPeriod(30));
			Boolean logging = clientOptions.logging(defaultOptions.logging(false));
			logging = logging || LoggerFactory.getLogger(DriverSpy.class).isDebugEnabled();

			this.schemaName = StringTool.getStringAfterLastOccurrence('/', url);

			// configurable props
			this.pool = new ComboPooledDataSource();
			this.pool.setInitialPoolSize(minPoolSize);
			this.pool.setMinPoolSize(minPoolSize);
			this.pool.setMaxPoolSize(maxPoolSize);

			List<String> urlParams = new ArrayList<>();
			// avoid extra RPC on readOnly connections:
			// http://dev.mysql.com/doc/relnotes/connector-j/en/news-5-1-23.html
			urlParams.add("useLocalSessionState=true");
			urlParams.add("zeroDateTimeBehavior=convertToNull");
			urlParams.add("connectionCollation=" + UTF8MB4_COLLATION);
			urlParams.add("characterEncoding=" + UTF8);

			String urlWithParams = url + "?" + Joiner.on("&").join(urlParams);
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
			return pool.getConnection();
		}

		public void shutdown(){
			try{
				DataSources.destroy(pool);
			}catch(SQLException e){
				logger.error("", e);
			}
		}

		public boolean isWritable(){
			return clientId.getWritable();
		}

		public String getSchemaName(){
			return schemaName;
		}

		@Override
		public String toString(){
			return clientId.getName() + "@" + pool.getJdbcUrl();
		}

	}

}
