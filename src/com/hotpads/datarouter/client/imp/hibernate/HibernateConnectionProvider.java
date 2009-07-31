package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;

import com.hotpads.datarouter.connection.JdbcConnectionPool;

public class HibernateConnectionProvider implements ConnectionProvider{

	DataSource dataSource;

	@Override
	public void configure(Properties props) throws HibernateException {
		this.dataSource = getConnectionPoolFromThread().getDataSource();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();		
	}

	@Override
	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
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
