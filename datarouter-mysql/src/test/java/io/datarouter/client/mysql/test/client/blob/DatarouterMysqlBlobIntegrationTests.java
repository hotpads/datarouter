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
package io.datarouter.client.mysql.test.client.blob;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.test.blob.BaseBlobIntegrationTests;
import io.datarouter.storage.test.blob.DatarouterBlobTestDao;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class DatarouterMysqlBlobIntegrationTests extends BaseBlobIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterMysqlTestClientids.MYSQL);
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
