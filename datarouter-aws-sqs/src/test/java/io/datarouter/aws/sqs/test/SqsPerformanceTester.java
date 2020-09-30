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
package io.datarouter.aws.sqs.test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.aws.sqs.DatarouterAwsSqsTestNgModuleFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.timer.PhaseTimer;

@Guice(moduleFactory = DatarouterAwsSqsTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class SqsPerformanceTester{
	private static final Logger logger = LoggerFactory.getLogger(SqsPerformanceTester.class);

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterSqsTestDao singleDao;
	@Inject
	private DatarouterSqsGroupTestDao groupDao;

	@BeforeClass
	public void beforeClass(){
		logger.info("######### drain existing non-group messages #############");
		drainQueueViaPeek(singleDao, 40);
		logger.info("######### drain existing group messages #############");
		drainGroupQueueViaPeek(groupDao, 40);
	}

	@AfterClass
	public void afterClas(){
		datarouter.shutdown();
	}

	@Test
	public void testLoadAndDrain(){
		final int numDatabeans = 1000;
		final int putBatchSize = 200;
		final int numGroupDatabeans = 100_000;
		final int groupPutBatchSize = 20_000;
		final int numDrainThreads = 20;

		logger.info("########### non-group poll ###########");
		loadQueue(singleDao, numDatabeans, putBatchSize);
		drainQueueViaPoll(singleDao, numDrainThreads);

		logger.info("########### non-group peek/ack ############");
		loadQueue(singleDao, numDatabeans, putBatchSize);
		drainQueueViaPeek(singleDao, numDrainThreads);

		logger.info("########### group peek/ack ############");
		loadQueue(groupDao, numGroupDatabeans, groupPutBatchSize);
		drainGroupQueueViaPeek(groupDao, numDrainThreads);
	}

	/*----------------- load ------------------*/

	private void loadQueue(SqsTestHelperDao dao, int numDatabeans, int batchSize){
		var timer = new PhaseTimer("putMulti " + numDatabeans);
		for(List<TestDatabean> batch : Scanner.of(makeDatabeans(numDatabeans)).batch(batchSize).iterable()){
			dao.putMulti(batch);
			logger.info("put through {}", ListTool.getLast(batch));
		}
		timer.add("loaded");
		logger.info(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeans));
	}

	private List<TestDatabean> makeDatabeans(int numDatabeans){
		List<TestDatabean> databeans = new ArrayList<>();
		for(int i = 1; i <= numDatabeans; ++i){
			databeans.add(new TestDatabean(System.currentTimeMillis() + "_" + i, "asdf", "qwerty"));
		}
		return databeans;
	}

	/*----------------- drain ------------------*/

	// timeout, otherwise it will poll forever
	private void drainQueueViaPoll(DatarouterSqsTestDao dao, int numThreads){
		var timer = new PhaseTimer("drain queue");
		var numDrained = new AtomicLong(0L);
		ExecutorService exec = Executors.newCachedThreadPool();
		for(int i = 0; i < numThreads; ++i){
			exec.submit(() -> {
				for(TestDatabean databean : dao.pollUntilEmpty(Duration.ofSeconds(3)).iterable()){
					numDrained.incrementAndGet();
					if(numDrained.get() % 200 == 0){
						logger.info("drained {}, latest={}", numDrained.get(), databean.getKey());
					}
				}
			});
		}
		ExecutorServiceTool.shutdown(exec, Duration.ofDays(1));
		timer.add("drained " + numDrained.intValue());
		logger.info(timer.toString() + "@" + timer.getItemsPerSecond(numDrained.intValue()));
	}

	// timeout is needed, otherwise it will poll forever
	private void drainGroupQueueViaPeek(DatarouterSqsGroupTestDao dao, int numThreads){
		var timer = new PhaseTimer("drain queue");
		var numDatabeansDrained = new AtomicLong(0L);
		var numMessagesDrained = new AtomicLong(0L);
		ExecutorService exec = Executors.newCachedThreadPool();
		for(int i = 0; i < numThreads; ++i){
			exec.submit(() -> {
				for(GroupQueueMessage<TestDatabeanKey,TestDatabean> message : dao.peekUntilEmpty(Duration.ofSeconds(3))
						.iterable()){
					List<TestDatabean> databeans = message.getDatabeans();
					numDatabeansDrained.addAndGet(databeans.size());
					dao.ack(message.getKey());
					numMessagesDrained.incrementAndGet();
					if(numMessagesDrained.get() % 5 == 0){
					logger.info("groupNode drained {}, latest={}", numDatabeansDrained.get(),
							ListTool.getLast(databeans).getKey());
					}
				}
			});
		}
		ExecutorServiceTool.shutdown(exec, Duration.ofDays(1));
		timer.add("drained " + numMessagesDrained.intValue() + " messages with " + numDatabeansDrained.intValue()
				+ " databeans");
		logger.info(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeansDrained.intValue()));
	}

	// timeout, otherwise it will poll forever
	private void drainQueueViaPeek(DatarouterSqsTestDao dao, int numThreads){
		var timer = new PhaseTimer("drain queue");
		var numDatabeansDrained = new AtomicLong(0L);
		var numMessagesDrained = new AtomicLong(0L);
		ExecutorService exec = Executors.newCachedThreadPool();
		for(int i = 0; i < numThreads; ++i){
			exec.submit(() -> {
				for(QueueMessage<TestDatabeanKey,TestDatabean> message : dao.peekUntilEmpty(Duration.ofSeconds(3))
						.iterable()){
					TestDatabean databean = message.getDatabean();
					numDatabeansDrained.incrementAndGet();
					dao.ack(message.getKey());
					numMessagesDrained.incrementAndGet();
					if(numMessagesDrained.get() % 200 == 0){
						logger.info("queue drained {}, latest={}", numDatabeansDrained.get(),
								databean.getKey());
					}
				}
			});
		}
		ExecutorServiceTool.shutdown(exec, Duration.ofDays(1));
		timer.add("drained " + numMessagesDrained.intValue() + " messages with " + numDatabeansDrained.intValue()
				+ " databeans");
		logger.info(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeansDrained.intValue()));
	}

}
