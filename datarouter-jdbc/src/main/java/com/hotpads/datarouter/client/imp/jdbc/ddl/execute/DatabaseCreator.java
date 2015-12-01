package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcOptions;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class DatabaseCreator{
	private static final Logger logger = LoggerFactory.getLogger(DatabaseCreator.class);

	private final JdbcOptions jdbcOptions;
	private final JdbcOptions defaultJdbcOptions;
	private final String clientName;
	private final SchemaUpdateOptions printOptions;
	private final SchemaUpdateOptions executeOptions;

	public DatabaseCreator(JdbcOptions jdbcOptions, JdbcOptions defaultJdbcOptions, String clientName,
			SchemaUpdateOptions printOptions, SchemaUpdateOptions executeOptions){
		this.jdbcOptions = jdbcOptions;
		this.defaultJdbcOptions = defaultJdbcOptions;
		this.clientName = clientName;
		this.printOptions = printOptions;
		this.executeOptions = executeOptions;
	}

	public void call(){
		if(printOptions.getCreateDatabases() || executeOptions.getCreateDatabases()){
			checkDatabaseExist();
		}
	}

	private void checkDatabaseExist(){
		String url =  jdbcOptions.url();
		String user = jdbcOptions.user(defaultJdbcOptions.user("root"));
		String password = jdbcOptions.password(defaultJdbcOptions.password(""));
		String hostname = DrStringTool.getStringBeforeLastOccurrence(':',url);
		String portDatabaseString = DrStringTool.getStringAfterLastOccurrence(':',url);
		int port = Integer.parseInt(DrStringTool.getStringBeforeLastOccurrence('/',portDatabaseString));
		String databaseName = DrStringTool.getStringAfterLastOccurrence('/',portDatabaseString);

		Connection connection = JdbcTool.openConnection(hostname, port, null, user, password);
		List<String> existingDatabases = JdbcTool.showDatabases(connection);

		//if database does not exist, create database
		if(!existingDatabases.contains(databaseName)){
			generateCreateDatabaseSchema(connection, clientName);
		}
	}

	private void generateCreateDatabaseSchema(Connection connection, String databaseName){
		logger.info("========================================== Creating the database " + databaseName
				+ " ============================");
		String sql = "Create database "+ databaseName +" ;";
		if(!executeOptions.getCreateDatabases()){
			logger.info("Please execute: "+sql);
			// TODO email the admin ?
		}else{
			try{
				logger.info(" Executing "+sql);
				Statement statement = connection.createStatement();
				statement.execute(sql);
			}catch(SQLException e){
				throw new RuntimeException(e);
			}
		}
	}

}
