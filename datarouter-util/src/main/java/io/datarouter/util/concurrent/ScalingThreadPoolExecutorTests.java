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
package io.datarouter.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ScalingThreadPoolExecutorTests{

	private static final int MAX_THREADS = 5;

	@Test
	public void test(){
		ThreadFactory threadFactory = new NamedThreadFactory(null, ScalingThreadPoolExecutorTests.class.getSimpleName(),
				false);
		ThreadPoolExecutor executor = new ScalingThreadPoolExecutor(0, MAX_THREADS, 0, TimeUnit.SECONDS, threadFactory);
		Phaser phaser = new Phaser(1);

		List<Future<?>> futures = new ArrayList<>();
		for(int i = 0; i < MAX_THREADS; i++){
			Assert.assertEquals(executor.getActiveCount(), i);
			Assert.assertEquals(executor.getPoolSize(), i);
			Assert.assertEquals(executor.getQueue().size(), 0);
			phaser.register();
			futures.add(executor.submit(new WaitRunnable(phaser)));
			phaser.arriveAndAwaitAdvance();
		}

		Assert.assertEquals(executor.getActiveCount(), MAX_THREADS);
		Assert.assertEquals(executor.getPoolSize(), MAX_THREADS);
		Assert.assertEquals(executor.getQueue().size(), 0);

		futures.add(executor.submit(new WaitRunnable(phaser)));
		Assert.assertEquals(executor.getActiveCount(), MAX_THREADS);
		Assert.assertEquals(executor.getPoolSize(), MAX_THREADS);
		Assert.assertEquals(executor.getQueue().size(), 1);

		futures.add(executor.submit(new WaitRunnable(phaser)));
		Assert.assertEquals(executor.getActiveCount(), MAX_THREADS);
		Assert.assertEquals(executor.getPoolSize(), MAX_THREADS);
		Assert.assertEquals(executor.getQueue().size(), 2);

		phaser.arrive();
		FutureTool.getAllVaried(futures);

		Assert.assertEquals(executor.getActiveCount(), 0);
		Assert.assertEquals(executor.getCompletedTaskCount(), MAX_THREADS + 2);
		Assert.assertEquals(executor.getQueue().size(), 0);

		executor.shutdownNow();
	}

	private class WaitRunnable implements Runnable{

		private Phaser phaser;

		public WaitRunnable(Phaser phaser){
			this.phaser = phaser;
		}

		@Override
		public void run(){
			if(phaser.getPhase() <= MAX_THREADS){
				phaser.arriveAndDeregister();
			}
			while(phaser.getPhase() <= MAX_THREADS){
				phaser.awaitAdvance(phaser.getPhase());
			}
		}
	}

}
