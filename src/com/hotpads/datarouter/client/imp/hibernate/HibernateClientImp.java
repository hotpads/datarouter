package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.RollbackException;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.MapTool;

public class HibernateClientImp 
extends BaseClient
implements JdbcConnectionClient, TxnClient, HibernateClient{
	protected Logger logger = Logger.getLogger(this.getClass());
	
	String name;
	public String getName(){
		return name;
	}
	
	@Override
	public ClientType getType(){
		return ClientType.hibernate;
	}
	
	@Override
	public String toString(){
		return this.name;
	}
	
	protected JdbcConnectionPool connectionPool;
	protected SessionFactory sessionFactory;
	
	protected Map<Long,ConnectionHandle> handleByThread = 
			Collections.synchronizedMap(new ConcurrentHashMap<Long,ConnectionHandle>());
	//		new ConcurrentHashMap<Long,ConnectionHandle>();
	
	protected Map<ConnectionHandle,Connection> connectionByHandle = 
			Collections.synchronizedMap(new ConcurrentHashMap<ConnectionHandle,Connection>());
	//		new ConcurrentHashMap<ConnectionHandle,Connection>();
	
	protected Map<ConnectionHandle,Session> sessionByConnectionHandle = 
			Collections.synchronizedMap(new ConcurrentHashMap<ConnectionHandle,Session>());
	//		new ConcurrentHashMap<ConnectionHandle,Session>();
	
	protected AtomicLong connectionCounter = new AtomicLong(-1L);
	
	
	/**************************** constructor **********************************/
	
	public HibernateClientImp(String name){
		this.name = name;
	}
	
	/******************************** methods **********************************/
	
	/****************************** ConnectionClient methods *************************/
	
	@Override
	public ConnectionHandle getExistingHandle(){
		Thread currentThread = Thread.currentThread();
		return this.handleByThread.get(currentThread.getId());
	}

	@Override
	public ConnectionHandle reserveConnection(){
		DRCounters.incSuffixClient(ClientType.hibernate, "connection open", getName());
		try {
			ConnectionHandle existingHandle = getExistingHandle();
			if(existingHandle != null){
				logger.debug("got existing connection:"+existingHandle);
				//Assert connection exists for handle
				existingHandle.incrementNumTickets();
				return existingHandle;
			}
			//jdbc triggers network round trip when getting connection to set autocommit=true
			long requestTimeMs = System.currentTimeMillis();
			Connection newConnection = connectionPool.getDataSource().getConnection();
			logIfSlowReserveConnection(requestTimeMs);
			
			long threadId = Thread.currentThread().getId();
			long connNumber = connectionCounter.incrementAndGet();
			ConnectionHandle handle = new ConnectionHandle(Thread.currentThread(), name, 
					connNumber, ConnectionHandle.OUTERMOST_TICKET_NUMBER);
			if(handleByThread.get(threadId)==null){
				handleByThread.put(threadId, handle);
			}
			connectionByHandle.put(handle, newConnection);
			logger.debug("new connection:"+handle);
			return handle;
		}catch(SQLException e){
			DRCounters.incSuffixClient(ClientType.hibernate, "connection open "+e.getClass().getSimpleName(), getName());
			throw new DataAccessException(e);
		}
	}
	
	protected void logIfSlowReserveConnection(long requestTimeMs){
		long elapsedTime = System.currentTimeMillis() - requestTimeMs;
		if(elapsedTime > 1){
			DRCounters.incSuffixClient(ClientType.hibernate, "connection open > 1ms", getName());
			logger.warn("slow reserveConnection:"+elapsedTime+"ms on "+getName());
		}
	}

	@Override
	public ConnectionHandle releaseConnection(){
		try {
			Thread currentThread = Thread.currentThread();
			ConnectionHandle handle = getExistingHandle();
			if(handle==null){ return null; }//the connection probably was never opened successfully
			handle.decrementNumTickets();
			if(handle.getNumTickets() > 0){
//				logger.warn("KEEPING CONNECTION OPEN for "+handle+", "+this.getStats());
				return handle;  //others are still using this connection
			}
			if(sessionByConnectionHandle.containsKey(handle)){
				try{
//					logger.warn("*************************************************************************************************");
//					logger.warn("SESSION STILL EXISTS for "+handle+".  CLEANING UP");
					this.cleanupSession();
//					logger.warn("*************************************************************************************************");
				}catch(Exception e){
					logger.warn("*************************************************************************************************");
					logger.warn("ERROR CLEANING UP SESSION for "+handle+", stats:"+this.getStats());
					logger.warn("*************************************************************************************************");
				}
			}
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
	public ConnectionHandle beginTxn(Isolation isolation, boolean disableAutoCommit){
		try {
			Connection connection = getExistingConnection();
			//jdbc standard says that autoCommit=true by default on each new connection
			if(disableAutoCommit){
				connection.setAutoCommit(false);
				logger.debug("setAutoCommit="+false+" on "+this.getExistingHandle());
				if(connection.getTransactionIsolation() != isolation.getJdbcVal().intValue()){
					connection.setTransactionIsolation(isolation.getJdbcVal());
					logger.debug("setTransactionIsolation="+isolation.toString()+" on "+this.getExistingHandle());
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
		Connection connection = getExistingConnection();
		Session session = sessionFactory.openSession(connection);
		session.setCacheMode(CacheMode.GET);
		session.setFlushMode(FlushMode.MANUAL);
		sessionByConnectionHandle.put(getExistingHandle(), session);
		return this.getExistingHandle();
	}
	
	@Override
	public ConnectionHandle flushSession(){
		ConnectionHandle handle = getExistingHandle();
		if(handle==null){ return handle; }
		Session session = sessionByConnectionHandle.get(handle);
		if(session!=null){
			try{
				session.flush();
			}catch(Exception e){
				logger.warn("problem closing session.  flush() threw exception.  handle="+getExistingHandle());
				logger.warn(getStats());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				try{
					logger.warn("ROLLING BACK TXN after failed flush().  handle="+getExistingHandle());
					rollbackTxn();
				}catch(Exception e2){
					logger.warn("TXN ROLLBACK FAILED after flush() threw exception.  handle="+getExistingHandle());
					logger.warn(name+" has "+MapTool.size(sessionByConnectionHandle)+" sessions");
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				}
				throw new RollbackException(e);
			}
		}
		return handle;
	}
	
	@Override
	public ConnectionHandle cleanupSession(){
		ConnectionHandle handle = getExistingHandle();
		if(handle==null){ return handle; }
		Session session = sessionByConnectionHandle.get(handle);
		if(session != null){
			try{
				session.clear();//otherwise things get left in the session factory??
			}catch(Exception e){
				logger.warn("problem clearing session.  clear() threw exception.  handle="+getExistingHandle());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
			try{
				session.disconnect();
			}catch(Exception e){
				logger.warn("problem closing session.  disconnect() threw exception.  handle="+getExistingHandle());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
			try{
				session.close();//should not be necessary, but best to be safe
			}catch(Exception e){
				logger.warn("problem closing session.  close() threw exception.  handle="+getExistingHandle());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
		}
		sessionByConnectionHandle.remove(handle);
		return handle;
	}

	
	/****************************** HibernateClient methods *************************/
	
	@Override
	public Session getExistingSession(){
		if(getExistingHandle()==null){ return null; }
		return this.sessionByConnectionHandle.get(getExistingHandle());
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	
	/************************** private *********************************/
	
	public String getStats(){
		return "client:"+name+" has "
		+MapTool.size(handleByThread)+" threadHandles,"
		+MapTool.size(connectionByHandle)+" connectionHandles,"
		+MapTool.size(sessionByConnectionHandle)+" sessionHandles";
	}
	
	
	/**************************** get/set ***************************************/

	public void setConnectionPool(JdbcConnectionPool connectionPool){
		this.connectionPool = connectionPool;
	}

	public void setSessionFactory(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	
}
