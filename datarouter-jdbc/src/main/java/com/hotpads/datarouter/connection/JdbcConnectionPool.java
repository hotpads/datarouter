package com.hotpads.datarouter.connection;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcOptions;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class JdbcConnectionPool{
	private static final Logger logger = LoggerFactory.getLogger(JdbcConnectionPool.class);

	public static final String 	POOL_DEFAULT = "default";
	
	private String name;
	private ComboPooledDataSource pool;
	private final JdbcOptions defaultOptions;
	private final JdbcOptions options;
	private boolean writable = false;
	private String schemaName;


	public JdbcConnectionPool(String name, Boolean writable,
			JdbcOptions defaultOptions, JdbcOptions clientOptions){
		this.defaultOptions = defaultOptions;
		this.options = clientOptions;
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

		schemaName = DrStringTool.getStringAfterLastOccurrence('/', url);

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

		List<String> urlParams = new ArrayList<>();
		//avoid extra RPC on readOnly connections: http://dev.mysql.com/doc/relnotes/connector-j/en/news-5-1-23.html
		urlParams.add("useLocalSessionState=true");

		String urlWithParams = url + "?" + Joiner.on("&").join(urlParams);
		try {
			String jdbcUrl;
			if(logging){
				//log4jdbc - see http://code.google.com/p/log4jdbc/
				pool.setDriverClass(net.sf.log4jdbc.DriverSpy.class.getName());
				jdbcUrl = "jdbc:log4jdbc:mysql://"+urlWithParams;
			}else{
				jdbcUrl = "jdbc:mysql://"+urlWithParams;
				pool.setDriverClass("com.mysql.jdbc.Driver");
			}
			pool.setJdbcUrl(jdbcUrl);
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
			logger.error("", e);
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

	public String getSchemaName(){
		return schemaName;
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
