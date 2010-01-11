package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.RollbackException;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.MapTool;

public class HibernateClientImp 
implements JdbcConnectionClient, TxnClient, HibernateClient{

	protected Logger logger = Logger.getLogger(this.getClass());

	String name;
	public String getName(){
		return name;
	}
	
	@Override
	public String toString(){
		return this.name;
	}
	
	protected JdbcConnectionPool connectionPool;
	protected SessionFactory sessionFactory;
	
	protected Map<Long,ConnectionHandle> handleByThread = 
		Collections.synchronizedMap(new HashMap<Long,ConnectionHandle>());
	
	protected Map<ConnectionHandle,Connection> connectionByHandle = 
		Collections.synchronizedMap(new HashMap<ConnectionHandle,Connection>());
	
	protected Map<ConnectionHandle,Session> sessionByConnectionHandle = 
		Collections.synchronizedMap(new HashMap<ConnectionHandle,Session>());
	
	long connectionCounter = -1;
	
	
	/**************************** constructor **********************************/
	
	HibernateClientImp(String name){
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
		try {
			ConnectionHandle existingHandle = this.getExistingHandle();
			if(existingHandle != null){
				logger.debug("got existing connection:"+existingHandle);
				//Assert connection exists for handle
				existingHandle.incrementNumTickets();
				return existingHandle;
			}
			Connection newConnection = this.connectionPool.getDataSource().getConnection();
			long threadId = Thread.currentThread().getId();
			long connNumber = ++this.connectionCounter;
			ConnectionHandle handle = new ConnectionHandle(Thread.currentThread(), this.name, connNumber, 1);
			if(this.handleByThread.get(threadId)==null){
				this.handleByThread.put(threadId, handle);
			}
			this.connectionByHandle.put(handle, newConnection);
			logger.debug("new connection:"+handle);
			return handle;
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle releaseConnection(){
		try {
			Thread currentThread = Thread.currentThread();
			ConnectionHandle handle = this.getExistingHandle();
			handle.decrementNumTickets();
			if(handle.getNumTickets() > 0){
				logger.warn("KEEPING CONNECTION OPEN for "+handle+", "+this.getStats());
				return handle;  //others are still using this connection
			}
			if(this.sessionByConnectionHandle.containsKey(handle)){
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
			Connection connection = this.connectionByHandle.get(handle);
			connection.close();
			this.connectionByHandle.remove(handle);
			this.handleByThread.remove(currentThread.getId());
			return handle;
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	
	/****************************** JdbcConnectionClient methods *************************/

	
	@Override
	public Connection acquireConnection(){
		this.reserveConnection();
		return this.getExistingConnection();
	}
	
	@Override
	public Connection getExistingConnection(){
		ConnectionHandle handle = this.getExistingHandle();
		if(handle==null){ return null; }
		return this.connectionByHandle.get(handle);
	}

	
	/****************************** JdbcTxnClient methods *************************/

	@Override
	public ConnectionHandle beginTxn(Isolation isolation){
		try {
			Connection connection = this.getExistingConnection();
			connection.setTransactionIsolation(isolation.getJdbcVal());
			logger.debug("setTransactionIsolation="+isolation.getJdbcVal()+" on "+this.getExistingHandle());
			connection.setAutoCommit(false);
			logger.debug("setAutoCommit=false on "+this.getExistingHandle());
			logger.debug("began txn on:"+this.getExistingHandle());
			return this.getExistingHandle();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle commitTxn(){
		try{
			Connection connection = this.getExistingConnection();
			if(connection != null){
				connection.commit();
				logger.debug("committed txn on:"+this.getExistingHandle());
			}else{
				logger.warn("couldn't commit txn because connection was null.  handle="+this.getExistingHandle());
			}
			return this.getExistingHandle();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle rollbackTxn(){
		try{
			Connection connection = this.getExistingConnection();
			if(connection != null){
				logger.warn("ROLLING BACK TXN "+this.getExistingHandle());
				connection.rollback();
			}else{
				logger.warn("couldn't rollback txn because connection was null.  handle="+this.getExistingHandle());
			}
			return this.getExistingHandle();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	
	/****************************** SessionClient methods *************************/
	
	@Override
	public ConnectionHandle openSession(){
		Connection connection = this.getExistingConnection();
		Session session = this.sessionFactory.openSession(connection);
		session.setCacheMode(CacheMode.GET);
		session.setFlushMode(FlushMode.MANUAL);
		this.sessionByConnectionHandle.put(this.getExistingHandle(), session);
		return this.getExistingHandle();
	}
	
	@Override
	public ConnectionHandle flushSession(){
		ConnectionHandle handle = this.getExistingHandle();
		if(handle==null){ return handle; }
		Session session = this.sessionByConnectionHandle.get(handle);
		if(session!=null){
			try{
				session.flush();
			}catch(Exception e){
				logger.warn("problem closing session.  flush() threw exception.  handle="+this.getExistingHandle());
				logger.warn(this.getStats());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				try{
					logger.warn("ROLLING BACK TXN after failed flush().  handle="+this.getExistingHandle());
					this.rollbackTxn();
				}catch(Exception e2){
					logger.warn("TXN ROLLBACK FAILED after flush() threw exception.  handle="+this.getExistingHandle());
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
		ConnectionHandle handle = this.getExistingHandle();
		if(handle==null){ return handle; }
		Session session = this.sessionByConnectionHandle.get(handle);
		if(session != null){
			try{
				session.clear();//otherwise things get left in the session factory??
			}catch(Exception e){
				logger.warn("problem clearing session.  clear() threw exception.  handle="+this.getExistingHandle());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
			try{
				session.disconnect();
			}catch(Exception e){
				logger.warn("problem closing session.  disconnect() threw exception.  handle="+this.getExistingHandle());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
			try{
				session.close();//should not be necessary, but best to be safe
			}catch(Exception e){
				logger.warn("problem closing session.  close() threw exception.  handle="+this.getExistingHandle());
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
		}
		this.sessionByConnectionHandle.remove(handle);
		return handle;
	}

	
	/****************************** HibernateClient methods *************************/
	
	@Override
	public Session getExistingSession(){
		return this.sessionByConnectionHandle.get(this.getExistingHandle());
	}

	@Override
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	
	/************************** private *********************************/
	
	public String getStats(){
		return "client:"+name+" has "
		+MapTool.size(handleByThread)+" threadHandles,"
		+MapTool.size(connectionByHandle)+" connectionHandles,"
		+MapTool.size(sessionByConnectionHandle)+" sessionHandles";
	}
	
	
	
}
