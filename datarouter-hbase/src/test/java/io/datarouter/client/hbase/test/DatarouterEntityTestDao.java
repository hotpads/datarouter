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
package io.datarouter.client.hbase.test;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.hbase.test.TestEntity.TestTrace;
import io.datarouter.client.hbase.test.TestEntity.TestTrace.TestTraceFielder;
import io.datarouter.client.hbase.test.TestEntity.TestTraceEntity;
import io.datarouter.client.hbase.test.TestEntity.TestTraceEntityKey;
import io.datarouter.client.hbase.test.TestEntity.TestTraceEntityPartitioner;
import io.datarouter.client.hbase.test.TestEntity.TestTraceKey;
import io.datarouter.client.hbase.test.TestEntity.TestTraceSpan;
import io.datarouter.client.hbase.test.TestEntity.TestTraceSpan.TestTraceSpanFielder;
import io.datarouter.client.hbase.test.TestEntity.TestTraceSpanKey;
import io.datarouter.client.hbase.test.TestEntity.TestTraceThread;
import io.datarouter.client.hbase.test.TestEntity.TestTraceThread.TestTraceThreadFielder;
import io.datarouter.client.hbase.test.TestEntity.TestTraceThreadKey;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.SubEntitySortedMapStorageNode;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;

@Singleton
public class DatarouterEntityTestDao extends BaseDao implements TestDao{

	private static final EntityNodeParams<TestTraceEntityKey,TestTraceEntity> PARAMS = new EntityNodeParams<>(
			"TraceEntity16_Test",
			TestTraceEntityKey::new,
			TestTraceEntity::new,
			TestTraceEntityPartitioner::new,
			"TraceEntity16_Test");

	private final EntityNode<TestTraceEntityKey,TestTraceEntity> entity;
	private final SubEntitySortedMapStorageNode<TestTraceEntityKey,TestTraceKey,TestTrace,TestTraceFielder> trace;
	private final SubEntitySortedMapStorageNode<
			TestTraceEntityKey,
			TestTraceThreadKey,
			TestTraceThread,
			TestTraceThreadFielder>
			traceThread;
	private final SubEntitySortedMapStorageNode<
			TestTraceEntityKey,
			TestTraceSpanKey,
			TestTraceSpan,
			TestTraceSpanFielder> traceSpan;

	@Inject
	public DatarouterEntityTestDao(
			Datarouter datarouter,
			EntityNodeFactory entityNodeFactory,
			WideNodeFactory wideNodeFactory,
			ClientId clientId){
		super(datarouter);

		entity = entityNodeFactory.create(clientId, PARAMS);

		trace = datarouter.register(wideNodeFactory.subEntityNode(
				PARAMS,
				clientId,
				TestTrace::new,
				TestTraceFielder::new,
				TestTraceEntity.QUALIFIER_PREFIX_Trace));
		entity.register(trace);

		traceThread = datarouter.register(wideNodeFactory.subEntityNode(
				PARAMS,
				clientId,
				TestTraceThread::new,
				TestTraceThreadFielder::new,
				TestTraceEntity.QUALIFIER_PREFIX_TraceThread));
		entity.register(traceThread);

		traceSpan = datarouter.register(wideNodeFactory.subEntityNode(
				PARAMS,
				clientId,
				TestTraceSpan::new,
				TestTraceSpanFielder::new,
				TestTraceEntity.QUALIFIER_PREFIX_TraceSpan));
		entity.register(traceSpan);
	}

	public void put(TestTrace databean){
		trace.put(databean);
	}

	public void putMulti(
			Collection<TestTrace> traces,
			Collection<TestTraceThread> threads,
			Collection<TestTraceSpan> spans){
		trace.putMulti(traces);
		traceThread.putMulti(threads);
		traceSpan.putMulti(spans);
	}

	public TestTraceEntity getEntity(TestTraceEntityKey key){
		return entity.getEntity(key);
	}

}
