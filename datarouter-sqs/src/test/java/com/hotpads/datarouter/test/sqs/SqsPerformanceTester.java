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
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.sqs.config.DatarouterSqsTestModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.concurrent.ExecutorServiceTool;
import com.hotpads.util.core.iterable.BatchingIterable;
import com.hotpads.util.core.profile.PhaseTimer;

@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
@Test(singleThreaded=true)
public class SqsPerformanceTester{
	private static final Logger logger = LoggerFactory.getLogger(SqsPerformanceTester.class);

	@Inject
	private Datarouter datarouter;
	@Inject
	private SqsTestRouter router;

	@AfterClass
	public void shutdown(){
		datarouter.shutdown();
	}

	@Test
	public void testLoadAndDrain(){
		loadQueue();
		drainQueue();
	}

	private void loadQueue(){
		final int numDatabeans = 1000;
		List<TestDatabean> databeans = new ArrayList<>();
		for(int i = 1; i <= numDatabeans; ++i){
			databeans.add(new TestDatabean(System.currentTimeMillis() + "_" + i, "asdf", "qwerty"));
		}
		PhaseTimer timer = new PhaseTimer("putMulti " + numDatabeans);
		for(List<TestDatabean> batch : new BatchingIterable<>(databeans, 100)){
			router.testDatabean.putMulti(batch, null);
			logger.warn("put through {}", DrCollectionTool.getLast(batch));
		}
		logger.warn(timer.toString() + "@" + timer.getItemsPerSecond(numDatabeans));
	}

	private void drainQueue(){
		PhaseTimer timer = new PhaseTimer("drain queue");
		Config config = new Config().setTimeout(1, TimeUnit.SECONDS);//timeout, otherwise it will poll forever
		AtomicLong numDrained = new AtomicLong(0L);
		ExecutorService exec = Executors.newCachedThreadPool();
		final int numThreads = 40;
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

}
