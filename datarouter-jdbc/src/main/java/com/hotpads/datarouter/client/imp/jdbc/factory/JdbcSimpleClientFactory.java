package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.ParallelSchemaUpdate;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class JdbcSimpleClientFactory 
implements ClientFactory{
	private static Logger logger = LoggerFactory.getLogger(JdbcSimpleClientFactory.class);

	private static final String 
		SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable";
	

	private DatarouterContext drContext;
	private String clientName;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	
	private JdbcConnectionPool connectionPool;
	private JdbcClient client;

	public JdbcSimpleClientFactory(DatarouterContext drContext, String clientName){
		this.drContext = drContext;
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
			new ParallelSchemaUpdate(drContext, clientName, connectionPool).call();
			timer.add("schema update");
		}

		logger.warn(timer.toString());
		return client;
	}
	
	private boolean isWritableClient(){
		return ClientId.getWritableNames(drContext.getClientPool().getClientIds()).contains(clientName);
	}
	
	protected void initConnectionPool(){
		connectionPool = new JdbcConnectionPool(drContext.getApplicationPaths(), clientName,
				multiProperties, isWritableClient());
	}
	
	protected boolean doSchemaUpdate(){
		boolean schemaUpdateEnabled = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties, 
				SCHEMA_UPDATE_ENABLE));
		return isWritableClient() && schemaUpdateEnabled;
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
