package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.DatabaseCreator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.ParallelSchemaUpdate;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
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
			POOL_DEFAULT = "default",
			SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable",
			PRINT_PREFIX = "schemaUpdate.print",
			EXECUTE_PREFIX = "schemaUpdate.execute";

	protected final Datarouter datarouter;
	protected final ClientAvailabilitySettings clientAvailabilitySettings;
	protected final JdbcFieldCodecFactory fieldCodecFactory;

	protected final String clientName;
	protected final List<Properties> multiProperties;
	private final JdbcOptions jdbcOptions;
	private final JdbcOptions defaultJdbcOptions;
	protected final SchemaUpdateOptions schemaUpdatePrintOptions;
	protected final SchemaUpdateOptions schemaUpdateExecuteOptions;
	private final boolean schemaUpdateEnabled;

	public JdbcSimpleClientFactory(Datarouter datarouter, JdbcFieldCodecFactory fieldCodecFactory,
			String clientName, ClientAvailabilitySettings clientAvailabilitySettings){
		this.datarouter = datarouter;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.fieldCodecFactory = fieldCodecFactory;
		this.clientName = clientName;
		Set<String> configFilePaths = datarouter.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.jdbcOptions = new JdbcOptions(multiProperties, clientName);
		this.defaultJdbcOptions = new JdbcOptions(multiProperties, POOL_DEFAULT);

		this.schemaUpdatePrintOptions = new SchemaUpdateOptions(multiProperties, PRINT_PREFIX, true);
		this.schemaUpdateExecuteOptions = new SchemaUpdateOptions(multiProperties, EXECUTE_PREFIX, false);

		this.schemaUpdateEnabled = DrBooleanTool.isTrue(DrPropertiesTool.getFirstOccurrence(multiProperties,
				SCHEMA_UPDATE_ENABLE));
	}

	@Override
	public Client call(){
		PhaseTimer timer = new PhaseTimer(clientName);

		JdbcConnectionPool connectionPool = initConnectionPool();
		timer.add("pool");

		JdbcClient client = new JdbcClientImp(clientName, connectionPool, clientAvailabilitySettings);
		timer.add("client");

		if(doSchemaUpdate()){
			new ParallelSchemaUpdate(datarouter, fieldCodecFactory, clientName, connectionPool,
					schemaUpdatePrintOptions, schemaUpdateExecuteOptions).call();
			timer.add("schema update");
		}

		logger.warn(timer.toString());
		return client;
	}

	private boolean isWritableClient(){
		return ClientId.getWritableNames(datarouter.getClientPool().getClientIds()).contains(clientName);
	}

	protected JdbcConnectionPool initConnectionPool(){
		// check if the createDatabase option is set to true before checking for missing databases.
		if(doSchemaUpdate()){
			new DatabaseCreator(jdbcOptions, defaultJdbcOptions, clientName, schemaUpdatePrintOptions,
					schemaUpdateExecuteOptions).call();
		}
		return new JdbcConnectionPool(clientName, isWritableClient(), defaultJdbcOptions, jdbcOptions);
	}

	protected boolean doSchemaUpdate(){
		return isWritableClient() && schemaUpdateEnabled;
	}

}
