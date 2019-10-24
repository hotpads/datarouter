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
package io.datarouter.httpclient.circuitbreaker;

import java.time.Clock;
import java.time.Duration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CallResultQueueTests{

	@Test
	public void testCircularInsert(){
		CallResultQueue callResultQueue = new CallResultQueue(Clock.systemUTC(), 3);
		callResultQueue.insertTrueResult();
		callResultQueue.insertTrueResult();
		callResultQueue.insertTrueResult();
		callResultQueue.insertFalseResultWithException(null);
		Assert.assertFalse(callResultQueue.getResults()[0]);
	}

	@Test
	public void testGetFailurePercentage(){
		CallResultQueue callResultQueue = new CallResultQueue(Clock.systemUTC(), 4);
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 0F);
		callResultQueue.insertFalseResultWithException(null);
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 25F);
		callResultQueue.insertFalseResultWithException(null);
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 50F);
		callResultQueue.insertFalseResultWithException(null);
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 75F);
		callResultQueue.insertFalseResultWithException(null);
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 100F);
		callResultQueue.insertTrueResult();
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 75F);
		callResultQueue.insertTrueResult();
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 50F);
		callResultQueue.insertTrueResult();
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 25F);
		callResultQueue.insertTrueResult();
		Assert.assertEquals(callResultQueue.getFailurePercentage(), 0F);
	}

	@Test
	public void testGetLastFailure(){
		CallResultQueue callResultQueue = new CallResultQueue(Clock.systemUTC(), 3);
		callResultQueue.insertTrueResult();
		Assert.assertFalse(callResultQueue.lastFailureEpochMillis.isPresent());

		callResultQueue = new CallResultQueue(Clock.systemUTC(), 3);
		callResultQueue.insertFalseResultWithException(null);
		Assert.assertTrue(callResultQueue.lastFailureEpochMillis.isPresent());
		Long lastFailure = callResultQueue.lastFailureEpochMillis.get();
		callResultQueue.insertTrueResult();
		callResultQueue.setClock(Clock.offset(Clock.systemUTC(), Duration.ofMillis(1)));
		Assert.assertEquals(callResultQueue.lastFailureEpochMillis.get(), lastFailure);
		callResultQueue.setClock(Clock.offset(Clock.systemUTC(), Duration.ofMillis(1)));
		callResultQueue.insertFalseResultWithException(null);
		Assert.assertNotEquals(callResultQueue.lastFailureEpochMillis.get(), lastFailure);
	}

}
