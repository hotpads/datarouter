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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.redis.DatarouterRedisTestNgModuleFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterRedisTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class RedisBlobNodeIntegrationTests{

	private static final String
			CONTENT = "testRedisBlob",
			READ_WRITE_KEY = "testReadWrite",
			WRITE_SCANNER_OF_BYTES_KEY = "testWriteScannerOfBytes",
			LENGTH_KEY = "testLength",
			DELETE_KEY = "testDelete";

	@Inject
	private Datarouter datarouter;
	@Inject
	private RedisBlobTestDao redisDao;

	@AfterClass
	public void afterClass(){
		Scanner.of(READ_WRITE_KEY, WRITE_SCANNER_OF_BYTES_KEY, LENGTH_KEY, DELETE_KEY)
				.forEach(redisDao::delete);
		datarouter.shutdown();
	}

	@Test
	public void testReadWrite(){
		redisDao.write(READ_WRITE_KEY, CONTENT);
		Assert.assertEquals(CONTENT, redisDao.read(READ_WRITE_KEY));
	}

	@Test
	public void testWriteScannerOfBytes(){
		redisDao.writeScannerOfBytes(WRITE_SCANNER_OF_BYTES_KEY, Scanner.of("hello", "datarouter", "byebye")
				.map(String::getBytes));
		Assert.assertEquals("hellodatarouterbyebye", redisDao.read(WRITE_SCANNER_OF_BYTES_KEY));
	}

	@Test
	public void testLength(){
		redisDao.write(LENGTH_KEY, CONTENT);
		Assert.assertEquals(Long.valueOf(CONTENT.length()), redisDao.length(LENGTH_KEY));
	}

	@Test
	public void testDelete(){
		redisDao.write(DELETE_KEY, CONTENT);
		redisDao.delete(DELETE_KEY);
		Assert.assertFalse(redisDao.exists(DELETE_KEY));
	}

}
