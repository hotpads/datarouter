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
package io.datarouter.trace.storage;

import java.util.Collection;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.util.PrimaryKeyVacuum;
import io.datarouter.storage.util.PrimaryKeyVacuum.PrimaryKeyVacuumBuilder;
import io.datarouter.trace.storage.entity.BaseTraceEntityKey;
import io.datarouter.trace.storage.entity.TraceEntity;
import io.datarouter.trace.storage.entity.TraceEntityKey;
import io.datarouter.trace.storage.span.TraceSpan;
import io.datarouter.trace.storage.span.TraceSpan.TraceSpanFielder;
import io.datarouter.trace.storage.span.TraceSpanKey;
import io.datarouter.trace.storage.thread.TraceThread;
import io.datarouter.trace.storage.thread.TraceThread.TraceThreadFielder;
import io.datarouter.trace.storage.thread.TraceThreadKey;
import io.datarouter.trace.storage.trace.Trace;
import io.datarouter.trace.storage.trace.Trace.TraceFielder;
import io.datarouter.trace.storage.trace.TraceKey;

@Singleton
public class DatarouterTraceDao extends BaseDao implements BaseDatarouterTraceDao{

	public static class DatarouterTraceDaoParams extends BaseDaoParams{

		public DatarouterTraceDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<TraceKey,Trace> trace;
	private final SortedMapStorage<TraceThreadKey,TraceThread> traceThread;
	private final SortedMapStorage<TraceSpanKey,TraceSpan> traceSpan;

	@Inject
	public DatarouterTraceDao(
			Datarouter datarouter,
			DatarouterTraceDaoParams params,
			NodeFactory nodeFactory){
		super(datarouter);
		trace = nodeFactory.create(
				params.clientId,
				TraceEntityKey::new,
				Trace::new,
				TraceFielder::new)
				.buildAndRegister();
		traceThread = nodeFactory.create(
				params.clientId,
				TraceEntityKey::new,
				TraceThread::new,
				TraceThreadFielder::new)
				.buildAndRegister();
		traceSpan = nodeFactory.create(
				params.clientId,
				TraceEntityKey::new,
				TraceSpan::new,
				TraceSpanFielder::new)
				.buildAndRegister();
	}

	@Override
	public void putMulti(
			Collection<TraceThread> threadDatabeans,
			Collection<TraceSpan> spanDatabeans,
			Trace traceDatabean){
		Config config = new Config()
				.setIgnoreNullFields(true)
				.setPersistentPut(false);
		traceThread.putMulti(threadDatabeans, config);
		traceSpan.putMulti(spanDatabeans, config);
		// insert trace last to fix ui
		trace.put(traceDatabean, config);
	}

	@Override
	public TraceEntity getEntity(TraceEntityKey entityKey){
		return new TraceEntity(
				entityKey,
				trace.get(new TraceKey(entityKey)),
				traceThread.scanWithPrefix(new TraceThreadKey(entityKey)).list(),
				traceSpan.scanWithPrefix(new TraceSpanKey(entityKey)).list());
	}

	public PrimaryKeyVacuum<TraceKey> makeTraceVacuum(){
		Predicate<TraceKey> isExpired = key -> isExpired(key.getEntityKey());
		return new PrimaryKeyVacuumBuilder<>(
				trace.scanKeys().advanceWhile(isExpired),
				$ -> true,
				trace::deleteMulti)
				.build();
	}

	public PrimaryKeyVacuum<TraceSpanKey> makeTraceSpanVacuum(){
		Predicate<TraceSpanKey> isExpired = key -> isExpired(key.getEntityKey());
		return new PrimaryKeyVacuumBuilder<>(
				traceSpan.scanKeys().advanceWhile(isExpired),
				$ -> true,
				traceSpan::deleteMulti)
				.build();
	}

	public PrimaryKeyVacuum<TraceThreadKey> makeTraceThreadVacuum(){
		Predicate<TraceThreadKey> isExpired = key -> isExpired(key.getEntityKey());
		return new PrimaryKeyVacuumBuilder<>(
				traceThread.scanKeys().advanceWhile(isExpired),
				$ -> true,
				traceThread::deleteMulti)
				.build();
	}

	private boolean isExpired(BaseTraceEntityKey<?> entityKey){
		return entityKey.getAge().compareTo(Trace.TTL) > 0;
	}

}
