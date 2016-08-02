package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.DatarouterEmailTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.concurrent.Lazy;

public class JdbcSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(JdbcSchemaUpdateService.class);

	@Singleton
	public static class JdbcSchemaUpdateServiceFactory{

		@Inject
		private Datarouter datarouter;
		@Inject
		private JdbcFieldCodecFactory fieldCodecFactory;
		@Inject
		@Named(DatarouterExecutorGuiceModule.POOL_schemaUpdateScheduler)
		private ScheduledExecutorService executor;

		public JdbcSchemaUpdateService create(JdbcConnectionPool connectionPool){
			return new JdbcSchemaUpdateService(datarouter, fieldCodecFactory, executor, connectionPool);
		}

	}


	public static final String
			PRINT_PREFIX = "schemaUpdate.print",
			EXECUTE_PREFIX = "schemaUpdate.execute";

	private static final long THROTTLING_DELAY_SECONDS = 10;

	private final Datarouter datarouter;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final JdbcConnectionPool connectionPool;
	private final SchemaUpdateOptions printOptions;
	private final SchemaUpdateOptions executeOptions;
	private final Lazy<List<String>> existingTableNames;
	private final List<String> printedSchemaUpdates;
	private final List<Future<Optional<String>>> futures;

	private JdbcSchemaUpdateService(Datarouter datarouter, JdbcFieldCodecFactory fieldCodecFactory,
			ScheduledExecutorService executor, JdbcConnectionPool connectionPool){
		this.datarouter = datarouter;
		this.fieldCodecFactory = fieldCodecFactory;
		this.connectionPool = connectionPool;
		List<Properties> multiProperties = DrPropertiesTool.fromFiles(datarouter.getConfigFilePaths());
		this.printOptions = new SchemaUpdateOptions(multiProperties , PRINT_PREFIX, true);
		this.executeOptions = new SchemaUpdateOptions(multiProperties, EXECUTE_PREFIX, false);
		this.printedSchemaUpdates = new ArrayList<>();
		this.futures = Collections.synchronizedList(new ArrayList<>());
		this.existingTableNames = Lazy.of(this::fetchExistingTables);
		executor.scheduleWithFixedDelay(this::gatherSchemaUpdates, 0, THROTTLING_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	public void queueNodeForSchemaUpdate(String clientName, PhysicalNode<?,?> node){
		futures.add(datarouter.getExecutorService().submit(new SingleTableSchemaUpdate(fieldCodecFactory, clientName,
				connectionPool, existingTableNames.get(), printOptions, executeOptions, node)));
	}

	private void gatherSchemaUpdates(){
		boolean shouldNotify = !printedSchemaUpdates.isEmpty();
		Iterator<Future<Optional<String>>> futureIterator = futures.iterator();
		while(futureIterator.hasNext()){
			Future<Optional<String>> future = futureIterator.next();
			if(future.isDone()){
				try{
					future.get().ifPresent(printedSchemaUpdates::add);
				}catch(InterruptedException | ExecutionException e){
					logger.error("", e);
					throw new RuntimeException(e);
				}
				futureIterator.remove();
			}else{
				shouldNotify = false;
			}
		}
		if(shouldNotify){
			sendEmail();
		}
	}

	private void sendEmail(){
		if (printedSchemaUpdates.isEmpty()){
			return;
		}
		String subject = "SchemaUpdate request from "+datarouter.getServerName();
		StringBuilder body = new StringBuilder();
		for(String update : printedSchemaUpdates){
			body.append(update + "\n\n");
		}
		DatarouterEmailTool.trySendEmail("noreply@hotpads.com", datarouter.getAdministratorEmail(), subject,
				body.toString());
		printedSchemaUpdates.clear();
	}

	private List<String> fetchExistingTables(){
		Connection connection = null;
		try{
			connection = connectionPool.checkOut();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
		try{
			return JdbcTool.showTables(connection);
		}finally{
			connectionPool.checkIn(connection);
		}
	}

}
