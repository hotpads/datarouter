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

import java.util.List;
import java.util.Optional;

import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.trace.storage.entity.Trace2Bundle;
import io.datarouter.trace.storage.entity.Trace2EntityKey;
import io.datarouter.trace.storage.span.Trace2Span;
import io.datarouter.trace.storage.span.Trace2Span.Trace2SpanFielder;
import io.datarouter.trace.storage.span.Trace2SpanKey;
import io.datarouter.trace.storage.thread.Trace2Thread;
import io.datarouter.trace.storage.thread.Trace2Thread.Trace2ThreadFielder;
import io.datarouter.trace.storage.thread.Trace2ThreadKey;
import io.datarouter.trace.storage.trace.Trace2;
import io.datarouter.trace.storage.trace.Trace2.Trace2Fielder;
import io.datarouter.trace.storage.trace.Trace2Key;

public abstract class BaseTrace2Dao extends BaseDao{

	private static final Config CONFIG = new Config()
			.setIgnoreNullFields(true)
			.setPersistentPut(false);

	private final SortedMapStorageNode<
			Trace2Key,
			Trace2,
			Trace2Fielder> traceNode;
	private final SortedMapStorageNode<
			Trace2ThreadKey,
			Trace2Thread,
			Trace2ThreadFielder> traceThreadNode;
	private final SortedMapStorageNode<
			Trace2SpanKey,
			Trace2Span,
			Trace2SpanFielder> traceSpanNode;

	public BaseTrace2Dao(Optional<String> tableNamePrefix,
			Datarouter datarouter,
			BaseDaoParams params,
			NodeFactory nodeFactory){
		super(datarouter);
		traceNode = nodeFactory.create(
				params.clientId,
				Trace2EntityKey::new,
				Trace2::new,
				Trace2Fielder::new)
				.withTableName(tableNamePrefix.map(prefix -> prefix + Trace2.class.getSimpleName()).orElse(null))
				.buildAndRegister();
		traceThreadNode = nodeFactory.create(
				params.clientId,
				Trace2EntityKey::new,
				Trace2Thread::new,
				Trace2ThreadFielder::new)
				.withTableName(tableNamePrefix.map(prefix -> prefix + Trace2Thread.class.getSimpleName()).orElse(null))
				.buildAndRegister();
		traceSpanNode = nodeFactory.create(
				params.clientId,
				Trace2EntityKey::new,
				Trace2Span::new,
				Trace2SpanFielder::new)
				.withTableName(tableNamePrefix.map(prefix -> prefix + Trace2Span.class.getSimpleName()).orElse(null))
				.buildAndRegister();
	}

	public void putMultiTraceBundles(
			List<Trace2Thread> threadDatabeans,
			List<Trace2Span> spanDatabeans,
			List<Trace2> traceDatabean){
		traceThreadNode.putMulti(threadDatabeans, CONFIG);
		traceSpanNode.putMulti(spanDatabeans, CONFIG);
		// last to reduce the chance of ending up with a partial trace in the UI
		traceNode.putMulti(traceDatabean, CONFIG);
	}

	public Scanner<Trace2> scanWithPrefixAnyDelay(String traceId){
		return traceNode.scanWithPrefix(new Trace2Key(traceId));
	}


	public Optional<Trace2Bundle> getEntity(Traceparent traceparent){
		return traceNode.find(new Trace2Key(traceparent))
				.map(trace -> new Trace2Bundle(
						trace.getServiceName(),
						trace,
						traceThreadNode.scanWithPrefix(new Trace2ThreadKey(traceparent, null)).list(),
						traceSpanNode.scanWithPrefix(new Trace2SpanKey(traceparent, null, null)).list()));
	}

}
