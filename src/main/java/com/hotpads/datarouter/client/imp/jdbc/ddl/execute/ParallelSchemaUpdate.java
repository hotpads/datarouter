package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.util.DataRouterEmailTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.concurrent.FutureTool;

public class ParallelSchemaUpdate 
implements Callable<Void>{
	
	/************ static fields *******************/
	
	public static final String 
		SERVER_NAME = "server.name",
		ADMINISTRATOR_EMAIL = "administrator.email",
		PRINT_PREFIX = "schemaUpdate.print",
		EXECUTE_PREFIX = "schemaUpdate.execute";
	
	
	/******************* fields **********************/

	private DataRouterContext drContext;
	private String clientName;
	private JdbcConnectionPool connectionPool;
	
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;

	private SchemaUpdateOptions printOptions;
	private SchemaUpdateOptions executeOptions;
	private Set<String> updatedTables;
	private List<String> printedSchemaUpdates;
	
	
	/***************** construct ***********************/

	public ParallelSchemaUpdate(DataRouterContext drContext, String clientName, JdbcConnectionPool connectionPool){
		this.drContext = drContext;
		this.clientName = clientName;
		this.connectionPool = connectionPool;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
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
		List<Callable<Void>> singleTableUpdates = ListTool.createArrayList();
		for(PhysicalNode<?, ?> physicalNode : IterableTool.nullSafe(physicalNodes)){
			DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
			if(fieldInfo.getFieldAware()){
				SingleTableSchemaUpdate singleTableUpdate = new SingleTableSchemaUpdate(clientName,
						connectionPool, existingTableNames, printOptions, executeOptions, updatedTables,
						existingTableNames, physicalNode);
				singleTableUpdates.add(singleTableUpdate);
			}
		}
		FutureTool.submitAndGetAll(singleTableUpdates, drContext.getExecutorService());
		
		sendEmail();
		return null;
	}

	
	private void sendEmail(){
		if(CollectionTool.isEmpty(printedSchemaUpdates)){ return; }
		if(StringTool.isEmpty(drContext.getAdministratorEmail()) || StringTool.isEmpty(drContext.getServerName())){ return; }
		String subject = "SchemaUpdate request from "+drContext.getServerName();
		StringBuilder body = new StringBuilder();
		for(String update : IterableTool.nullSafe(printedSchemaUpdates)){
			body.append(update + "\n\n");
		}
		DataRouterEmailTool.sendEmail("schemaupdate@hotpads.com", drContext.getAdministratorEmail(), subject, 
				body.toString());
	}
}