package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.util.DatarouterEmailTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.concurrent.FutureTool;

public class ParallelSchemaUpdate
implements Callable<Void>{
	private static Logger logger = LoggerFactory.getLogger(ParallelSchemaUpdate.class);

	/************ static fields *******************/

	public static final String
	SERVER_NAME = "server.name",
	ADMINISTRATOR_EMAIL = "administrator.email",
	PRINT_PREFIX = "schemaUpdate.print",
	EXECUTE_PREFIX = "schemaUpdate.execute";


	/******************* fields **********************/

	private final DatarouterContext drContext;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final String clientName;
	private final JdbcConnectionPool connectionPool;

	private final Set<String> configFilePaths;
	private final List<Properties> multiProperties;

	private final SchemaUpdateOptions printOptions;
	private final SchemaUpdateOptions executeOptions;
	private final Set<String> updatedTables;
	private final List<String> printedSchemaUpdates;


	/***************** construct ***********************/

	public ParallelSchemaUpdate(DatarouterContext drContext, JdbcFieldCodecFactory fieldCodecFactory,
			String clientName, JdbcConnectionPool connectionPool){
		this.drContext = drContext;
		this.fieldCodecFactory = fieldCodecFactory;
		this.clientName = clientName;
		this.connectionPool = connectionPool;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.printOptions = new SchemaUpdateOptions(multiProperties, PRINT_PREFIX, true	);
		this.executeOptions = new SchemaUpdateOptions(multiProperties, EXECUTE_PREFIX, false);
		this.updatedTables = Collections.synchronizedSet(new TreeSet<String>());
		this.printedSchemaUpdates = Collections.synchronizedList(new ArrayList<String>());
	}


	/************** methods **********************/

	@Override
	public Void call(){
		//get the existing table names
		List<String> existingTableNames;
		Connection connection = null;
		try{
			connection = connectionPool.checkOut();
			existingTableNames = JdbcTool.showTables(connection);
		} finally{
			connectionPool.checkIn(connection);// is this how you return it to the pool?
		}

		//run an update for each PhysicalNode
		List<? extends PhysicalNode<?, ?>> physicalNodes = drContext.getNodes().getPhysicalNodesForClient(clientName);
		List<Callable<Void>> singleTableUpdates = new ArrayList<>();
		for(PhysicalNode<?, ?> physicalNode : DrIterableTool.nullSafe(physicalNodes)){
			DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
			if(fieldInfo.getFieldAware()){
				SingleTableSchemaUpdate singleTableUpdate = new SingleTableSchemaUpdate(fieldCodecFactory, clientName,
						connectionPool, existingTableNames, printOptions, executeOptions, updatedTables,
						printedSchemaUpdates, physicalNode);
				singleTableUpdates.add(singleTableUpdate);
			}
		}
		FutureTool.submitAndGetAll(singleTableUpdates, drContext.getExecutorService());

		sendEmail();
		return null;
	}


	private void sendEmail(){
		if(DrCollectionTool.isEmpty(printedSchemaUpdates)){ return; }
		if(DrStringTool.isEmpty(drContext.getAdministratorEmail()) || DrStringTool.isEmpty(drContext.getServerName())){
			//note: this can be caused by not calling drContext.activate().  need to fix this startup flaw.
			logger.warn("please set your datarouter administratorEmail and serverName");
			return;
		}
		String subject = "SchemaUpdate request from "+drContext.getServerName();
		StringBuilder body = new StringBuilder();
		for(String update : DrIterableTool.nullSafe(printedSchemaUpdates)){
			body.append(update + "\n\n");
		}
		DatarouterEmailTool.sendEmail("noreply@hotpads.com", drContext.getAdministratorEmail(), subject,
				body.toString());
	}
}
