package com.hotpads.datarouter.client.imp.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.client.type.SessionClient;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.MapTool;

public class JdbcClientImp 
extends BaseClient
implements JdbcConnectionClient, TxnClient, SessionClient, JdbcClient{
	private static Logger logger = LoggerFactory.getLogger(JdbcClientImp.class);
	
	private String name;
	
	public String getName(){
		return name;
	}
	
	@Override
	public ClientType getType(){
		return JdbcClientType.INSTANCE;
	}
	
	@Override
	public String toString(){
		return this.name;
	}
	
	protected JdbcConnectionPool connectionPool;
	
	protected Map<Long,ConnectionHandle> handleByThread = new ConcurrentHashMap<Long,ConnectionHandle>();
	protected Map<ConnectionHandle,Connection> connectionByHandle = new ConcurrentHashMap<ConnectionHandle,Connection>();
	
	protected AtomicLong connectionCounter = new AtomicLong(-1L);
	
	
	/**************************** constructor **********************************/
	
	public JdbcClientImp(String name, JdbcConnectionPool connectionPool){
		this.name = name;
		this.connectionPool = connectionPool;
	}
	
	/******************************** methods **********************************/
	
	/****************************** ConnectionClient methods *************************/
	
	@Override
	public ConnectionHandle getExistingHandle(){
		Thread currentThread = Thread.currentThread();
		return handleByThread.get(currentThread.getId());
	}

	@Override
	public ConnectionHandle reserveConnection(){
		DRCounters.incSuffixClient(getType(), "connection open", getName());
		try {
			ConnectionHandle existingHandle = getExistingHandle();
			if(existingHandle != null){
//				logger.warn("got existing connection:"+existingHandle);
				DRCounters.incSuffixClient(getType(), "connection open existing", getName());
				//Assert connection exists for handle
				existingHandle.incrementNumTickets();
				return existingHandle;
			}
			//jdbc triggers network round trip when getting connection to set autocommit=true
			long requestTimeNs = System.nanoTime();
			Connection newConnection = connectionPool.getDataSource().getConnection();
			logIfSlowReserveConnection(requestTimeNs);
			
			long threadId = Thread.currentThread().getId();
			long connNumber = connectionCounter.incrementAndGet();
			ConnectionHandle handle = new ConnectionHandle(Thread.currentThread(), name, 
					connNumber, ConnectionHandle.OUTERMOST_TICKET_NUMBER);
			if(handleByThread.get(threadId)==null){
				handleByThread.put(threadId, handle);
			}
			connectionByHandle.put(handle, newConnection);
//			logger.warn("new connection:"+handle);
			DRCounters.incSuffixClient(getType(), "connection open new", getName());
			return handle;
		}catch(SQLException e){
			DRCounters.incSuffixClient(getType(), "connection open "+e.getClass().getSimpleName(), getName());
			throw new DataAccessException(e);
		}
	}
	
	protected void logIfSlowReserveConnection(long requestTimeNs){
		long elapsedUs = (System.nanoTime() - requestTimeNs) / 1000;
		if(elapsedUs > 1000){
			DRCounters.incSuffixClient(getType(), "connection open > 1ms", getName());
		}
		if(elapsedUs > 2000){
			DRCounters.incSuffixClient(getType(), "connection open > 2ms", getName());
		}
		if(elapsedUs > 5000){
			DRCounters.incSuffixClient(getType(), "connection open > 5ms", getName());
			logger.warn("slow reserveConnection: "+elapsedUs+"us on "+getName());
		}
		if(elapsedUs > 10000){
			DRCounters.incSuffixClient(getType(), "connection open > 10ms", getName());
		}
	}

	@Override
	public ConnectionHandle releaseConnection(){
		try {
			Thread currentThread = Thread.currentThread();
			ConnectionHandle handle = getExistingHandle();
			if(handle==null){ return null; }//the connection probably was never opened successfully
			
			//decrement counters
			handle.decrementNumTickets();
			if(handle.getNumTickets() > 0){
//				logger.warn("KEEPING CONNECTION OPEN for "+handle+", "+this.getStats());
				return handle;  //others are still using this connection
			}
			
			//cleanup session
			cleanupSession();
			
			//release connection
			Connection connection = connectionByHandle.get(handle);
			//on close, there will be a network round trip if isolation needs to be set back to default
			//on close, there will be another network round trip if autocommit needs to be disabled
			connection.close();
			
			connectionByHandle.remove(handle);
			handleByThread.remove(currentThread.getId());
			return handle;
		} catch (SQLException e) {
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
		if(handle==null){ return null; }
		return connectionByHandle.get(handle);
	}

	
	/****************************** JdbcTxnClient methods *************************/

	@Override
	public ConnectionHandle beginTxn(Isolation isolation, boolean autoCommit){
		try {
			Connection connection = getExistingConnection();
			//jdbc standard says that autoCommit=true by default on each new connection
			if(!autoCommit){
				connection.setAutoCommit(false);
				logger.debug("setAutoCommit="+false+" on "+getExistingHandle());
				if(connection.getTransactionIsolation() != isolation.getJdbcVal().intValue()){
					connection.setTransactionIsolation(isolation.getJdbcVal());
					logger.debug("setTransactionIsolation="+isolation.toString()+" on "+getExistingHandle());
				}
			}
			return getExistingHandle();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle commitTxn(){
		try{
			Connection connection = getExistingConnection();
			if(connection != null){
				if( ! connection.getAutoCommit()){
					connection.commit();
					logger.debug("committed txn on:"+getExistingHandle());
				}
			}else{
				logger.warn("couldn't commit txn because connection was null.  handle="+getExistingHandle());
			}
			return getExistingHandle();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle rollbackTxn(){
		try{
			Connection connection = getExistingConnection();
			if(connection == null){
				logger.warn("couldn't rollback txn because connection was null.  handle="+getExistingHandle());
			}else if( ! connection.getAutoCommit()){
				logger.warn("ROLLING BACK TXN "+getExistingHandle());
				connection.rollback();
			}
			return getExistingHandle();
		} catch (SQLException e) {
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
		connectionPool.shutdown();
		
	}
	
	/************************** private *********************************/
	
	public String getStats(){
		return "client:"+name+" has "
		+MapTool.size(handleByThread)+" threadHandles"
		+","+MapTool.size(connectionByHandle)+" connectionHandles";
	}
	
	
	/**************************** get/set ***************************************/

//	public void setConnectionPool(JdbcConnectionPool connectionPool){
//		this.connectionPool = connectionPool;
//	}
	
	
}
