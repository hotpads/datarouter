package com.hotpads.datarouter.client.imp.mysql.ddl.execute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.mysql.util.JdbcTool;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.util.DatarouterEmailTool;
import com.hotpads.util.core.concurrent.Lazy;

public class JdbcSchemaUpdateServiceFactory{
	private static final Logger logger = LoggerFactory.getLogger(JdbcSchemaUpdateServiceFactory.class);

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private SingleTableSchemaUpdateFactory singleTableSchemaUpdateFactory;
	@Inject
	@Named(DatarouterExecutorGuiceModule.POOL_schemaUpdateScheduler)
	private ScheduledExecutorService executor;

	public JdbcSchemaUpdateService create(JdbcConnectionPool connectionPool){
		return new JdbcSchemaUpdateService(connectionPool);
	}

	public class JdbcSchemaUpdateService{
		public static final String
				PRINT_PREFIX = "schemaUpdate.print",
				EXECUTE_PREFIX = "schemaUpdate.execute";

		private static final long THROTTLING_DELAY_SECONDS = 10;

		private final JdbcConnectionPool connectionPool;
		private final SchemaUpdateOptions printOptions;
		private final SchemaUpdateOptions executeOptions;
		private final Lazy<List<String>> existingTableNames;
		private final List<String> printedSchemaUpdates;
		private final List<Future<Optional<String>>> futures;

		private JdbcSchemaUpdateService(JdbcConnectionPool connectionPool){
			this.connectionPool = connectionPool;
			this.printOptions = new SchemaUpdateOptions(datarouterProperties.getConfigDirectory(), PRINT_PREFIX, true);
			this.executeOptions = new SchemaUpdateOptions(datarouterProperties.getConfigDirectory(), EXECUTE_PREFIX,
					false);
			this.printedSchemaUpdates = new ArrayList<>();
			this.futures = Collections.synchronizedList(new ArrayList<>());
			this.existingTableNames = Lazy.of(this::fetchExistingTables);
			executor.scheduleWithFixedDelay(this::gatherSchemaUpdates, 0, THROTTLING_DELAY_SECONDS, TimeUnit.SECONDS);
		}

		public Future<Optional<String>> queueNodeForSchemaUpdate(String clientName, PhysicalNode<?,?> node){
			Future<Optional<String>> future = executor.submit(singleTableSchemaUpdateFactory
					.new SingleTableSchemaUpdate(clientName, connectionPool, existingTableNames, printOptions,
							executeOptions, node));
			futures.add(future);
			return future;
		}

		private void gatherSchemaUpdates(){
			gatherSchemaUpdates(false);
		}

		public synchronized void gatherSchemaUpdates(boolean wait){
			boolean shouldNotify = true;
			Iterator<Future<Optional<String>>> futureIterator = futures.iterator();
			while(futureIterator.hasNext()){
				Future<Optional<String>> future = futureIterator.next();
				if(wait || future.isDone()){
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
			if(printedSchemaUpdates.isEmpty()){
				return;
			}
			String subject = "SchemaUpdate request from " + datarouterProperties.getServerName();
			StringBuilder body = new StringBuilder();
			for(String update : printedSchemaUpdates){
				body.append(update + "\n\n");
			}
			DatarouterEmailTool.trySendEmail("noreply@hotpads.com", datarouterProperties.getAdministratorEmail(),
					subject, body.toString());
			printedSchemaUpdates.clear();
		}

		private List<String> fetchExistingTables(){
			Connection connection;
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

}
