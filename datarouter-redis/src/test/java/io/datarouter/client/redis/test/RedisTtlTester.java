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
package io.datarouter.client.redis.test;

import java.time.Duration;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.redis.DatarouterRedisTestNgModuleFactory;
import io.datarouter.storage.Datarouter;

// Difficult to test TTLs in maven
@Guice(moduleFactory = DatarouterRedisTestNgModuleFactory.class)
public class RedisTtlTester{

	@Inject
	private Datarouter datarouter;
	@Inject
	private RedisTestDao dao;

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testTtl(){
		String id = "testTtl";
		dao.increment(id, 1, Duration.ofSeconds(2));

		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertFalse(dao.findTallyCount(id).isPresent());
		dao.deleteTally(id);
	}

	@Test
	public void testTtlUpdate(){
		String id = "testTtlUpdate";
		dao.deleteTally(id);

		// Multiple increments does not modify the original TTL
		// This bean's TTL stays at 2 seconds
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1, Duration.ofSeconds(2));

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertFalse(dao.findTallyCount(id).isPresent());
		dao.deleteTally(id);
	}

	@Test
	public void testTtlAdvance(){
		String id = "testTtlAdvance";
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1, Duration.ofSeconds(2));
		dao.increment(id, 1);
		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertFalse(dao.findTallyCount(id).isPresent());
		dao.deleteTally(id);
	}

}
