package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
				return handle;  //others are still using this connection
			}
			Connection connection = this.connectionByHandle.get(handle);
			connection.close();
			this.handleByThread.remove(currentThread.getId());
			this.connectionByHandle.remove(handle);
			return handle;
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	
	/****************************** JdbcConnectionClient methods *************************/

	
	@Override
	public Connection getExistingConnection(){
		ConnectionHandle handle = this.getExistingHandle();
		if(handle==null){ return null; }
		return this.connectionByHandle.get(handle);
	}
	
	@Override
	public Connection acquireConnection(){
		this.reserveConnection();
		return this.getExistingConnection();
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
			connection.commit();
			logger.debug("committed txn on:"+this.getExistingHandle());
			return this.getExistingHandle();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public ConnectionHandle rollbackTxn(){
		try{
			Connection connection = this.getExistingConnection();
			connection.rollback();
			logger.debug("rolled-back txn on:"+this.getExistingHandle());
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
	public ConnectionHandle closeSession(){
		ConnectionHandle handle = this.getExistingHandle();
		if(handle==null){ return handle; }
		Session session = this.sessionByConnectionHandle.get(handle);
		if(session != null){
			try{
				session.flush();
			}catch(Exception e){
				logger.warn("problem closing session.  flush() threw exception.");
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
			try{
				session.disconnect();
			}catch(Exception e){
				logger.warn("problem closing session.  disconnect() threw exception.");
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
	
	
	
}
