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
package io.datarouter.storage.config.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.lazy.Lazy;
import io.datarouter.util.mutable.MutableString;

public abstract class BaseSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(BaseSchemaUpdateService.class);

	private static final long THROTTLING_DELAY_SECONDS = 10;

	private final DatarouterProperties datarouterProperties;
	private final DatarouterAdministratorEmailService adminEmailService;
	private final DatarouterSchemaUpdateScheduler executor;

	private final Map<ClientId,Lazy<List<String>>> existingTableNamesByClient;
	private final List<Future<Optional<SchemaUpdateResult>>> futures;

	public BaseSchemaUpdateService(
			DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService adminEmailService,
			DatarouterSchemaUpdateScheduler executor){
		this.datarouterProperties = datarouterProperties;
		this.adminEmailService = adminEmailService;
		this.executor = executor;

		this.futures = Collections.synchronizedList(new ArrayList<>());
		this.existingTableNamesByClient = new ConcurrentHashMap<>();
		executor.scheduleWithFixedDelay(this::gatherSchemaUpdates, 0, THROTTLING_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	public Future<Optional<SchemaUpdateResult>> queueNodeForSchemaUpdate(ClientId clientId, PhysicalNode<?,?,?> node){
		Lazy<List<String>> existingTableNames = existingTableNamesByClient.computeIfAbsent(clientId,
				this::lazyFetchExistingTables);
		Future<Optional<SchemaUpdateResult>> future = executor.submit(makeSchemaUpdateCallable(clientId,
				existingTableNames, node));
		futures.add(future);
		return future;
	}

	protected abstract Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(ClientId clientId,
			Lazy<List<String>> existingTableNames, PhysicalNode<?,?,?> node);

	private void gatherSchemaUpdates(){
		gatherSchemaUpdates(false);
	}

	public synchronized void gatherSchemaUpdates(boolean wait){
		boolean shouldNotify = true;
		Map<ClientId,List<String>> printedSchemaUpdates = new HashMap<>();
		Iterator<Future<Optional<SchemaUpdateResult>>> futureIterator = futures.iterator();
		MutableString oneStartupBlockReason = new MutableString("");
		while(futureIterator.hasNext()){
			Future<Optional<SchemaUpdateResult>> future = futureIterator.next();
			if(wait || future.isDone()){
				try{
					Optional<SchemaUpdateResult> optional = future.get();
					if(optional.isEmpty()){
						continue;
					}
					printedSchemaUpdates.computeIfAbsent(optional.get().clientId, $ -> new ArrayList<>())
							.add(optional.get().ddl);
					optional.get().startupBlockReason
							.ifPresent(oneStartupBlockReason::set);
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
			sendEmail(printedSchemaUpdates);
		}
		if(!oneStartupBlockReason.getString().isEmpty()){
			logger.error(oneStartupBlockReason.getString());
			throw new RuntimeException(oneStartupBlockReason.getString());
		}
	}

	private void sendEmail(Map<ClientId,List<String>> printedSchemaUpdates){
		if(printedSchemaUpdates.isEmpty()){
			return;
		}
		for(Entry<ClientId,List<String>> clientAndDdls : printedSchemaUpdates.entrySet()){
			String subject = "SchemaUpdate request on " + clientAndDdls.getKey().getName() + " from "
					+ datarouterProperties.getEnvironment();
			StringBuilder body = new StringBuilder();
			for(String update : clientAndDdls.getValue()){
				body.append(update + "\n\n");
			}
			sendEmail(datarouterProperties.getAdministratorEmail(),
					adminEmailService.getAdministratorEmailAddressesCsv(), subject, body.toString());
		}
	}

	protected abstract void sendEmail(String fromEmail, String toEmail, String subject, String body);

	private Lazy<List<String>> lazyFetchExistingTables(ClientId clientId){
		return Lazy.of(() -> fetchExistingTables(clientId));
	}

	protected abstract List<String> fetchExistingTables(ClientId clientId);

}
