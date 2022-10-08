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
package io.datarouter.storage.test.blob;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.node.factory.BlobNodeFactory;
import io.datarouter.storage.util.Subpath;

public abstract class BaseBlobIntegrationTests{

	private static final String
			CONTENT = "testBlob",
			READ_WRITE_KEY = "testReadWrite",
			WRITE_SCANNER_OF_BYTES_KEY = "testWriteScannerOfBytes",
			LENGTH_KEY = "testLength",
			DELETE_KEY = "testDelete";

	@Inject
	private Datarouter datarouter;
	@Inject
	private BlobNodeFactory nodeFactory;

	protected DatarouterBlobTestDao dao;

	protected void setup(ClientId clientId){
		this.dao = new DatarouterBlobTestDao(datarouter, nodeFactory, clientId);
	}

	@AfterClass
	public void afterClass(){
		Scanner.of(READ_WRITE_KEY, WRITE_SCANNER_OF_BYTES_KEY, LENGTH_KEY, DELETE_KEY)
				.forEach(dao::delete);
		datarouter.shutdown();
	}

	@Test
	public void testReadWrite(){
		dao.write(READ_WRITE_KEY, CONTENT);
		Assert.assertEquals(CONTENT, dao.read(READ_WRITE_KEY));
	}

	@Test
	public void testLength(){
		dao.write(LENGTH_KEY, CONTENT);
		Assert.assertEquals(Long.valueOf(CONTENT.length()), dao.length(LENGTH_KEY));
	}

	@Test
	public void testDelete(){
		dao.write(DELETE_KEY, CONTENT);
		dao.delete(DELETE_KEY);
		Assert.assertEquals(false, dao.exists(DELETE_KEY));
	}

	@Test
	public void testReadTtl(){
		var config = new Config().setTtl(Duration.ofSeconds(2));
		dao.write(DELETE_KEY, CONTENT, config);
		try{
			Thread.sleep(Duration.ofSeconds(3).toMillis());
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		boolean exists = dao.exists(DELETE_KEY);
		dao.delete(DELETE_KEY);
		Assert.assertFalse(exists);
	}

	@Test
	public void scan(){
		List<String> keys = new ArrayList<>();
		for(int i = 0; i < 10; i++){
			String key = READ_WRITE_KEY + "/" + i;
			keys.add(key);
			dao.write(key, CONTENT);
		}
		var subpath = Subpath.join(List.of(DatarouterBlobTestDao.SUBPATH, new Subpath(READ_WRITE_KEY)));
		int size = dao.scan(subpath)
				.concatIter(Function.identity())
				.list()
				.size();
		Assert.assertEquals(size, 10);
		Scanner.of(keys)
				.forEach(dao::delete);
	}

	@Test
	public void scanKeys(){
		Set<String> keys = new HashSet<>();
		for(int i = 0; i < 10; i++){
			String key = READ_WRITE_KEY + "/" + i;
			keys.add(key);
			dao.write(key, CONTENT);
		}
		var subpath = Subpath.join(List.of(DatarouterBlobTestDao.SUBPATH, new Subpath(READ_WRITE_KEY)));
		int size = dao.scanKeys(subpath)
				.concatIter(Function.identity())
				.list()
				.size();
		Assert.assertEquals(size, 10);
		Scanner.of(keys)
				.forEach(dao::delete);
	}

	@Test
	public void vacuum(){
		Set<String> keys = new HashSet<>();
		for(int i = 0; i < 10; i++){
			String key = "" + i;
			keys.add(key);
			dao.write(key,
					key,
					new Config().setTtl(Duration.of(1000, ChronoUnit.MILLIS)));
		}
		dao.write("11", "11", new Config().setTtl(Duration.of(10, ChronoUnit.MINUTES)));
		try{
			Thread.sleep(2000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		dao.vacuum();
		List<List<Pathbean>> beans = dao.scan(DatarouterBlobTestDao.SUBPATH)
				.list();
		Assert.assertTrue(beans.size() >= 1);
		Scanner.of(beans)
				.concat(Scanner::of)
				.forEach(item -> Assert.assertFalse(keys.contains(item.getKey().getFile())));
		dao.delete("11");
	}

}
