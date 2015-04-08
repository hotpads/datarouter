package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.RollbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.type.SessionClient;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.util.core.DrExceptionTool;
import com.hotpads.datarouter.util.core.DrMapTool;

public class HibernateClientImp 
extends JdbcClientImp
implements SessionClient, HibernateClient{
	private static Logger logger = LoggerFactory.getLogger(HibernateClientImp.class);

	private SessionFactory sessionFactory;

	private Map<ConnectionHandle,Session> sessionByConnectionHandle = new ConcurrentHashMap<ConnectionHandle,Session>();
	
	@Override
	public ClientType getType(){
		return HibernateClientType.INSTANCE;
	}
	
	
	/**************************** constructor **********************************/
	
	public HibernateClientImp(String name, JdbcConnectionPool connectionPool, SessionFactory sessionFactory){
		super(name, connectionPool);
		this.sessionFactory = sessionFactory;
	}
	
	/******************************** methods **********************************/
	
	/****************************** ConnectionClient methods *************************/
	

	
	/****************************** JdbcConnectionClient methods *************************/


	
	/****************************** JdbcTxnClient methods *************************/


	
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
				logger.warn("problem closing session.  flush() threw exception.  handle=" + getExistingHandle());
				logger.warn(getStats());
				logger.warn("", e);
				try{
					logger.warn("ROLLING BACK TXN after failed flush().  handle=" + getExistingHandle());
					rollbackTxn();
				}catch(Exception e2){
					logger.warn("TXN ROLLBACK FAILED after flush() threw exception.  handle=" + getExistingHandle());
					logger.warn(getName() + " has " + DrMapTool.size(sessionByConnectionHandle) + " sessions");
					logger.warn("", e);
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
				session.clear();// otherwise things get left in the session factory??
			}catch(Exception e){
				logger.warn("problem clearing session.  clear() threw exception.  handle=" + getExistingHandle(), e);
			}
			try{
				session.disconnect();
			}catch(Exception e){
				logger.warn("problem closing session.  disconnect() threw exception.  handle=" + getExistingHandle(), e);
			}
			try{
				session.close();// should not be necessary, but best to be safe
			}catch(Exception e){
				logger.warn("problem closing session.  close() threw exception.  handle=" + getExistingHandle(), e);
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
		return super.getStats()
				+","+DrMapTool.size(sessionByConnectionHandle)+" sessionHandles";
	}
	
	
}
