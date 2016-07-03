package com.hotpads.datarouter.test.sqs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
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

	@Inject
	private Datarouter datarouter;
	@Inject
	private SqsTestRouter router;

	@BeforeClass
	public void beforeClass(){
		print("######### drain existing non-group messages #############");
		drainQueueViaPeek(false, 40);
		print("######### drain existing group messages #############");
		drainQueueViaPeek(true, 40);
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

		print("########### non-group poll ###########");
		loadQueue(router.testDatabean, numDatabeans, putBatchSize);
		drainQueueViaPoll(numDrainThreads);

		print("########### non-group peek/ack ############");
		loadQueue(router.testDatabean, numDatabeans, putBatchSize);
		drainQueueViaPeek(false, numDrainThreads);


		print("########### group peek/ack ############");
		loadQueue(router.groupTestDatabean, numGroupDatabeans, groupPutBatchSize);
		drainQueueViaPeek(true, numDrainThreads);
	}

	/*----------------- load ------------------*/

	private void loadQueue(QueueStorageWriter<TestDatabeanKey,TestDatabean> node, int numDatabeans, int batchSize){
		PhaseTimer timer = new PhaseTimer("putMulti " + node.toString() + " " + numDatabeans);
		for(List<TestDatabean> batch : new BatchingIterable<>(makeDatabeans(numDatabeans), batchSize)){
			node.putMulti(batch, null);
			print("put through {}", DrCollectionTool.getLast(batch));
		}
		timer.add("loaded");
		print(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeans));
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
					if(numDrained.get() % 200 == 0){
						print("drained {}, latest={}", numDrained.get(), databean.getKey());
					}
				}
			});
		}
		exec.shutdown();
		ExecutorServiceTool.awaitTerminationForever(exec);
		timer.add("drained " + numDrained.intValue());
		print(timer.toString() + "@" + timer.getItemsPerSecond(numDrained.intValue()));
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
						List<TestDatabean> databeans = message.getDatabeans();
						numDatabeansDrained.addAndGet(databeans.size());
						router.groupTestDatabean.ack(message.getKey(), null);
						numMessagesDrained.incrementAndGet();
						if(numMessagesDrained.get() % 5 == 0){
						print("groupNode={}, drained {}, latest={}", groupNode, numDatabeansDrained.get(),
								DrCollectionTool.getLast(databeans).getKey());
						}
					}
				}else{
					for(QueueMessage<TestDatabeanKey,TestDatabean> message : router.testDatabean.peekUntilEmpty(
							config)){
						TestDatabean databean = message.getDatabean();
						numDatabeansDrained.incrementAndGet();
						router.testDatabean.ack(message.getKey(), null);
						numMessagesDrained.incrementAndGet();
						if(numMessagesDrained.get() % 200 == 0){
							print("groupNode={}, drained {}, latest={}", groupNode, numDatabeansDrained.get(),
									databean.getKey());
						}
					}
				}
			});
		}
		exec.shutdown();
		ExecutorServiceTool.awaitTerminationForever(exec);
		timer.add("drained " + numMessagesDrained.intValue() + " messages with " + numDatabeansDrained.intValue()
				+ " databeans");
		print(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeansDrained.intValue()));
	}

	/*----------------- helper -------------------*/

	private static void print(String message, Object... params){
		Message messageObject = ParameterizedMessageFactory.INSTANCE.newMessage(message, params);
		System.out.println(messageObject.getFormattedMessage());
	}

}
