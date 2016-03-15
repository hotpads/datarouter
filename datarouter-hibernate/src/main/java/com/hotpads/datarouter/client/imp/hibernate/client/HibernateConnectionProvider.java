package com.hotpads.datarouter.client.imp.hibernate.client;

import java.sql.Connection;
import java.util.Properties;

import org.hibernate.connection.ConnectionProvider;

import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.exception.UnavailableException;

public class HibernateConnectionProvider implements ConnectionProvider{

	private JdbcConnectionPool connectionPool;

	@Override
	public void configure(Properties props){
		this.connectionPool = getConnectionPoolFromThread();
	}

	@Override
	public Connection getConnection(){
		try{
			return connectionPool.checkOut();
		}catch(Exception e){
			throw new UnavailableException(e);
		}
	}

	@Override
	public void closeConnection(Connection conn){
		try{
			conn.close();
		}catch(Exception e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public void close(){
	}

	@Override
	public boolean supportsAggressiveRelease() {
		// TODO Auto-generated method stub
		return false;
	}


	/******************************* configuration stuff (very weird) *****************************/

	private static final ThreadLocal<JdbcConnectionPool> connectionPoolHolder = new ThreadLocal<>();

	public static void bindDataSourceToThread(JdbcConnectionPool connectionPool){
		connectionPoolHolder.set(connectionPool);
	}

	public static JdbcConnectionPool getConnectionPoolFromThread(){
		return connectionPoolHolder.get();
	}

	public static void clearConnectionPoolFromThread(){
		connectionPoolHolder.set(null);
	}

}
