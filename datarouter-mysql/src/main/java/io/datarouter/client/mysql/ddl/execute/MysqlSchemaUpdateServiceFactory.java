/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.mysql.ddl.execute;

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

import io.datarouter.client.mysql.connection.MysqlConnectionPoolFactory.MysqlConnectionPool;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.guice.DatarouterStorageExecutorGuiceModule;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.DatarouterEmailService;
import io.datarouter.util.lazy.Lazy;

public class MysqlSchemaUpdateServiceFactory{
	private static final Logger logger = LoggerFactory.getLogger(MysqlSchemaUpdateServiceFactory.class);

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private SingleTableSchemaUpdateFactory singleTableSchemaUpdateFactory;
	@Inject
	@Named(DatarouterStorageExecutorGuiceModule.POOL_schemaUpdateScheduler)
	private ScheduledExecutorService executor;
	@Inject
	private DatarouterEmailService datarouterEmailService;

	public MysqlSchemaUpdateService create(MysqlConnectionPool connectionPool){
		return new MysqlSchemaUpdateService(connectionPool);
	}

	public class MysqlSchemaUpdateService{

		private static final long THROTTLING_DELAY_SECONDS = 10;

		private final MysqlConnectionPool connectionPool;
		private final Lazy<List<String>> existingTableNames;
		private final List<String> printedSchemaUpdates;
		private final List<Future<Optional<String>>> futures;

		private MysqlSchemaUpdateService(MysqlConnectionPool connectionPool){
			this.connectionPool = connectionPool;
			this.printedSchemaUpdates = new ArrayList<>();
			this.futures = Collections.synchronizedList(new ArrayList<>());
			this.existingTableNames = Lazy.of(this::fetchExistingTables);
			executor.scheduleWithFixedDelay(this::gatherSchemaUpdates, 0, THROTTLING_DELAY_SECONDS, TimeUnit.SECONDS);
		}

		public Future<Optional<String>> queueNodeForSchemaUpdate(String clientName, PhysicalNode<?,?,?> node){
			Future<Optional<String>> future = executor.submit(singleTableSchemaUpdateFactory
					.new SingleTableSchemaUpdate(clientName, connectionPool, existingTableNames, node));
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
			datarouterEmailService.trySendEmail(datarouterProperties.getAdministratorEmail(), datarouterProperties
					.getAdministratorEmail(), subject, body.toString());
			printedSchemaUpdates.clear();
		}

		private List<String> fetchExistingTables(){
			try(Connection connection = connectionPool.checkOut()){
				return MysqlTool.showTables(connection, connectionPool.getSchemaName());
			}catch(SQLException e){
				throw new RuntimeException(e);
			}
		}

	}

}
