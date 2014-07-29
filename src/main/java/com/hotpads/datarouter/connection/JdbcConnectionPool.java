package com.hotpads.datarouter.connection;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcOptions;
import com.hotpads.util.core.ExceptionTool;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class JdbcConnectionPool{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String name;
	private ComboPooledDataSource pool;
	protected JdbcOptions defaultOptions;
	protected JdbcOptions options;
	private boolean writable = false;
	

	public static final String
		prefix = ConnectionPools.prefixPool,
		poolDefault = "default";
	
	public JdbcConnectionPool(String name, Iterable<Properties> multiProperties, Boolean writable){
		this.defaultOptions = new JdbcOptions(multiProperties, poolDefault);
		this.options = new JdbcOptions(multiProperties, name);
		this.writable = writable;
		createFromScratch(name);
	}
	
	@Override
	public String toString(){
		return name+"@"+pool.getJdbcUrl();
	}
	
	public void createFromScratch(String name){
		this.name = name;
		
		String url = options.url();
		String user = options.user(defaultOptions.user("root"));
		String password = options.password(defaultOptions.password(""));
		Integer minPoolSize = options.minPoolSize(defaultOptions.minPoolSize(1));
		Integer maxPoolSize = options.maxPoolSize(defaultOptions.maxPoolSize(20));
		Boolean logging = options.logging(defaultOptions.logging(false));
		
		
		//configurable props
		pool = new ComboPooledDataSource();
		
		try{
			pool.setMinPoolSize(minPoolSize);
		}catch(Exception e){
		}
		
		try{
			pool.setMaxPoolSize(maxPoolSize);
		}catch(Exception e){
		}
		
		try {
			if(logging){
				//log4jdbc - see http://code.google.com/p/log4jdbc/
				pool.setJdbcUrl("jdbc:log4jdbc:mysql://"+url);
				pool.setDriverClass(net.sf.log4jdbc.DriverSpy.class.getName());
			}else{
				//normal jdbc
				pool.setJdbcUrl("jdbc:mysql://"+url);
				pool.setDriverClass("com.mysql.jdbc.Driver");
			}
		}catch(PropertyVetoException pve) {
			throw new RuntimeException(pve);
		}
		
		pool.setUser(user);
		pool.setPassword(password);
		pool.setAcquireIncrement(1);
		pool.setAcquireRetryAttempts(30);
		pool.setAcquireRetryDelay(500);
		pool.setIdleConnectionTestPeriod(30);
		pool.setMaxIdleTime(300);

		if(!writable){
			pool.setConnectionCustomizerClassName(ReadOnlyConnectionCustomizer.class.getName());
		}
		
	}
	
	public Connection checkOut(){
		try{
			return pool.getConnection();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}
	
	public void checkIn(Connection connection){
		if(connection==null){ return; }
		try{
			connection.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}
	
	public void shutdown(){
		try{
			DataSources.destroy(getDataSource());
		}catch(SQLException e){
			logger.error(ExceptionTool.getStackTraceAsString(e));
		}
	}
	

	/******************************* get/set *****************************/
	
	public String getName() {
		return name;
	}

	public ComboPooledDataSource getDataSource() {
		return pool;
	}
	
	public boolean isWritable() {
		return writable;
	}
	
	/*

	<bean id="dataSourcePoolSales"
		class="com.mchange.v2.c3p0.ComboPooledDataSource">
		<property name="driverClass"><value>com.mysql.jdbc.Driver</value></property>
		<property name="jdbcUrl"><ref local="dataSourceSalesUrl"/></property>
		<property name="user"><ref local="dataSourceSalesUser"/></property>
		<property name="password"><ref local="dataSourceSalesPassword"/></property>
		<property name="minPoolSize"><value>1</value></property>
		<property name="acquireIncrement"><value>1</value></property>
		<property name="acquireRetryAttempts"><value>30</value></property>
		<property name="acquireRetryDelay"><value>500</value></property>
		<!-- <property name="checkoutTimeout"><value>10</value></property>  -->
		<property name="maxPoolSize"><value>10</value></property>
		<property name="idleConnectionTestPeriod"><value>300</value></property>
		<!-- <property name="automaticTestTable"><value>c3p0TestTable</value></property>-->
		<property name="maxIdleTime"><value>3600</value></property> 		
		<!-- 	<property name="maxStatements"><value>200</value></property> -->
	</bean>


	 */
	
	
}
