package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.exception.DataAccessException;

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
	protected Map<String,Connection> connectionByName = Collections.synchronizedMap(new HashMap<String,Connection>());
	protected Map<String,Session> sessionByConnectionName = Collections.synchronizedMap(new HashMap<String,Session>());

	long connectionCounter = -1;
	
	
	/**************************** private methods **********************************/
	
	String getConnectionNameForThisClient(Config config){
		if(config==null){ return null; }
		if(config.getConnectionNameByClientName()==null){ return null; }
		return config.getConnectionNameByClientName().get(this.name);
	}
	
	Connection getConnection(Config config){
		String connectionNameForThisClient = this.getConnectionNameForThisClient(config);
		if(connectionNameForThisClient==null){ return null; }
		return this.connectionByName.get(connectionNameForThisClient);
	}

	
	/****************************** ConnectionClient methods *************************/

	@Override
	public String reserveConnection(String tryConnectionName){
		try {
			Connection connection = this.connectionByName.get(tryConnectionName);
			String connName = tryConnectionName;
			if(connection != null){
				logger.debug("got existing connection:"+connName+":"+this.connectionByName.keySet());
				return connName;
			}
			connection = this.connectionPool.getDataSource().getConnection();
			long connNumber = ++this.connectionCounter;
			connName = tryConnectionName + "-" + connNumber;
			this.connectionByName.put(connName, connection);
			logger.debug("new connection:"+connName/*+":"+this.connectionByName.keySet()*/);
			return connName;
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public void releaseConnection(String connectionName){
		try {
			Connection connection = this.connectionByName.get(connectionName);
			this.connectionByName.remove(connectionName);
			connection.close();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	
	/****************************** JdbcConnectionClient methods *************************/
	
	@Override
	public Connection getExistingConnection(String connectionName){
		return this.connectionByName.get(connectionName);
	}

	
	/****************************** JdbcTxnClient methods *************************/

	@Override
	public String beginTxn(String tryConnectionName, Isolation isolation){
		try {
			String connectionName = this.reserveConnection(tryConnectionName);
			Connection connection = this.connectionByName.get(connectionName);
			connection.setTransactionIsolation(isolation.getJdbcVal());
			logger.debug("setTransactionIsolation="+isolation.getJdbcVal()+" on "+connectionName);
			connection.setAutoCommit(false);
			logger.debug("setAutoCommit=false on "+connectionName);
			logger.debug("began txn on:"+connectionName);
			return connectionName;
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public void commitTxn(String connectionName){
		try{
			Connection connection = this.connectionByName.get(connectionName);
			connection.commit();
			logger.debug("committed txn on:"+connectionName);
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public void rollbackTxn(String connectionName){
		try{
			Connection connection = this.connectionByName.get(connectionName);
			connection.rollback();
			logger.debug("rolled-back txn on:"+connectionName);
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	
	/****************************** SessionClient methods *************************/
	
	@Override
	public String openSession(String tryConnectionName){
		String connectionName = this.reserveConnection(tryConnectionName);
		Connection connection = this.connectionByName.get(connectionName);
		Session session = this.sessionFactory.openSession(connection);
		this.sessionByConnectionName.put(connectionName, session);
		return connectionName;
	}
	
	@Override
	public void closeSession(String connectionName){
		Session session = this.sessionByConnectionName.get(connectionName);
		if(session != null){
			session.close();
		}
		this.sessionByConnectionName.remove(connectionName);
	}

	
	/****************************** HibernateClient methods *************************/
	
	@Override
	public Session getExistingSession(String connectionName){
		return this.sessionByConnectionName.get(connectionName);
	}

	@Override
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	
	
}
