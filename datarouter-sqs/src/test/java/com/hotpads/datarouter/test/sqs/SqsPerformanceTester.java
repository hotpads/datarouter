package com.hotpads.datarouter.test.sqs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.sqs.config.DatarouterSqsTestModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.concurrent.ExecutorServiceTool;
import com.hotpads.util.core.iterable.BatchingIterable;
import com.hotpads.util.core.profile.PhaseTimer;

@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
@Test(singleThreaded = true)
public class SqsPerformanceTester{
	private static final Logger logger = LoggerFactory.getLogger(SqsPerformanceTester.class);

	@Inject
	private Datarouter datarouter;
	@Inject
	private SqsTestRouter router;

	@BeforeClass
	public void beforeClass(){
		logger.warn("######### drain existing non-group messages #############");
		drainQueueViaPeek(false, 40);
		logger.warn("######### drain existing group messages #############");
		drainQueueViaPeek(true, 40);
	}

	@AfterClass
	public void afterClas(){
		datarouter.shutdown();
	}

	@Test
	public void testLoadAndDrain(){
		logger.warn("########### non-group poll ###########");
		loadQueue(router.testDatabean, 1000);
		drainQueueViaPoll(40);

		logger.warn("########### non-group via peek/ack ############");
		loadQueue(router.testDatabean, 1000);
		drainQueueViaPeek(false, 40);

		logger.warn("########### group via peek/ack ############");
		loadQueue(router.groupTestDatabean, 1000);
		drainQueueViaPeek(true, 40);
	}

	/*----------------- load ------------------*/

	private void loadQueue(QueueStorageWriter<TestDatabeanKey,TestDatabean> node, int numDatabeans){
		PhaseTimer timer = new PhaseTimer("putMulti " + node.toString() + " " + numDatabeans);
		for(List<TestDatabean> batch : new BatchingIterable<>(makeDatabeans(numDatabeans), 100)){
			node.putMulti(batch, null);
			logger.warn("put through {}", DrCollectionTool.getLast(batch));
		}
		timer.add("loaded");
		logger.warn(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeans));
	}

	private List<TestDatabean> makeDatabeans(int numDatabeans){
		List<TestDatabean> databeans = new ArrayList<>();
		for(int i = 1; i <= numDatabeans; ++i){
			databeans.add(new TestDatabean(System.currentTimeMillis() + "_" + i, "asdf", "qwerty"));
		}
		return databeans;
	}

	/*----------------- drain ------------------*/

	private void drainQueueViaPoll(int numThreads){
		PhaseTimer timer = new PhaseTimer("drain queue");
		Config config = new Config().setTimeout(3, TimeUnit.SECONDS);// timeout, otherwise it will poll forever
		AtomicLong numDrained = new AtomicLong(0L);
		ExecutorService exec = Executors.newCachedThreadPool();
		for(int i = 0; i < numThreads; ++i){
			exec.submit(() -> {
				for(TestDatabean databean : router.testDatabean.pollUntilEmpty(config)){
					numDrained.incrementAndGet();
					if(numDrained.get() % 100 == 0){
						logger.warn("drained {}, latest={}", numDrained.get(), databean.getKey());

					}
				}
			});
		}
		exec.shutdown();
		ExecutorServiceTool.awaitTerminationForever(exec);
		timer.add("drained " + numDrained.intValue());
		logger.warn(timer.toString() + "@" + timer.getItemsPerSecond(numDrained.intValue()));
	}

	private void drainQueueViaPeek(boolean groupNode, int numThreads){
		PhaseTimer timer = new PhaseTimer("drain queue");
		Config config = new Config().setTimeout(3, TimeUnit.SECONDS);// timeout, otherwise it will poll forever
		AtomicLong numDatabeansDrained = new AtomicLong(0L);
		AtomicLong numMessagesDrained = new AtomicLong(0L);
		ExecutorService exec = Executors.newCachedThreadPool();
		for(int i = 0; i < numThreads; ++i){
			exec.submit(() -> {
				if(groupNode){
					for(GroupQueueMessage<TestDatabeanKey,TestDatabean> message : router.groupTestDatabean
							.peekUntilEmpty(config)){
						for(TestDatabean databean : message.getDatabeans()){
							numDatabeansDrained.incrementAndGet();
							if(numDatabeansDrained.get() % 100 == 0){
								logger.warn("groupNode={}, drained {}, latest={}", groupNode, numDatabeansDrained.get(),
										databean.getKey());

							}
						}
						router.groupTestDatabean.ack(message.getKey(), null);
						numMessagesDrained.incrementAndGet();
					}
				}else{
					for(QueueMessage<TestDatabeanKey,TestDatabean> message : router.testDatabean.peekUntilEmpty(
							config)){
						TestDatabean databean = message.getDatabean();
						numDatabeansDrained.incrementAndGet();
						if(numDatabeansDrained.get() % 100 == 0){
							logger.warn("groupNode={}, drained {}, latest={}", groupNode, numDatabeansDrained.get(),
									databean.getKey());
						}
						// ack after counting for consistency with group version
						router.testDatabean.ack(message.getKey(), null);
						numMessagesDrained.incrementAndGet();
					}
				}
			});
		}
		exec.shutdown();
		ExecutorServiceTool.awaitTerminationForever(exec);
		timer.add("drained " + numMessagesDrained.intValue() + " messages with " + numDatabeansDrained.intValue()
				+ " databeans");
		logger.warn(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeansDrained.intValue()));
	}

}
