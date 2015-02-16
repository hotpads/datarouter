package com.hotpads.datarouter.client.imp.jdbc;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.connection.ConnectionProvider;

import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.exception.UnavailableException;

public class JdbcConnectionProvider implements ConnectionProvider{

	private DataSource dataSource;

	@Override
	public void configure(Properties props){
		this.dataSource = getConnectionPoolFromThread().getDataSource();
	}

	@Override
	public Connection getConnection(){
		try{
			Connection connection = dataSource.getConnection();
			return connection;		
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
	public void close() {
	}

	@Override
	public boolean supportsAggressiveRelease() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/******************************* configuration stuff (very weird) *****************************/

	private static final ThreadLocal<JdbcConnectionPool> connectionPoolHolder = new ThreadLocal<JdbcConnectionPool>();
	
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
