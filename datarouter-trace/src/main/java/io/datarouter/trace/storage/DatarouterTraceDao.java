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
import io.datarouter.trace.storage.entity.TraceEntityKey;
import io.datarouter.trace.storage.entity.UiTraceBundleDto;
import io.datarouter.trace.storage.span.TraceSpan;
import io.datarouter.trace.storage.span.TraceSpan.TraceSpanFielder;
import io.datarouter.trace.storage.span.TraceSpanKey;
import io.datarouter.trace.storage.thread.TraceThread;
import io.datarouter.trace.storage.thread.TraceThread.TraceThreadFielder;
import io.datarouter.trace.storage.thread.TraceThreadKey;
import io.datarouter.trace.storage.trace.Trace;
import io.datarouter.trace.storage.trace.Trace.TraceFielder;
import io.datarouter.trace.storage.trace.TraceKey;
import io.datarouter.trace.storage.trace.TraceTool;
import io.datarouter.trace.web.AccessException;

@Singleton
public class DatarouterTraceDao extends BaseDao implements BaseDatarouterTraceDao{

	public static class DatarouterTraceDaoParams extends BaseDaoParams{

		public DatarouterTraceDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<TraceKey,Trace> traceNode;
	private final SortedMapStorage<TraceThreadKey,TraceThread> traceThreadNode;
	private final SortedMapStorage<TraceSpanKey,TraceSpan> traceSpanNode;

	@Inject
	public DatarouterTraceDao(
			Datarouter datarouter,
			DatarouterTraceDaoParams params,
			NodeFactory nodeFactory){
		super(datarouter);
		traceNode = nodeFactory.create(
				params.clientId,
				TraceEntityKey::new,
				Trace::new,
				TraceFielder::new)
				.buildAndRegister();
		traceThreadNode = nodeFactory.create(
				params.clientId,
				TraceEntityKey::new,
				TraceThread::new,
				TraceThreadFielder::new)
				.buildAndRegister();
		traceSpanNode = nodeFactory.create(
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
		traceThreadNode.putMulti(threadDatabeans, config);
		traceSpanNode.putMulti(spanDatabeans, config);
		// insert trace last to fix ui
		traceNode.put(traceDatabean, config);
	}

	@Override
	public UiTraceBundleDto getEntity(String traceId) throws AccessException{
		Trace trace = traceNode.get(new TraceKey(traceId));
		if(trace == null){
			throw TraceTool.makeException(traceId);
		}
		return new UiTraceBundleDto(
				"",
				trace,
				traceThreadNode.scanWithPrefix(new TraceThreadKey(traceId, null)).list(),
				traceSpanNode.scanWithPrefix(new TraceSpanKey(traceId, null, null)).list());
	}

	public PrimaryKeyVacuum<TraceKey> makeTraceVacuum(){
		Predicate<TraceKey> isExpired = key -> isExpired(key.getEntityKey());
		return new PrimaryKeyVacuumBuilder<>(
				traceNode.scanKeys().advanceWhile(isExpired),
				$ -> true,
				traceNode::deleteMulti)
				.build();
	}

	public PrimaryKeyVacuum<TraceSpanKey> makeTraceSpanVacuum(){
		Predicate<TraceSpanKey> isExpired = key -> isExpired(key.getEntityKey());
		return new PrimaryKeyVacuumBuilder<>(
				traceSpanNode.scanKeys().advanceWhile(isExpired),
				$ -> true,
				traceSpanNode::deleteMulti)
				.build();
	}

	public PrimaryKeyVacuum<TraceThreadKey> makeTraceThreadVacuum(){
		Predicate<TraceThreadKey> isExpired = key -> isExpired(key.getEntityKey());
		return new PrimaryKeyVacuumBuilder<>(
				traceThreadNode.scanKeys().advanceWhile(isExpired),
				$ -> true,
				traceThreadNode::deleteMulti)
				.build();
	}

	private boolean isExpired(BaseTraceEntityKey<?> entityKey){
		return entityKey.getAge().compareTo(Trace.TTL) > 0;
	}

}
