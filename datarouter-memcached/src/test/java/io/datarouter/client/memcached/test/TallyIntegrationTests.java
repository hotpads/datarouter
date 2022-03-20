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
package io.datarouter.client.memcached.test;

import java.time.Duration;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.memcached.DatarouterMemcachedTestNgModuleFactory;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.tally.Tally;

@Guice(moduleFactory = DatarouterMemcachedTestNgModuleFactory.class)
public class TallyIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterTallyTestDao dao;

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testIncrement(){
		String key = "testIncrement";
		var bean = new Tally(key, null);
		dao.deleteTally(key);
		Assert.assertFalse(dao.findTallyCount(key).isPresent());
		dao.incrementAndGetCount(key, 5);
		Assert.assertTrue(dao.findTallyCount(key).isPresent());
		Assert.assertEquals(dao.getTallyCount(key).longValue(), 5);
		dao.incrementAndGetCount(bean.getKey().getId(), 100);
		Assert.assertEquals(dao.getTallyCount(key).longValue(), 105);
		dao.deleteTally(key);
	}

	@Test
	public void testDelete(){
		String key = "testDelete";
		dao.incrementAndGetCount(key, 2);
		Assert.assertTrue(dao.findTallyCount(key).isPresent());
		dao.deleteTally(key);
		Assert.assertFalse(dao.findTallyCount(key).isPresent());
	}

	@Test
	public void testIncrementWihoutPut(){
		String key = "testIncrementWihoutPut";
		dao.deleteTally(key);
		dao.incrementAndGetCount(key, 5);
		Assert.assertEquals(dao.getTallyCount(key), Long.valueOf(5));
		Assert.assertEquals(dao.getTallyCount(key), Long.valueOf(5));
		dao.incrementAndGetCount(key, 5);
		Assert.assertEquals(dao.getTallyCount(key), Long.valueOf(10));
		dao.deleteTally(key);
	}

	@Test
	public void testNullKeys(){
		String key = null;
		// throws RuntimeException
		Assert.assertThrows(RuntimeException.class, () -> dao.findTallyCount(key).isEmpty());
		Assert.assertThrows(RuntimeException.class, () -> dao.deleteTally(key));
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
	public void testLongKey(){
		String key = "a".repeat(200);
		long newCount = dao.incrementAndGetCount(key, 3);
		//should see a logger.warn
		Assert.assertEquals(newCount, 0);//zero because we couldn't increment it
	}

}
