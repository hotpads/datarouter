package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.SchemaUpdateOptions;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.DatabaseCreator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.JdbcSchemaUpdateService;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.JdbcSchemaUpdateService.JdbcSchemaUpdateServiceFactory;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class JdbcSimpleClientFactory
implements ClientFactory{
	private static final Logger logger = LoggerFactory.getLogger(JdbcSimpleClientFactory.class);

	private static final String
			POOL_DEFAULT = "default";

	private final Datarouter datarouter;
	private final ClientAvailabilitySettings clientAvailabilitySettings;
	private final JdbcSchemaUpdateServiceFactory jdbcSchemaUpdateServiceFactory;

	private final String clientName;
	private final JdbcOptions jdbcOptions;
	private final JdbcOptions defaultJdbcOptions;
	private final SchemaUpdateOptions schemaUpdatePrintOptions;
	private final SchemaUpdateOptions schemaUpdateExecuteOptions;
	private final boolean schemaUpdateEnabled;

	public JdbcSimpleClientFactory(Datarouter datarouter, String clientName,
			ClientAvailabilitySettings clientAvailabilitySettings,
			JdbcSchemaUpdateServiceFactory jdbcSchemaUpdateServiceFactory){
		this.datarouter = datarouter;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.clientName = clientName;
		this.jdbcSchemaUpdateServiceFactory = jdbcSchemaUpdateServiceFactory;
		Set<String> configFilePaths = datarouter.getConfigFilePaths();
		List<Properties> multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.jdbcOptions = new JdbcOptions(multiProperties, clientName);
		this.defaultJdbcOptions = new JdbcOptions(multiProperties, POOL_DEFAULT);
		this.schemaUpdatePrintOptions = new SchemaUpdateOptions(multiProperties, JdbcSchemaUpdateService.PRINT_PREFIX,
				true);
		this.schemaUpdateExecuteOptions = new SchemaUpdateOptions(multiProperties,
				JdbcSchemaUpdateService.EXECUTE_PREFIX, false);
		this.schemaUpdateEnabled = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				SchemaUpdateOptions.SCHEMA_UPDATE_ENABLE));
	}

	@Override
	public Client call(){
		PhaseTimer timer = new PhaseTimer(clientName);

		JdbcConnectionPool connectionPool = initConnectionPool();
		timer.add("pool");

		JdbcSchemaUpdateService schemaUpdateService = jdbcSchemaUpdateServiceFactory.create(connectionPool);
		JdbcClient client = new JdbcClientImp(clientName, connectionPool, schemaUpdateService,
				clientAvailabilitySettings, schemaUpdateEnabled);
		timer.add("client");

		logger.warn(timer.toString());
		return client;
	}

	private boolean isWritableClient(){
		return datarouter.getClientPool().getClientId(clientName).getWritable();
	}

	protected JdbcConnectionPool initConnectionPool(){
		// check if the createDatabase option is set to true before checking for missing databases.
		if(doSchemaUpdate()){
			new DatabaseCreator(jdbcOptions, defaultJdbcOptions, schemaUpdatePrintOptions, schemaUpdateExecuteOptions)
					.call();
		}
		return new JdbcConnectionPool(clientName, isWritableClient(), defaultJdbcOptions, jdbcOptions);
	}

	protected boolean doSchemaUpdate(){
		return isWritableClient() && schemaUpdateEnabled;
	}

}
