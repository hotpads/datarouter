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
package io.datarouter.ratelimiter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

// slow
public class SimpleRateLimiterTests{

	@Test
	public void testNext(){
		long limit = 20L;
		SimpleRateLimiter rateLimiter = new SimpleRateLimiter(limit);
		long next = rateLimiter.nextAllowed;
		rateLimiter.next();
		Assert.assertTrue(next < rateLimiter.nextAllowed);
	}

	@Test
	public void testIsAllowed(){
		long limit = 24 * 60 * 60 * 1000;
		SimpleRateLimiter rateLimiter = new SimpleRateLimiter(limit);
		Assert.assertTrue(rateLimiter.isAllowed(false));
		Assert.assertTrue(rateLimiter.isAllowed(false));
		rateLimiter.next();
		Assert.assertFalse(rateLimiter.isAllowed(false));
		rateLimiter.customDelay(0L);
		Assert.assertTrue(rateLimiter.isAllowed(false));
	}

	@Test
	public void testWaitForPermission(){
		long limit = 400L;
		SimpleRateLimiter rateLimiter = new SimpleRateLimiter(limit);
		Assert.assertTrue(rateLimiter.isAllowed(false));
		rateLimiter.next();
		Assert.assertFalse(rateLimiter.isAllowed(false));
		rateLimiter.waitForPermission(true, false);
		Assert.assertTrue(rateLimiter.isAllowed(false));
		rateLimiter.next();
		rateLimiter.waitForPermission(true, false);
		Assert.assertTrue(rateLimiter.isAllowed(false));

		rateLimiter = new SimpleRateLimiter(0L);
		Assert.assertTrue(rateLimiter.isAllowed(false));
		rateLimiter.next();
		Assert.assertTrue(rateLimiter.isAllowed(false));
	}

	@Test
	public void testMutliThreaded() throws Exception{
		long limit = 20L;
		int numThreads = 7;
		int numTimeIntervals = 42;
		// Spawn a few threads that share a SimpleRate limiter and increment
		// a count as they are granted permission. Count should match
		// interval/limit

		ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

		List<Callable<Integer>> counterCallables = new LinkedList<>();

		final SimpleRateLimiter rateLimiter = new SimpleRateLimiter(limit);
		final long endTime = System.currentTimeMillis() + (limit * numTimeIntervals);

		for(int i = 0; i < numThreads; i++){
			counterCallables.add(new Callable<>(){
				private int integer = 0;

				@Override
				public Integer call(){
					while(endTime > System.currentTimeMillis()){
						rateLimiter.waitForPermission(false, true);
						if(endTime <= System.currentTimeMillis()){
							break;
						}
						integer++;
					}
					return integer;
				}
			});
		}

		int total = 0;
		for(Future<Integer> f : executor.invokeAll(counterCallables)){
			total += f.get();
		}

		/* The expected result if instructions were always instantaneous is one increment per time interval regardless
		 * of number of threads. Increasing the limit will make total==numTimeIntervals, but also make the test run more
		 * slowly. If the rate limiter were allowing multiple threads to activate and increment incorrectly, total would
		 * be much larger than numTimeIntervals. */
		Assert.assertTrue(numTimeIntervals >= total, "total was " + total);
		executor.shutdownNow();
	}

}
