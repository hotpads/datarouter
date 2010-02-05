package com.hotpads.datarouter.connection;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

import com.hotpads.util.core.BooleanTool;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class JdbcConnectionPool{

	private String name;
	private ComboPooledDataSource pool;
	private boolean readOnly = false;  //don't currently enforce this, but probably should somehow
	
	public static final String
		prefix = ConnectionPools.prefixConnectionPool,
		poolDefault = "default",
		paramUrl = ".url",
		paramUser = ".user",
		paramPassword = ".password",
		paramMinPoolSize = ".minPoolSize",
		paramMaxPoolSize = ".maxPoolSize",
		paramReadOnly = ".readOnly",
		paramLogging = ".logging",
		nestedParamDataSource = ".param.dataSource";
	
	public JdbcConnectionPool(String name, Properties properties) 
			throws PropertyVetoException, NamingException
	{
		String source = properties.getProperty(prefix+name+ConnectionPools.paramSource);
//		if("params".equals(source)){
//			createFromParams(name, properties);
//		}else{
			createFromScratch(name, properties);
//		}
	}
	
	@Override
	public String toString(){
		return this.name+"@"+this.pool.getJdbcUrl();
	}
	
	public void createFromScratch(String name, Properties properties)
			throws PropertyVetoException, NamingException
	{
		this.name = name;

		
		//get defaults
		String defaultUser = properties.getProperty(prefix+poolDefault+paramUser);
		String defaultPassword = properties.getProperty(prefix+poolDefault+paramPassword);
		String defaultMinPoolSizeString = properties.getProperty(prefix+poolDefault+paramMinPoolSize);
		String defaultMaxPoolSizeString = properties.getProperty(prefix+poolDefault+paramMaxPoolSize);
		String defaultLoggingString = properties.getProperty(prefix+poolDefault+paramLogging);

		if(defaultUser==null){ defaultUser = "root"; }
		if(defaultPassword==null){ defaultPassword = ""; }
		if(defaultMinPoolSizeString==null){ defaultMinPoolSizeString = "1"; }
		if(defaultMaxPoolSizeString==null){ defaultMaxPoolSizeString = "20"; }
		if(defaultLoggingString==null){ defaultLoggingString = "false"; }
		
		
		//get values and substitute defaults if not present
		String url = properties.getProperty(prefix+name+paramUrl);
		String user = properties.getProperty(prefix+name+paramUser);
		String password = properties.getProperty(prefix+name+paramPassword);
		String minPoolSizeString = properties.getProperty(prefix+name+paramMinPoolSize);
		String maxPoolSizeString = properties.getProperty(prefix+name+paramMaxPoolSize);
		String readOnlyString = properties.getProperty(prefix+name+paramReadOnly);
		String loggingString = properties.getProperty(prefix+name+paramLogging);

		if(user==null){ user = defaultUser; }
		if(password==null){ password = defaultPassword; }
		if(minPoolSizeString==null){ minPoolSizeString = defaultMinPoolSizeString; }
		if(maxPoolSizeString==null){ maxPoolSizeString = defaultMaxPoolSizeString; }
		if(loggingString==null){ loggingString = defaultLoggingString; }
		
		
		//configurable props
		this.pool = new ComboPooledDataSource();
		
		try{
			Integer minPoolSize = Integer.valueOf(minPoolSizeString);
			this.pool.setMaxPoolSize(minPoolSize);
		}catch(Exception e){
		}
		
		try{
			Integer maxPoolSize = Integer.valueOf(maxPoolSizeString);
			this.pool.setMaxPoolSize(maxPoolSize);
		}catch(Exception e){
		}
		
		this.readOnly = BooleanTool.isTrue(readOnlyString);
		
		boolean logging = BooleanTool.isTrue(loggingString);
		
		if(logging){
			//log4jdbc - see http://code.google.com/p/log4jdbc/
			this.pool.setJdbcUrl("jdbc:log4jdbc:mysql://"+url);
			this.pool.setDriverClass(net.sf.log4jdbc.DriverSpy.class.getName());
		}else{
			//normal jdbc
			this.pool.setJdbcUrl("jdbc:mysql://"+url);
			this.pool.setDriverClass("com.mysql.jdbc.Driver");
		}
		
		this.pool.setUser(user);
		this.pool.setPassword(password);
		this.pool.setMinPoolSize(1);
		this.pool.setAcquireIncrement(1);
		this.pool.setAcquireRetryAttempts(30);
		this.pool.setAcquireRetryDelay(500);
		this.pool.setIdleConnectionTestPeriod(30);
		this.pool.setMaxIdleTime(300);
		
		if(this.readOnly){
			this.pool.setConnectionCustomizerClassName(ReadOnlyConnectionCustomizer.class.getName());
		}
		
	}
	
	
//	public void createFromParams(String name, Properties properties, Map<String, Object> params){
//		this.name = name;
//
//		String poolParamKey = properties.getProperty(prefix+name+nestedParamDataSource);
//		this.pool = (ComboPooledDataSource)params.get(poolParamKey);
//	}
	
	
	public void shutdown(){
		try{
			DataSources.destroy(this.getDataSource());
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	

	/******************************* get/set *****************************/
	
	public String getName() {
		return name;
	}

	public ComboPooledDataSource getDataSource() {
		return pool;
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
