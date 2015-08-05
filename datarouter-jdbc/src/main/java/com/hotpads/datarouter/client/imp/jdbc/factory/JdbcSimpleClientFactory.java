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
import com.hotpads.util.core.profile.PhaseTimer;

public class JdbcSimpleClientFactory
implements ClientFactory{
	private static Logger logger = LoggerFactory.getLogger(JdbcSimpleClientFactory.class);

	private static final String SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable";


	private final DatarouterContext drContext;
	protected final JdbcFieldCodecFactory fieldCodecFactory;
	private final String clientName;
	private final Set<String> configFilePaths;
	private final List<Properties> multiProperties;

	private JdbcConnectionPool connectionPool;
	private JdbcClient client;

	public JdbcSimpleClientFactory(DatarouterContext drContext, JdbcFieldCodecFactory fieldCodecFactory,
			String clientName){
		this.drContext = drContext;
		this.fieldCodecFactory = fieldCodecFactory;
		this.clientName = clientName;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
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
		checkDatabaseExist();
		connectionPool = new JdbcConnectionPool(drContext.getApplicationPaths(), clientName,
				multiProperties, isWritableClient());
	}


	protected boolean doSchemaUpdate(){
		boolean schemaUpdateEnabled = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				SCHEMA_UPDATE_ENABLE));
		return isWritableClient() && schemaUpdateEnabled;
	}

	private boolean checkDatabaseExist(){
		String genericUrl = new JdbcOptions(multiProperties, clientName).getGenericUrl();
		Connection connection = JdbcTool.openConnectionNotSpecifyingDb(genericUrl, "root", "");
		List<String> existingDatabases = JdbcTool.showDatabases(connection);
		//if database does not exist, create database
		if(!existingDatabases.contains(clientName)){
			if(isWritableClient()){
				try{
					generateCreateDatabaseSchema(connection, clientName);
				}catch(SQLException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
		}
		return true;
	}

	private void generateCreateDatabaseSchema(Connection connection, String databaseName) throws SQLException{
		System.out.println("========================================== Creating the database " +databaseName
				+" ============================");
		String sql = "Create database "+ databaseName +" ;";
		System.out.println("Please execute: "+sql);
		/*if(!executeOptions.getCreateTables()){
			System.out.println("Please execute: "+sql);
		}
		else{
			System.out.println(sql);
			statement.execute(sql);*/
		System.out.println("============================================================================="
				+"=======================");

		Statement statement = connection.createStatement();
		statement.execute(sql);

		//}

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
