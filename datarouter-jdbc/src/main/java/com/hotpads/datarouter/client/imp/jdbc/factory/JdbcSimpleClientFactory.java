package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.ParallelSchemaUpdate;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class JdbcSimpleClientFactory
implements ClientFactory{
	private static Logger logger = LoggerFactory.getLogger(JdbcSimpleClientFactory.class);

	private static final String SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable";
	private static final String CREATE_DATABASE = "createDatabases";
	public static final String 	POOL_DEFAULT = "default";

	private final DatarouterContext drContext;
	protected final JdbcFieldCodecFactory fieldCodecFactory;
	private final String clientName;
	private final Set<String> configFilePaths;
	private final List<Properties> multiProperties;
	private final JdbcOptions jdbcOptions;
	private final JdbcOptions defaultJdbcOptions;
	private final boolean shouldExecuteCreateDb;
	private final boolean shouldPrintCreateDb;
	
	private JdbcConnectionPool connectionPool;
	private JdbcClient client;

	public JdbcSimpleClientFactory(DatarouterContext drContext, JdbcFieldCodecFactory fieldCodecFactory,
			String clientName){
		this.drContext = drContext;
		this.fieldCodecFactory = fieldCodecFactory;
		this.clientName = clientName;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.jdbcOptions = new JdbcOptions(multiProperties, clientName);
		this.defaultJdbcOptions = new JdbcOptions(multiProperties, POOL_DEFAULT);
		this.shouldExecuteCreateDb = checkJdbcOption(ParallelSchemaUpdate.EXECUTE_PREFIX+"."+CREATE_DATABASE);
		this.shouldPrintCreateDb = checkJdbcOption(ParallelSchemaUpdate.PRINT_PREFIX+"."+CREATE_DATABASE);
	}

	@Override
	public Client call(){
		PhaseTimer timer = new PhaseTimer(clientName);

		initConnectionPool();
		timer.add("pool");

		client = new JdbcClientImp(clientName, connectionPool);
		timer.add("client");

		if(doSchemaUpdate()){
			new ParallelSchemaUpdate(drContext, fieldCodecFactory, clientName, connectionPool).call();
			timer.add("schema update");
		}

		logger.warn(timer.toString());
		return client;
	}

	private boolean isWritableClient(){
		return ClientId.getWritableNames(drContext.getClientPool().getClientIds()).contains(clientName);
	}

	protected void initConnectionPool(){
		//if the schemaupdate option ofr execute and print is set to false, then do not check for Schema difference
		if(shouldExecuteCreateDb || shouldPrintCreateDb){
			checkDatabaseExist();
		}
		connectionPool = new JdbcConnectionPool(clientName,	isWritableClient(), defaultJdbcOptions, jdbcOptions);
	}


	protected boolean doSchemaUpdate(){
		return isWritableClient() && checkJdbcOption(SCHEMA_UPDATE_ENABLE);
	}

	private void checkDatabaseExist() {
		String url =  jdbcOptions.url();
		String user = jdbcOptions.user("root");
		String password = jdbcOptions.password("");
		String hostname = DrStringTool.getStringBeforeLastOccurrence(':',url);
		String portDatabaseString = DrStringTool.getStringAfterLastOccurrence(':',url);
		int port = Integer.parseInt(DrStringTool.getStringBeforeLastOccurrence('/',portDatabaseString));
		String databaseName = DrStringTool.getStringAfterLastOccurrence('/',portDatabaseString);

		Connection connection = JdbcTool.openConnection(hostname, port, null, user, password);
		List<String> existingDatabases = JdbcTool.showDatabases(connection);
		//if database does not exist, create database
		if(!existingDatabases.contains(databaseName)){
			if(isWritableClient()){
				generateCreateDatabaseSchema(connection, clientName);
			}
		}
	}

	private void generateCreateDatabaseSchema(Connection connection, String databaseName){
		System.out.println("========================================== Creating the database " +databaseName
				+" ============================");
		String sql = "Create database "+ databaseName +" ;";
		if(!shouldExecuteCreateDb){
			System.out.println("Please execute: "+sql);
		}else {
			try{
				System.out.println(sql);
				Statement statement = connection.createStatement();
				statement.execute(sql);
			}catch(SQLException e){
				throw new RuntimeException(e);
			}
		}
	}
	
	private boolean checkJdbcOption(String requiredString){
		return DrBooleanTool.isTrue(jdbcOptions.getRequiredString(requiredString));
		
	}

	public String getClientName(){
		return clientName;
	}

	public List<Properties> getMultiProperties(){
		return multiProperties;
	}

	public DatarouterContext getDrContext(){
		return drContext;
	}

	public JdbcConnectionPool getConnectionPool(){
		return connectionPool;
	}


}
