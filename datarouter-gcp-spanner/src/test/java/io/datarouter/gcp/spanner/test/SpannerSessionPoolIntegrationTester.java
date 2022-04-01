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
package io.datarouter.gcp.spanner.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.cloud.spanner.ErrorCode;
import com.google.cloud.spanner.SpannerException;

import io.datarouter.gcp.spanner.SpannerTestNgModuleFactory;
import io.datarouter.gcp.spanner.client.SpannerClientOptions;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.util.Count.Counts;

@Guice(moduleFactory = SpannerTestNgModuleFactory.class)
public class SpannerSessionPoolIntegrationTester{
	private static final Logger logger = LoggerFactory.getLogger(SpannerSessionPoolIntegrationTester.class);

	private final SortedMapStorageNode<
			TestDatabeanKey,
			TestDatabean,
			TestDatabeanFielder> node;
	private final ExecutorService scannerExec;
	private final ExecutorService opExec;
	private final int maxSessions;

	@Inject
	public SpannerSessionPoolIntegrationTester(NodeFactory nodeFactory, SpannerClientOptions options){
		ClientId clientId = SpannerTestCliendIds.SPANNER;
		node = nodeFactory.create(
				clientId,
				TestDatabean::new,
				TestDatabeanFielder::new)
				.withTableName("SessionPoolTest")
				.buildAndRegister();
		scannerExec = Executors.newFixedThreadPool(100);
		opExec = Executors.newFixedThreadPool(100);
		maxSessions = options.maxSessions(clientId.getName());
	}

	@AfterClass
	public void afterClass(){
		scannerExec.shutdown();
		opExec.shutdown();
	}

	@Test
	public void seedData(){
		var counts = new Counts();
		var count = counts.add("put");
		Scanner.iterate(0, i -> i + 1)
				.limit(10_000)
				.map(i -> new TestDatabean(Integer.toString(i), "hello-" + i, "world-" + i))
				.batch(1_000)
				.each(node::putMulti)
				.each(count::incrementBySize)
				.forEach(batch -> logger.warn("{}", counts));
	}

	@Test
	public void scanWithInterrupts(){
		node.scan().findFirst();//init client in parent thread without timeout

		var counts = new Counts();
		var callCount = counts.add("call");
		var cancelledCount = counts.add("cancelled");
		var resourceExhaustedCount = counts.add("resourceExhausted");
		var otherErrorCount = counts.add("otherError");
		var successCount = counts.add("success");

		int numIterations = maxSessions + 2;
		int numThreads = 1;
		boolean paralleScan = false;
		var config = new Config().setResponseBatchSize(2_000);
		int timeoutMs = 300;
		boolean cancelFutures = true;
		boolean mayInterruptIfRunning = true;
		int logEveryN = 20;

		Scanner.iterate(0, i -> i + 1)
				.limit(numIterations)
				.parallel(new ParallelScannerContext(scannerExec, numThreads, false, paralleScan))
				.each(i -> {
					var future = opExec.submit(() -> {
						try{
							callCount.increment();
							node.scan(config).count();
							successCount.increment();
							return null;
						}catch(SpannerException spannerException){
							if(spannerException.getErrorCode().equals(ErrorCode.CANCELLED)){
								cancelledCount.increment();
							}else if(spannerException.getErrorCode().equals(ErrorCode.RESOURCE_EXHAUSTED)){
								resourceExhaustedCount.increment();
							}else{
								otherErrorCount.increment();
							}
							logger.info("spannerException errorCode={} {}", spannerException.getErrorCode(), counts);
							logger.warn("spannerException errorCode={} {}",
									spannerException.getErrorCode(),
									counts,
									spannerException);
							throw spannerException;
						}
					});

					try{
						future.get(timeoutMs, TimeUnit.MILLISECONDS);
					}catch(ExecutionException e){
						logger.warn("", e);
					}catch(TimeoutException e){
						logger.warn("", e);
						if(cancelFutures){
							future.cancel(mayInterruptIfRunning);
						}
					}catch(InterruptedException e){
						logger.warn("", e);
					}
				})
				.sample(logEveryN, true)
				.forEach($ -> logger.warn("{}", counts));

		Assert.assertEquals(resourceExhaustedCount.value(), 0);
	}

}
