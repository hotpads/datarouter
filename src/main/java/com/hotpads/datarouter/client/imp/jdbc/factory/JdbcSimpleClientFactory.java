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
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class JdbcSimpleClientFactory 
implements ClientFactory{
	private static Logger logger = LoggerFactory.getLogger(JdbcSimpleClientFactory.class);

	private static final String 
		SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable";
	

	private DataRouterContext drContext;
	private String clientName;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	
	private JdbcConnectionPool connectionPool;
	private JdbcClient client;

	public JdbcSimpleClientFactory(DataRouterContext drContext, String clientName){
		this.drContext = drContext;
		this.clientName = clientName;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
	}

	@Override
	public Client call(){
		PhaseTimer timer = new PhaseTimer(clientName);
		
		connectionPool = getConnectionPool(clientName, multiProperties);
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
	
	protected JdbcConnectionPool getConnectionPool(String clientName, List<Properties> multiProperties){
		JdbcConnectionPool connectionPool = new JdbcConnectionPool(drContext.getApplicationRootPath(), clientName,
				multiProperties, isWritableClient());
		return connectionPool;
	}
	
	protected boolean doSchemaUpdate(){
		boolean schemaUpdateEnabled = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, 
				SCHEMA_UPDATE_ENABLE));
		return isWritableClient() && schemaUpdateEnabled;
	}

	public String getClientName(){
		return clientName;
	}

	public List<Properties> getMultiProperties(){
		return multiProperties;
	}

	public DataRouterContext getDrContext(){
		return drContext;
	}
	
	
	
}
