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
package io.datarouter.client.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.Driver;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.ddl.execute.DatabaseCreator;
import io.datarouter.client.mysql.ddl.execute.MysqlSchemaUpdateService;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ConnectionHandle;
import io.datarouter.storage.client.SchemaUpdateResult;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.collection.MapTool;
import io.datarouter.util.timer.PhaseTimer;

@Singleton
public class MysqlClientManager extends BaseClientManager implements MysqlConnectionClientManager, TxnClientManager{
	private static final Logger logger = LoggerFactory.getLogger(MysqlClientManager.class);

	private final Map<ClientId,Map<Long,ConnectionHandle>> handleByThreadByClient = new ConcurrentHashMap<>();
	private final Map<ClientId,Map<ConnectionHandle,Connection>> connectionByHandleByClient = new ConcurrentHashMap<>();
	private final Map<ClientId,AtomicLong> connectionCounterByClient = new ConcurrentHashMap<>();

	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;
	@Inject
	private MysqlConnectionPoolHolder mysqlConnectionPoolHolder;
	@Inject
	private MysqlSchemaUpdateService schemaUpdateService;
	@Inject
	private MysqlClientType clientType;
	@Inject
	private DatabaseCreator databaseCreator;

	@Override
	protected void safeInitClient(ClientId clientId){
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		loadDriver();
		databaseCreator.createDatabaseIfNeeded(clientId);
		timer.add("databaseCreation");
		mysqlConnectionPoolHolder.createConnectionPool(clientId);
		timer.add("pool");
		logger.warn(timer.toString());
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

	private AtomicLong connectionCounter(ClientId clientId){
		return connectionCounterByClient.computeIfAbsent(clientId, $ -> new AtomicLong(-1));
	}

	private Map<ConnectionHandle,Connection> connectionByHandle(ClientId clientId){
		return connectionByHandleByClient.computeIfAbsent(clientId, $ -> new ConcurrentHashMap<>());
	}

	private Map<Long,ConnectionHandle> handleByThread(ClientId clientId){
		return handleByThreadByClient.computeIfAbsent(clientId, $ -> new ConcurrentHashMap<>());
	}

	@Override
	protected Future<Optional<SchemaUpdateResult>> doSchemaUpdate(PhysicalNode<?,?,?> node){
		if(schemaUpdateOptions.getEnabled()){
			return schemaUpdateService.queueNodeForSchemaUpdate(node.getFieldInfo().getClientId(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public void gatherSchemaUpdates(){
		schemaUpdateService.gatherSchemaUpdates(true);
	}

	@Override
	public ConnectionHandle getExistingHandle(ClientId clientId){
		Thread currentThread = Thread.currentThread();
		return handleByThread(clientId).get(currentThread.getId());
	}

	@Override
	public void reserveConnection(ClientId clientId){
		initClient(clientId);
		DatarouterCounters.incClient(clientType, "connection open", clientId.getName(), 1);
		try(var $ = TracerTool.startSpan(TracerThreadLocal.get(), "reserve " + clientId.getName())){
			ConnectionHandle existingHandle = getExistingHandle(clientId);
			if(existingHandle != null){
				// logger.warn("got existing connection:"+existingHandle);
				TracerTool.appendToSpanInfo("connection", "existing");
				DatarouterCounters.incClient(clientType, "connection open existing", clientId.getName(), 1);
				// Assert connection exists for handle
				existingHandle.incrementNumTickets();
				return;
			}
			long requestTimeNs = System.nanoTime();
			Connection newConnection = mysqlConnectionPoolHolder.getConnectionPool(clientId).checkOut();
			logIfSlowReserveConnection(clientId, requestTimeNs);

			long threadId = Thread.currentThread().getId();
			long connNumber = connectionCounter(clientId).incrementAndGet();
			ConnectionHandle handle = new ConnectionHandle(Thread.currentThread(), clientId.getName(), connNumber,
					ConnectionHandle.OUTERMOST_TICKET_NUMBER);
			if(handleByThread(clientId).get(threadId) == null){
				handleByThread(clientId).put(threadId, handle);
			}
			connectionByHandle(clientId).put(handle, newConnection);
			// logger.warn("new connection:"+handle);
			TracerTool.appendToSpanInfo("connection", "new");
			DatarouterCounters.incClient(clientType, "connection open new", clientId.getName(), 1);
		}catch(SQLException e){
			DatarouterCounters.incClient(clientType, "connection open " + e.getClass().getSimpleName(), clientId
					.getName(), 1);
			throw new DataAccessException("Could not reserve connection client=" + clientId.getName(), e);
		}
	}

	private void logIfSlowReserveConnection(ClientId clientId, long requestTimeNs){
		long elapsedUs = (System.nanoTime() - requestTimeNs) / 1000;
		if(elapsedUs > 1000){
			DatarouterCounters.incClient(clientType, "connection open > 1ms", clientId.getName(), 1L);
		}
		if(elapsedUs > 2000){
			DatarouterCounters.incClient(clientType, "connection open > 2ms", clientId.getName(), 1L);
		}
		if(elapsedUs > 5000){
			DatarouterCounters.incClient(clientType, "connection open > 5ms", clientId.getName(), 1L);
			long millis = TimeUnit.MICROSECONDS.toMillis(elapsedUs);
			logger.warn("slow reserveConnection: " + millis + "ms on " + clientId.getName());
		}
		if(elapsedUs > 10000){
			DatarouterCounters.incClient(clientType, "connection open > 10ms", clientId.getName(), 1L);
		}
	}

	@Override
	public void releaseConnection(ClientId clientId){
		try{
			Thread currentThread = Thread.currentThread();
			ConnectionHandle handle = getExistingHandle(clientId);
			if(handle == null){
				return;// the connection probably was never opened successfully
			}

			// decrement counters
			handle.decrementNumTickets();
			if(handle.getNumTickets() > 0){
				// logger.warn("KEEPING CONNECTION OPEN for "+handle+", "+this.getStats());
				return; // others are still using this connection
			}

			// release connection
			connectionByHandle(clientId).get(handle).close();
			// on close, there will be a network round trip if isolation needs to be set back to default
			// on close, there will be another network round trip if autocommit needs to be disabled

			connectionByHandle(clientId).remove(handle);
			handleByThread(clientId).remove(currentThread.getId());
			DatarouterCounters.incClient(clientType, "releaseConnection", clientId.getName(), 1L);
			return;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public Connection getExistingConnection(ClientId clientId){
		ConnectionHandle handle = getExistingHandle(clientId);
		if(handle == null){
			return null;
		}
		return connectionByHandle(clientId).get(handle);
	}

	@Override
	public void beginTxn(ClientId clientId, Isolation isolation, boolean autoCommit){
		try{
			Connection connection = getExistingConnection(clientId);
			// jdbc standard says that autoCommit=true by default on each new connection
			if(!autoCommit){
				connection.setAutoCommit(false);
				logger.debug("setAutoCommit=" + false + " on " + getExistingHandle(clientId));
				if(connection.getTransactionIsolation() != isolation.getJdbcVal().intValue()){
					connection.setTransactionIsolation(isolation.getJdbcVal());
					logger.debug("setTransactionIsolation=" + isolation.toString() + " on " + getExistingHandle(
							clientId));
				}
			}
			DatarouterCounters.incClient(clientType, "beginTxn", clientId.getName(), 1L);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void commitTxn(ClientId clientId){
		try{
			Connection connection = getExistingConnection(clientId);
			if(connection != null){
				if(!connection.getAutoCommit()){
					connection.commit();
					logger.debug("committed txn on:" + getExistingHandle(clientId));
				}
			}else{
				logger.warn("couldn't commit txn because connection was null.  handle=" + getExistingHandle(clientId));
			}
			DatarouterCounters.incClient(clientType, "commitTxn", clientId.getName(), 1L);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void rollbackTxn(ClientId clientId){
		try{
			Connection connection = getExistingConnection(clientId);
			if(connection == null){
				logger.warn("couldn't rollback txn because connection was null clientName={} handle={}",
						clientId.getName(), getExistingHandle(clientId), new Exception());
			}else if(!connection.getAutoCommit()){
				logger.warn("ROLLING BACK TXN " + getExistingHandle(clientId));
				connection.rollback();
			}
			DatarouterCounters.incClient(clientType, "rollbackTxn", clientId.getName(), 1L);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void shutdown(ClientId clientId){
		schemaUpdateService.gatherSchemaUpdates(true);
		mysqlConnectionPoolHolder.getConnectionPool(clientId).shutdown();
	}

	public String getStats(ClientId clientId){
		return "client:" + clientId.getName() + " has " + MapTool.size(handleByThread(clientId)) + " threadHandles"
				+ "," + MapTool.size(connectionByHandle(clientId)) + " connectionHandles";
	}

}
