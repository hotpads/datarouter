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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolFactory.MysqlConnectionPool;
import io.datarouter.client.mysql.ddl.execute.MysqlSchemaUpdateServiceFactory.MysqlSchemaUpdateService;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.ConnectionHandle;
import io.datarouter.storage.client.imp.BaseClient;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.collection.MapTool;

public class MysqlClient extends BaseClient implements MysqlConnectionClient, TxnClient{
	private static final Logger logger = LoggerFactory.getLogger(MysqlClient.class);

	private final Map<Long,ConnectionHandle> handleByThread = new ConcurrentHashMap<>();
	private final Map<ConnectionHandle,Connection> connectionByHandle = new ConcurrentHashMap<>();
	private final AtomicLong connectionCounter = new AtomicLong(-1L);

	private final SchemaUpdateOptions schemaUpdateOptions;
	private final MysqlConnectionPool connectionPool;
	private final MysqlSchemaUpdateService schemaUpdateService;

	public MysqlClient(String name, MysqlConnectionPool connectionPool, MysqlSchemaUpdateService schemaUpdateService,
			SchemaUpdateOptions schemaUpdateOptions, ClientType<?> clientType){
		super(name, clientType);
		this.connectionPool = connectionPool;
		this.schemaUpdateService = schemaUpdateService;
		this.schemaUpdateOptions = schemaUpdateOptions;
	}

	@Override
	public Future<Optional<String>> notifyNodeRegistration(PhysicalNode<?,?,?> node){
		if(schemaUpdateOptions.getEnabled()){
			return schemaUpdateService.queueNodeForSchemaUpdate(getName(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public ConnectionHandle getExistingHandle(){
		Thread currentThread = Thread.currentThread();
		return handleByThread.get(currentThread.getId());
	}

	@Override
	public void reserveConnection(){
		DatarouterCounters.incClient(getType(), "connection open", getName(), 1L);
		try{
			ConnectionHandle existingHandle = getExistingHandle();
			if(existingHandle != null){
				// logger.warn("got existing connection:"+existingHandle);
				DatarouterCounters.incClient(getType(), "connection open existing", getName(), 1L);
				// Assert connection exists for handle
				existingHandle.incrementNumTickets();
				return;
			}
			long requestTimeNs = System.nanoTime();
			Connection newConnection = connectionPool.checkOut();
			logIfSlowReserveConnection(requestTimeNs);

			long threadId = Thread.currentThread().getId();
			long connNumber = connectionCounter.incrementAndGet();
			ConnectionHandle handle = new ConnectionHandle(Thread.currentThread(), getName(), connNumber,
					ConnectionHandle.OUTERMOST_TICKET_NUMBER);
			if(handleByThread.get(threadId) == null){
				handleByThread.put(threadId, handle);
			}
			connectionByHandle.put(handle, newConnection);
			// logger.warn("new connection:"+handle);
			DatarouterCounters.incClient(getType(), "connection open new", getName(), 1L);
		}catch(SQLException e){
			DatarouterCounters.incClient(getType(), "connection open " + e.getClass().getSimpleName(), getName(), 1L);
			throw new DataAccessException(e);
		}
	}

	private void logIfSlowReserveConnection(long requestTimeNs){
		long elapsedUs = (System.nanoTime() - requestTimeNs) / 1000;
		if(elapsedUs > 1000){
			DatarouterCounters.incClient(getType(), "connection open > 1ms", getName(), 1L);
		}
		if(elapsedUs > 2000){
			DatarouterCounters.incClient(getType(), "connection open > 2ms", getName(), 1L);
		}
		if(elapsedUs > 5000){
			DatarouterCounters.incClient(getType(), "connection open > 5ms", getName(), 1L);
			long millis = TimeUnit.MICROSECONDS.toMillis(elapsedUs);
			logger.warn("slow reserveConnection: " + millis + "ms on " + getName());
		}
		if(elapsedUs > 10000){
			DatarouterCounters.incClient(getType(), "connection open > 10ms", getName(), 1L);
		}
	}

	@Override
	public void releaseConnection(){
		try{
			Thread currentThread = Thread.currentThread();
			ConnectionHandle handle = getExistingHandle();
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
			connectionByHandle.get(handle).close();
			// on close, there will be a network round trip if isolation needs to be set back to default
			// on close, there will be another network round trip if autocommit needs to be disabled

			connectionByHandle.remove(handle);
			handleByThread.remove(currentThread.getId());
			return;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public Connection getExistingConnection(){
		ConnectionHandle handle = getExistingHandle();
		if(handle == null){
			return null;
		}
		return connectionByHandle.get(handle);
	}

	@Override
	public void beginTxn(Isolation isolation, boolean autoCommit){
		try{
			Connection connection = getExistingConnection();
			// jdbc standard says that autoCommit=true by default on each new connection
			if(!autoCommit){
				connection.setAutoCommit(false);
				logger.debug("setAutoCommit=" + false + " on " + getExistingHandle());
				if(connection.getTransactionIsolation() != isolation.getJdbcVal().intValue()){
					connection.setTransactionIsolation(isolation.getJdbcVal());
					logger.debug("setTransactionIsolation=" + isolation.toString() + " on " + getExistingHandle());
				}
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void commitTxn(){
		try{
			Connection connection = getExistingConnection();
			if(connection != null){
				if(!connection.getAutoCommit()){
					connection.commit();
					logger.debug("committed txn on:" + getExistingHandle());
				}
			}else{
				logger.warn("couldn't commit txn because connection was null.  handle=" + getExistingHandle());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void rollbackTxn(){
		try{
			Connection connection = getExistingConnection();
			if(connection == null){
				logger.warn("couldn't rollback txn because connection was null.  handle=" + getExistingHandle());
			}else if(!connection.getAutoCommit()){
				logger.warn("ROLLING BACK TXN " + getExistingHandle());
				connection.rollback();
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void shutdown(){
		schemaUpdateService.gatherSchemaUpdates(true);
		connectionPool.shutdown();
	}

	public String getStats(){
		return "client:" + getName() + " has " + MapTool.size(handleByThread) + " threadHandles" + "," + MapTool
				.size(connectionByHandle) + " connectionHandles";
	}

}
