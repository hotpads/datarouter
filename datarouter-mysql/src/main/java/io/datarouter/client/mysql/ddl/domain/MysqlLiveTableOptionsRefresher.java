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
package io.datarouter.client.mysql.ddl.domain;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder.MysqlConnectionPool;
import io.datarouter.client.mysql.ddl.generate.imp.ConnectionSqlTableGenerator;
import io.datarouter.client.mysql.ddl.generate.imp.SqlTableMetadata;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.timer.PhaseTimer;

// This assumes that all columns have the same charset/collation
@Singleton
public class MysqlLiveTableOptionsRefresher{
	private static final Logger logger = LoggerFactory.getLogger(MysqlLiveTableOptionsRefresher.class);

	@Inject
	private MysqlConnectionPoolHolder mysqlConnectionPoolHolder;

	private final Map<ClientAndTable,MysqlLiveTableOptions> map = new ConcurrentHashMap<>();

	private final AtomicLong counter = new AtomicLong();

	private MysqlLiveTableOptions read(ClientAndTable clientAndTable){
		PhaseTimer phaseTimer = new PhaseTimer();
		MysqlConnectionPool connectionPool = mysqlConnectionPoolHolder.getConnectionPool(clientAndTable.clientId);
		phaseTimer.add("getPool");
		String schemaName = connectionPool.getSchemaName();
		phaseTimer.add("getSchema");
		SqlTableMetadata sqlTableMetadata;
		// TODO create a new connection or use a dedicated pool
		try(Connection connection = connectionPool.checkOut()){
			phaseTimer.add("checkOut");
			sqlTableMetadata = ConnectionSqlTableGenerator.fetchSqlTableMetadata(connection, schemaName,
					clientAndTable.tableName);
		}catch(SQLException e){
			throw new RuntimeException("schema=" + schemaName + " table=" + clientAndTable.tableName, e);
		}
		phaseTimer.add("fetchSqlTableMetadata");
		logger.debug("{} {}", clientAndTable, phaseTimer);
		counter.addAndGet(phaseTimer.getElapsedTimeBetweenFirstAndLastEvent());
		return new MysqlLiveTableOptions(sqlTableMetadata.characterSet, sqlTableMetadata.collation);
	}

	public MysqlLiveTableOptions get(ClientId clientId, String tableName){
		return map.computeIfAbsent(new ClientAndTable(clientId, tableName), this::read);
	}

	public void refresh(){
		long blockingTime = counter.getAndSet(0);
		for(Entry<ClientAndTable,MysqlLiveTableOptions> entry : map.entrySet()){
			MysqlLiveTableOptions newOptions = read(entry.getKey());
			if(!newOptions.equals(entry.getValue())){
				logger.warn("collation change detected {} => {}", entry.getValue().toString("old"), newOptions
						.toString("new"));
				entry.setValue(newOptions);
			}
		}
		long refreshTime = counter.getAndSet(0);
		logger.info("blockingTime={}ms refreshTime={}ms tableCount={}", blockingTime, refreshTime, map.size());
	}

	static class ClientAndTable{

		ClientId clientId;
		String tableName;

		private ClientAndTable(ClientId clientId, String tableName){
			this.clientId = clientId;
			this.tableName = tableName;
		}

		@Override
		public int hashCode(){
			return Objects.hash(clientId, tableName);
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj){
				return true;
			}
			if(obj == null){
				return false;
			}
			if(getClass() != obj.getClass()){
				return false;
			}
			ClientAndTable other = (ClientAndTable)obj;
			return Objects.equals(other.clientId, clientId) && Objects.equals(other.tableName, tableName);
		}

		@Override
		public String toString(){
			return "clientId=" + clientId.getName() + " tableName=" + tableName;
		}

	}

}
