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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.SchemaUpdateResult;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.DatarouterEmailService;
import io.datarouter.util.lazy.Lazy;
import io.datarouter.util.mutable.MutableString;

@Singleton
public class MysqlSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(MysqlSchemaUpdateService.class);

	private static final long THROTTLING_DELAY_SECONDS = 10;

	private final DatarouterProperties datarouterProperties;
	private final SingleTableSchemaUpdateFactory singleTableSchemaUpdateFactory;
	private final DatarouterSchemaUpdateScheduler executor;
	private final DatarouterEmailService datarouterEmailService;
	private final MysqlConnectionPoolHolder mysqlConnectionPoolHolder;

	private final Map<ClientId,Lazy<List<String>>> existingTableNamesByClient;
	private final List<String> printedSchemaUpdates;
	private final List<Future<Optional<SchemaUpdateResult>>> futures;

	@Inject
	public MysqlSchemaUpdateService(DatarouterProperties datarouterProperties,
			SingleTableSchemaUpdateFactory singleTableSchemaUpdateFactory, DatarouterSchemaUpdateScheduler executor,
			DatarouterEmailService datarouterEmailService, MysqlConnectionPoolHolder mysqlConnectionPoolHolder){
		this.datarouterProperties = datarouterProperties;
		this.singleTableSchemaUpdateFactory = singleTableSchemaUpdateFactory;
		this.executor = executor;
		this.datarouterEmailService = datarouterEmailService;
		this.mysqlConnectionPoolHolder = mysqlConnectionPoolHolder;

		this.printedSchemaUpdates = new ArrayList<>();
		this.futures = Collections.synchronizedList(new ArrayList<>());
		this.existingTableNamesByClient = new ConcurrentHashMap<>();
		executor.scheduleWithFixedDelay(this::gatherSchemaUpdates, 0, THROTTLING_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	public Future<Optional<SchemaUpdateResult>> queueNodeForSchemaUpdate(ClientId clientId, PhysicalNode<?,?,?> node){
		Lazy<List<String>> existingTableNames = existingTableNamesByClient.computeIfAbsent(clientId,
				this::fetchExistingTables);
		Future<Optional<SchemaUpdateResult>> future = executor.submit(
				singleTableSchemaUpdateFactory.new SingleTableSchemaUpdate(clientId, existingTableNames, node));
		futures.add(future);
		return future;
	}

	private void gatherSchemaUpdates(){
		gatherSchemaUpdates(false);
	}

	public synchronized void gatherSchemaUpdates(boolean wait){
		boolean shouldNotify = true;
		Iterator<Future<Optional<SchemaUpdateResult>>> futureIterator = futures.iterator();
		MutableString oneErrorMessage = new MutableString("");
		while(futureIterator.hasNext()){
			Future<Optional<SchemaUpdateResult>> future = futureIterator.next();
			if(wait || future.isDone()){
				try{
					Optional<SchemaUpdateResult> optional = future.get();
					if(!optional.isPresent()){
						continue;
					}
					printedSchemaUpdates.add(optional.get().ddl);
					optional.get().errorMessage
						.ifPresent(oneErrorMessage::set);
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
		if(!oneErrorMessage.getString().isEmpty()){
			logger.error(oneErrorMessage.getString());
			throw new RuntimeException(oneErrorMessage.getString());
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

	private Lazy<List<String>> fetchExistingTables(ClientId clientId){
		return Lazy.of(() -> {
			try(Connection connection = mysqlConnectionPoolHolder.getConnectionPool(clientId).checkOut()){
				return MysqlTool.showTables(connection, mysqlConnectionPoolHolder.getConnectionPool(clientId)
						.getSchemaName());
			}catch(SQLException e){
				throw new RuntimeException(e);
			}
		});
	}

}
