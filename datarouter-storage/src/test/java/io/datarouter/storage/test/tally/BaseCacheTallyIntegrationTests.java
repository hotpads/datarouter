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
package io.datarouter.storage.test.tally;

import java.time.Duration;

import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class BaseCacheTallyIntegrationTests extends BaseTallyIntegrationTests{

	@Test
	public void testTtlUpdate(){
		String key = "testTtlUpdate";
		dao.deleteTally(key);

		// Multiple increments does not modify the original TTL
		// This bean's TTL stays at 2 seconds
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertFalse(dao.findTallyCount(key).isPresent());
	}

	@Test
	public void testTtlAdvance(){
		String key = "testTtlAdvance";
		dao.deleteTally(key);

		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));
		dao.incrementAndGetCount(key, 1);

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertFalse(dao.findTallyCount(key).isPresent());
	}

	@Test
	public void testTtl(){
		String key = "testTtl";
		dao.deleteTally(key);

		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertFalse(dao.findTallyCount(key).isPresent());
	}

	@Test
	public void testLongKey(){
		String key = "a".repeat(200);
		long newCount = dao.incrementAndGetCount(key, 3);
		//should see a logger.warn
		Assert.assertEquals(newCount, 0);//zero because we couldn't increment it
	}

}
