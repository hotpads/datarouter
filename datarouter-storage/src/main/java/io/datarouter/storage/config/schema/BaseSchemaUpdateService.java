/*
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.ClusterSchemaUpdateLock;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.mutable.MutableString;
import io.datarouter.util.singletonsupplier.SingletonSupplier;

public abstract class BaseSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(BaseSchemaUpdateService.class);

	private static final long THROTTLING_DELAY_SECONDS = 10;

	private final DatarouterProperties datarouterProperties;
	private final DatarouterSchemaUpdateScheduler executor;
	private final Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao;
	private final Provider<ChangelogRecorder> changelogRecorder;

	private final String buildId;
	private final Map<ClientId,Supplier<List<String>>> existingTableNamesByClient;
	private final List<Future<Optional<SchemaUpdateResult>>> futures;

	public BaseSchemaUpdateService(
			DatarouterProperties datarouterProperties,
			DatarouterSchemaUpdateScheduler executor,
			Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao,
			Provider<ChangelogRecorder> changelogRecorder,
			String buildId){
		this.datarouterProperties = datarouterProperties;
		this.executor = executor;
		this.schemaUpdateLockDao = schemaUpdateLockDao;
		this.changelogRecorder = changelogRecorder;
		this.buildId = buildId;

		this.futures = Collections.synchronizedList(new ArrayList<>());
		this.existingTableNamesByClient = new ConcurrentHashMap<>();
		executor.scheduleWithFixedDelay(this::gatherSchemaUpdates, 0, THROTTLING_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	public Future<Optional<SchemaUpdateResult>> queueNodeForSchemaUpdate(ClientId clientId, PhysicalNode<?,?,?> node){
		Supplier<List<String>> existingTableNames = existingTableNamesByClient.computeIfAbsent(clientId,
				this::lazyFetchExistingTables);
		Future<Optional<SchemaUpdateResult>> future = executor.submit(makeSchemaUpdateCallable(clientId,
				existingTableNames, node));
		futures.add(future);
		return future;
	}

	protected abstract Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node);

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

		if(shouldNotify && acquireSchemaUpdateLock(printedSchemaUpdates)){
			sendEmail(printedSchemaUpdates, !oneStartupBlockReason.getString().isEmpty());
			recordChangelog(printedSchemaUpdates);
		}
		if(!oneStartupBlockReason.getString().isEmpty()){
			logger.error(oneStartupBlockReason.getString());
			throw new RuntimeException(oneStartupBlockReason.getString());
		}
	}

	private void sendEmail(Map<ClientId,List<String>> printedSchemaUpdates, boolean isBlocking){
		if(printedSchemaUpdates.isEmpty()){
			return;
		}
		printedSchemaUpdates.forEach((clientId, ddlList) -> {
			String blocking = isBlocking ? " - Blocking " : " ";
			String subject = "SchemaUpdate Request" + blocking + "on " + clientId.getName() + " from "
					+ datarouterProperties.getEnvironment();
			StringBuilder allStatements = new StringBuilder();
			ddlList.forEach(ddl -> allStatements.append(ddl).append("\n\n"));
			logger.warn("Sending schema update email for client={}", clientId.getName());
			sendEmail(
					subject,
					allStatements.toString());
		});
	}

	protected abstract void sendEmail(String subject, String body);

	private Supplier<List<String>> lazyFetchExistingTables(ClientId clientId){
		return SingletonSupplier.of(() -> fetchExistingTables(clientId));
	}

	protected abstract List<String> fetchExistingTables(ClientId clientId);

	private boolean acquireSchemaUpdateLock(Map<ClientId,List<String>> printedSchemaUpdates){
		if(printedSchemaUpdates.isEmpty()){
			return false;
		}
		String statement = printedSchemaUpdates.entrySet().stream()
				.findFirst()
				.map(entry -> String.join("\n\n", entry.getValue()))
				.get();
		Instant now = Instant.now();
		Integer build = Optional.ofNullable(buildId)
				.filter(buildId -> !"${env.BUILD_NUMBER}".equals(buildId))
				.map(Integer::valueOf)
				.orElseGet(() -> (int)now.getEpochSecond());
		ClusterSchemaUpdateLock lock = new ClusterSchemaUpdateLock(
				build,
				statement,
				datarouterProperties.getServerName(),
				now);
		try{
			schemaUpdateLockDao.get().putAndAcquire(lock);
			logger.warn("Acquired schema update lock for hash={}", lock.getKey().getStatementHash());
			return true;
		}catch(Exception ex){
			logger.warn("Didn't acquire schema update lock for hash={}", lock.getKey().getStatementHash());
			return false;
		}
	}

	private void recordChangelog(Map<ClientId,List<String>> printedSchemaUpdates){
		printedSchemaUpdates.forEach((clientId, ddlList) -> {
			StringBuilder allStatements = new StringBuilder();
			ddlList.forEach(ddl -> allStatements.append(ddl).append("\n\n"));
			var dto = new DatarouterChangelogDtoBuilder(
					"SchemaUpdate",
					"clientId: " + clientId.getName(),
					"SchemaUpdate request",
					datarouterProperties.getAdministratorEmail())
					.withComment(allStatements.toString())
					.build();
				changelogRecorder.get().record(dto);
		});
	}

}
