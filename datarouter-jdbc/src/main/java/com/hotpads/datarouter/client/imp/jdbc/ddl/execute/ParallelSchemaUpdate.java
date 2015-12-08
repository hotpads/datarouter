package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.util.DatarouterEmailTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.concurrent.FutureTool;

public class ParallelSchemaUpdate
implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(ParallelSchemaUpdate.class);

	/******************* fields **********************/

	private final Datarouter datarouter;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final String clientName;
	private final JdbcConnectionPool connectionPool;

	private final SchemaUpdateOptions printOptions;
	private final SchemaUpdateOptions executeOptions;
	private final Set<String> updatedTables;
	private final List<String> printedSchemaUpdates;

	/***************** construct ***********************/

	public ParallelSchemaUpdate(Datarouter datarouter, JdbcFieldCodecFactory fieldCodecFactory,
			String clientName, JdbcConnectionPool connectionPool, SchemaUpdateOptions printOptions,
			SchemaUpdateOptions executeOptions){
		this.datarouter = datarouter;
		this.fieldCodecFactory = fieldCodecFactory;
		this.clientName = clientName;
		this.connectionPool = connectionPool;
		this.printOptions = printOptions;
		this.executeOptions = executeOptions;
		this.updatedTables = Collections.synchronizedSet(new TreeSet<>());
		this.printedSchemaUpdates = Collections.synchronizedList(new ArrayList<>());
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
		}finally{
			connectionPool.checkIn(connection);// is this how you return it to the pool?
		}

		//run an update for each PhysicalNode
		List<Callable<Void>> singleTableUpdates = new ArrayList<>();
		for(PhysicalNode<?, ?> physicalNode : datarouter.getNodes().getPhysicalNodesForClient(clientName)){
			DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
			if(fieldInfo.getFieldAware()){
				SingleTableSchemaUpdate singleTableUpdate = new SingleTableSchemaUpdate(fieldCodecFactory, clientName,
						connectionPool, existingTableNames, printOptions, executeOptions, updatedTables,
						printedSchemaUpdates, physicalNode);
				singleTableUpdates.add(singleTableUpdate);
			}
		}
		FutureTool.submitAndGetAll(singleTableUpdates, datarouter.getExecutorService());

		sendEmail();
		return null;
	}


	private void sendEmail(){
		if (DrCollectionTool.isEmpty(printedSchemaUpdates)){
			return;
		}
		if(DrStringTool.isEmpty(datarouter.getAdministratorEmail()) || DrStringTool.isEmpty(datarouter
				.getServerName())) {
			//note: this can be caused by not calling datarouter.activate().  need to fix this startup flaw.
			logger.warn("please set your datarouter administratorEmail and serverName");
			return;
		}
		String subject = "SchemaUpdate request from "+datarouter.getServerName();
		StringBuilder body = new StringBuilder();
		for(String update : DrIterableTool.nullSafe(printedSchemaUpdates)){
			body.append(update + "\n\n");
		}
		DatarouterEmailTool.trySendEmail("noreply@hotpads.com", datarouter.getAdministratorEmail(), subject,
				body.toString());
	}

}
