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

	// Runs on local, but throws error on buildserver
	@Test(enabled = false)
	public void testIncrement(){
		String key = "testKey1";
		Tally bean = new Tally(key, null);
		dao.put(bean);

		int count = 5;
		dao.incrementAndGetCount(bean.getKey().getId(), count);
		Assert.assertFalse(dao.exists(key));

		count += 100;
		dao.incrementAndGetCount(bean.getKey().getId(), count);
		Assert.assertFalse(dao.exists(key));

		dao.delete(key);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testDelete(){
		String key = "testKey2";
		Tally bean = new Tally(key, null);
		dao.put(bean);

		dao.delete(key);
		Tally roundTripped = dao.get(bean.getKey());
		Assert.assertNull(roundTripped);

		// Throws NullPointerException since databean has been deleted from Memcached
		dao.incrementAndGetCount(roundTripped.getKey().getId(), 10);

		dao.delete(key);
	}

	@Test
	public void testIncrementWihoutPut(){
		String key = "testKey3";
		dao.delete(key);

		dao.incrementAndGetCount(key, 5);

		// if assert error occurs, delete key then rerun test
		Assert.assertEquals(dao.getTallyCount(key), Long.valueOf(5));
		Assert.assertEquals(dao.getTallyCount(key), Long.valueOf(5));
		dao.incrementAndGetCount(key, 5);
		Assert.assertEquals(dao.getTallyCount(key), Long.valueOf(10));

		dao.delete(key);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testGetTallyCountOnNull(){
		String key = null;
		// throws RuntimeException
		Assert.assertTrue(dao.findTallyCount(key).isEmpty());
		dao.delete(key);
	}

	@Test
	public void testTtl(){
		String key = "testKey4";
		dao.delete(key);

		dao.incrementAndGetCount(key, 1, Duration.ofSeconds(2));

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertFalse(dao.exists(key));
	}

	@Test
	public void testTtlUpdate(){
		String key = "testKey5";
		dao.delete(key);

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
		Assert.assertFalse(dao.exists(key));
	}

	@Test
	public void testTtlAdvance(){
		String key = "testKey6";
		dao.delete(key);

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
		Assert.assertFalse(dao.exists(key));
	}

}
