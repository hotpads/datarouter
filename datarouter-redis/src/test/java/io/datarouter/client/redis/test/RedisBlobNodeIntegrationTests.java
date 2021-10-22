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

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.redis.DatarouterRedisTestNgModuleFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;

@Guice(moduleFactory = DatarouterRedisTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class RedisBlobNodeIntegrationTests{

	private static final String CONTENT = "testRedisBlob";
	private static final String TEST_LOCATION1 = "testLocation1";
	private static final String TEST_LOCATION2 = "testLocation2";
	private static final String TEST_LOCATION3 = "testLocation3";

	@Inject
	private Datarouter datarouter;
	@Inject
	private RedisBlobTestDao redisDao;

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testWrite(){
		redisDao.write(TEST_LOCATION1, CONTENT);
		Assert.assertEquals(CONTENT, redisDao.read(TEST_LOCATION1));
	}

	@Test
	public void testWriteScannerOfBytes(){
		redisDao.writeScannerOfBytes(TEST_LOCATION3, Scanner.of("hello", "datarouter",
				"byebye").map(String::getBytes));
		Assert.assertEquals("hellodatarouterbyebye", redisDao.read(TEST_LOCATION3));
	}

	@Test
	public void testRead(){
		Assert.assertEquals(CONTENT, redisDao.read(TEST_LOCATION1));
	}

	@Test
	public void testLength(){
		Assert.assertEquals(Long.valueOf(CONTENT.length()), redisDao.length(TEST_LOCATION1));
	}

	@Test
	public void testDelete(){
		redisDao.write(TEST_LOCATION2, CONTENT);
		redisDao.delete(TEST_LOCATION2);
		Assert.assertEquals(false, redisDao.exists(TEST_LOCATION2));
	}

}
