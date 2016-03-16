package com.hotpads.datarouter.connection;

import java.beans.PropertyVetoException;
import java.nio.charset.StandardCharsets;
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
import com.mysql.jdbc.Driver;

import net.sf.log4jdbc.DriverSpy;

public class JdbcConnectionPool{
	private static final Logger logger = LoggerFactory.getLogger(JdbcConnectionPool.class);

	public static final String UTF8MB4_CHARSET = "utf8mb4";
	private static final String UTF8MB4_COLLATION = UTF8MB4_CHARSET + "_bin";
	private static final String UTF8 = StandardCharsets.UTF_8.name();

	private final String name;
	private final ComboPooledDataSource pool;
	private final boolean writable;
	private final String schemaName;


	public JdbcConnectionPool(String name, Boolean writable, JdbcOptions defaultOptions, JdbcOptions clientOptions){
		this.writable = writable;
		this.name = name;

		String url = clientOptions.url();
		String user = clientOptions.user(defaultOptions.user("root"));
		String password = clientOptions.password(defaultOptions.password(""));
		Integer minPoolSize = clientOptions.minPoolSize(defaultOptions.minPoolSize(1));
		Integer maxPoolSize = clientOptions.maxPoolSize(defaultOptions.maxPoolSize(20));
		Boolean logging = clientOptions.logging(defaultOptions.logging(false));

		this.schemaName = DrStringTool.getStringAfterLastOccurrence('/', url);

		// configurable props
		this.pool = new ComboPooledDataSource();
		this.pool.setInitialPoolSize(minPoolSize);
		this.pool.setMinPoolSize(minPoolSize);
		this.pool.setMaxPoolSize(maxPoolSize);

		List<String> urlParams = new ArrayList<>();
		// avoid extra RPC on readOnly connections: http://dev.mysql.com/doc/relnotes/connector-j/en/news-5-1-23.html
		urlParams.add("useLocalSessionState=true");
		urlParams.add("zeroDateTimeBehavior=convertToNull");
		urlParams.add("connectionCollation=" + UTF8MB4_COLLATION);
		urlParams.add("characterEncoding=" + UTF8);

		String urlWithParams = url + "?" + Joiner.on("&").join(urlParams);
		try{
			String jdbcUrl;
			if(logging){
				// log4jdbc - see http://code.google.com/p/log4jdbc/
				this.pool.setDriverClass(DriverSpy.class.getName());
				jdbcUrl = "jdbc:log4jdbc:mysql://" + urlWithParams;
			}else{
				jdbcUrl = "jdbc:mysql://" + urlWithParams;
				this.pool.setDriverClass(Driver.class.getName());
			}
			this.pool.setJdbcUrl(jdbcUrl);
		}catch(PropertyVetoException pve){
			throw new RuntimeException(pve);
		}

		this.pool.setUser(user);
		this.pool.setPassword(password);
		this.pool.setAcquireIncrement(1);
		this.pool.setAcquireRetryAttempts(30);
		this.pool.setAcquireRetryDelay(500);
		this.pool.setIdleConnectionTestPeriod(30);
		this.pool.setMaxIdleTime(300);

		if(writable){
			this.pool.setConnectionCustomizerClassName(Utf8mb4ConnectionCustomizer.class.getName());
		}else{
			this.pool.setConnectionCustomizerClassName(ReadOnlyUtf8mb4ConnectionCustomizer.class.getName());
		}
	}

	public Connection checkOut() throws SQLException{
		return pool.getConnection();
	}

	public void checkIn(Connection connection){
		if(connection == null){
			return;
		}
		try{
			connection.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

	public void shutdown(){
		try{
			DataSources.destroy(pool);
		}catch(SQLException e){
			logger.error("", e);
		}
	}

	/******************************* get/set *****************************/

	public String getName(){
		return name;
	}

	public boolean isWritable(){
		return writable;
	}

	public String getSchemaName(){
		return schemaName;
	}

	@Override
	public String toString(){
		return name + "@" + pool.getJdbcUrl();
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
