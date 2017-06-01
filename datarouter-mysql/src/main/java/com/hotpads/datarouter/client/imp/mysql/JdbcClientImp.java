package com.hotpads.datarouter.client.imp.mysql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.mysql.ddl.execute.JdbcSchemaUpdateServiceFactory.JdbcSchemaUpdateService;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

public class JdbcClientImp extends BaseClient implements JdbcConnectionClient, TxnClient, JdbcClient{
	private static final Logger logger = LoggerFactory.getLogger(JdbcClientImp.class);

	private Map<Long,ConnectionHandle> handleByThread = new ConcurrentHashMap<>();
	private Map<ConnectionHandle,Connection> connectionByHandle = new ConcurrentHashMap<>();
	private AtomicLong connectionCounter = new AtomicLong(-1L);

	private final boolean schemaUpdateEnabled;

	private final JdbcConnectionPool connectionPool;
	private final JdbcSchemaUpdateService schemaUpdateService;

	/**************************** constructor **********************************/

	public JdbcClientImp(String name, JdbcConnectionPool connectionPool, JdbcSchemaUpdateService schemaUpdateService,
			ClientAvailabilitySettings clientAvailabilitySettings, boolean schemaUpdateEnabled){
		super(name, clientAvailabilitySettings);
		this.connectionPool = connectionPool;
		this.schemaUpdateService = schemaUpdateService;
		this.schemaUpdateEnabled = schemaUpdateEnabled;
	}

	/******************************** methods **********************************/

	@Override
	public ClientType getType(){
		return JdbcClientType.INSTANCE;
	}

	@Override
	public Future<Optional<String>> notifyNodeRegistration(PhysicalNode<?,?> node){
		if(schemaUpdateEnabled){
			return schemaUpdateService.queueNodeForSchemaUpdate(getName(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	/****************************** ConnectionClient methods *************************/

	@Override
	public ConnectionHandle getExistingHandle(){
		Thread currentThread = Thread.currentThread();
		return handleByThread.get(currentThread.getId());
	}

	@Override
	public ConnectionHandle reserveConnection(){
		DRCounters.incClient(getType(), "connection open", getName(), 1L);
		try{
			ConnectionHandle existingHandle = getExistingHandle();
			if(existingHandle != null){
				// logger.warn("got existing connection:"+existingHandle);
				DRCounters.incClient(getType(), "connection open existing", getName(), 1L);
				// Assert connection exists for handle
				existingHandle.incrementNumTickets();
				return existingHandle;
			}
			// jdbc triggers network round trip when getting connection to set autocommit=true
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
			DRCounters.incClient(getType(), "connection open new", getName(), 1L);
			return handle;
		}catch(SQLException e){
			DRCounters.incClient(getType(), "connection open " + e.getClass().getSimpleName(), getName(), 1L);
			throw new DataAccessException(e);
		}
	}

	private void logIfSlowReserveConnection(long requestTimeNs){
		long elapsedUs = (System.nanoTime() - requestTimeNs) / 1000;
		if(elapsedUs > 1000){
			DRCounters.incClient(getType(), "connection open > 1ms", getName(), 1L);
		}
		if(elapsedUs > 2000){
			DRCounters.incClient(getType(), "connection open > 2ms", getName(), 1L);
		}
		if(elapsedUs > 5000){
			DRCounters.incClient(getType(), "connection open > 5ms", getName(), 1L);
			logger.warn("slow reserveConnection: " + elapsedUs + "us on " + getName());
		}
		if(elapsedUs > 10000){
			DRCounters.incClient(getType(), "connection open > 10ms", getName(), 1L);
		}
	}

	@Override
	public ConnectionHandle releaseConnection(){
		try{
			Thread currentThread = Thread.currentThread();
			ConnectionHandle handle = getExistingHandle();
			if(handle == null){
				return null;// the connection probably was never opened successfully
			}

			// decrement counters
			handle.decrementNumTickets();
			if(handle.getNumTickets() > 0){
				// logger.warn("KEEPING CONNECTION OPEN for "+handle+", "+this.getStats());
				return handle; // others are still using this connection
			}

			// cleanup session
			cleanupSession();

			// release connection
			Connection connection = connectionByHandle.get(handle);
			// on close, there will be a network round trip if isolation needs to be set back to default
			// on close, there will be another network round trip if autocommit needs to be disabled
			connection.close();

			connectionByHandle.remove(handle);
			handleByThread.remove(currentThread.getId());
			return handle;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	/****************************** JdbcConnectionClient methods *************************/

	@Override
	public Connection acquireConnection(){
		reserveConnection();
		return getExistingConnection();
	}

	@Override
	public Connection getExistingConnection(){
		ConnectionHandle handle = getExistingHandle();
		if(handle == null){
			return null;
		}
		return connectionByHandle.get(handle);
	}

	/****************************** JdbcTxnClient methods *************************/

	@Override
	public ConnectionHandle beginTxn(Isolation isolation, boolean autoCommit){
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
			return getExistingHandle();
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle commitTxn(){
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
			return getExistingHandle();
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle rollbackTxn(){
		try{
			Connection connection = getExistingConnection();
			if(connection == null){
				logger.warn("couldn't rollback txn because connection was null.  handle=" + getExistingHandle());
			}else if(!connection.getAutoCommit()){
				logger.warn("ROLLING BACK TXN " + getExistingHandle());
				connection.rollback();
			}
			return getExistingHandle();
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	/****************************** SessionClient methods *************************/

	@Override
	public ConnectionHandle openSession(){
		return null;
	}

	@Override
	public ConnectionHandle flushSession(){
		return null;
	}

	@Override
	public ConnectionHandle cleanupSession(){
		return null;
	}

	/****************************** shutdown *************************/

	@Override
	public void shutdown(){
		schemaUpdateService.gatherSchemaUpdates(true);
		connectionPool.shutdown();
		try{
			AbandonedConnectionCleanupThread.shutdown();
		}catch(InterruptedException e){
			// logger doesn't work here (already shutdown?)
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for(Enumeration<Driver> drivers = DriverManager.getDrivers(); drivers.hasMoreElements();){
			Driver driver = drivers.nextElement();
			if(driver.getClass().getClassLoader() == classLoader){
				// This driver was registered by the webapp's ClassLoader, so deregister it:
				try{
					DriverManager.deregisterDriver(driver);
				}catch(SQLException ex){
					// logger doesn't work here (already shutdown?)
				}
			}
		}
	}

	/************************** private *********************************/

	public String getStats(){
		return "client:" + getName() + " has " + DrMapTool.size(handleByThread) + " threadHandles" + "," + DrMapTool
				.size(connectionByHandle) + " connectionHandles";
	}

}
