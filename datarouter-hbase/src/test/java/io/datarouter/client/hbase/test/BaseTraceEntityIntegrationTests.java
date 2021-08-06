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
package io.datarouter.client.hbase.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.test.TestEntity.TestTrace;
import io.datarouter.client.hbase.test.TestEntity.TestTraceEntityKey;
import io.datarouter.client.hbase.test.TestEntity.TestTraceSpan;
import io.datarouter.client.hbase.test.TestEntity.TestTraceThread;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.util.UlidTool;
import io.datarouter.util.number.RandomTool;

public abstract class BaseTraceEntityIntegrationTests{

	private static final List<TestTrace> traces = new ArrayList<>();
	private static final List<TestTraceThread> threads = new ArrayList<>();
	private static final List<TestTraceSpan> spans = new ArrayList<>();
	private static final String id;

	static{
		id = UlidTool.nextUlid();

		TestTrace trace1 = new TestTrace(id);
		traces.add(trace1);

		// only one thread should have hasParent=false. gets id 0
		TestTraceThread thread1a = new TestTraceThread(trace1.getTraceId(), 0L);
		threads.add(thread1a);
		Assert.assertEquals(thread1a.getKey().getId(), trace1.getTraceId());
		TestTraceSpan span1a1 = new TestTraceSpan(thread1a.getKey().getId(), thread1a.getThreadId(), 1, 1);
		spans.add(span1a1);
		TestTraceSpan span1a2 = new TestTraceSpan(thread1a.getKey().getId(), thread1a.getThreadId(), 2, 1);
		spans.add(span1a2);

		// only one thread should have hasParent=true. gets id random
		TestTraceThread thread1b = new TestTraceThread(trace1.getTraceId(), RandomTool.nextPositiveLong());
		threads.add(thread1b);
		Assert.assertEquals(thread1b.getTraceId(), trace1.getTraceId());
		TestTraceSpan span1b1 = new TestTraceSpan(thread1b.getTraceId(), thread1b.getThreadId(), 1, 1);
		spans.add(span1b1);
		TestTraceSpan span1b2 = new TestTraceSpan(thread1b.getTraceId(), thread1b.getThreadId(), 2, 1);
		spans.add(span1b2);
		TestTraceSpan span1b3 = new TestTraceSpan(thread1b.getTraceId(), thread1b.getThreadId(), 3, 1);
		spans.add(span1b3);
		Assert.assertEquals(span1b3.getTraceId(), trace1.getTraceId());

		for(TestTrace trace : traces){
			trace.setContext("blah");
			trace.setDuration(123L);
			trace.setParams("paramA=a&paramB=b");
			trace.setType("mighty");
		}

		for(TestTraceThread thread : threads){
			thread.setInfo("the info");
			thread.setName("my name");
			thread.setParentId(2222L);
			thread.setQueuedDuration(111L);
			thread.setRunningDuration(33333L);
			thread.setServerId("el server");
		}

		for(TestTraceSpan span : spans){
			span.setDuration(321L);
			span.setInfo("the info is lost");
			span.setName("phillip");
		}
	}

	private final DatarouterEntityTestDao dao;

	public BaseTraceEntityIntegrationTests(
			Datarouter datarouter,
			EntityNodeFactory entityNodeFactory,
			WideNodeFactory wideNodeFactory,
			ClientId clientId){
		dao = new DatarouterEntityTestDao(datarouter, entityNodeFactory, wideNodeFactory, clientId);
	}

	@Test
	public void testSimple(){
		String idSimple = UlidTool.nextUlid();
		TestTrace trace = new TestTrace(idSimple, System.currentTimeMillis());
		dao.put(trace);
		TestTrace roundTrippedTrace = dao.getEntity(new TestTraceEntityKey(idSimple)).getTrace();
		Assert.assertEquals(roundTrippedTrace, trace);
	}

	@Test
	public void testRoundTripped(){
		dao.putMulti(traces, threads, spans);
		TestTraceEntityKey ek = new TestTraceEntityKey(id);

		TestTrace roundTrippedTrace = dao.getEntity(ek).getTrace();
		Assert.assertEquals(roundTrippedTrace, traces.get(0));

		List<TestTraceSpan> roundTrippedSpans = dao.getEntity(ek).getTraceSpans();
		Assert.assertEquals(roundTrippedSpans.size(), spans.size());
		Assert.assertEquals(roundTrippedSpans, spans);

		List<TestTraceThread> roundTrippedThreads = dao.getEntity(ek).getTraceThreads();
		Assert.assertEquals(roundTrippedThreads.size(), threads.size());
		Assert.assertEquals(roundTrippedThreads, threads);
	}

}
