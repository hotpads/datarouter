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
package io.datarouter.client.mysql.test.client.imp.mysql.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.client.mysql.test.client.insert.DatarouterPutOpTestDao;
import io.datarouter.client.mysql.test.client.insert.PutOpTestBean;
import io.datarouter.client.mysql.test.client.insert.PutOpTestBeanKey;
import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.util.tuple.Pair;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class PutOpIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterPutOpTestDao dao;

	@BeforeClass
	public void beforeClass(){
		resetTable();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	private void resetTable(){
		dao.deleteAll();
	}

	@Test
	public void testDefault(){
		Pair<String,String> result = test("testDefault", new Config());
		Assert.assertEquals(result.getLeft(), "baz");
		Assert.assertEquals(result.getRight(), "qux");
	}

	@Test
	public void testInsertIgnore(){
		Pair<String,String> result = test("testInsertIgnore", new Config().setPutMethod(PutMethod.INSERT_IGNORE));
		Assert.assertEquals(result.getLeft(), "baz");
		Assert.assertEquals(result.getRight(), "baz");
	}

	@Test
	public void testInsertOnDuplicateUpdate(){
		var config = new Config().setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE);
		Pair<String,String> result = test("testInsertOnDuplicateUpdate", config);
		Assert.assertEquals(result.getLeft(), "baz");
		Assert.assertEquals(result.getRight(), "qux");
	}

	@Test
	public void testInsertOrBust(){
		var config = new Config().setPutMethod(PutMethod.INSERT_OR_BUST);
		Pair<String,String> result = test("testInsertOrBust", config, false, true);
		Assert.assertEquals(result.getLeft(), "baz");
		Assert.assertEquals(result.getRight(), "baz");
	}

	@Test
	public void testInsertOrUpdate(){
		Pair<String,String> result = test("testInsertOrUpdate", new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		Assert.assertEquals(result.getLeft(), "baz");
		Assert.assertEquals(result.getRight(), "qux");
	}

	@Test
	public void testUpdateOrInsert(){
		Pair<String,String> result = test("testUpdateOrInsert", new Config().setPutMethod(PutMethod.UPDATE_OR_INSERT));
		Assert.assertEquals(result.getLeft(), "baz");
		Assert.assertEquals(result.getRight(), "qux");
	}

	@Test
	public void testUpdateOrBust(){
		var config = new Config().setPutMethod(PutMethod.UPDATE_OR_BUST);
		Pair<String,String> result = test("testUpdateOrBust", config, true, true);
		Assert.assertNull(result.getLeft());
		Assert.assertNull(result.getRight());
	}

	@Test
	public void testMerge(){
		Pair<String,String> result = test("testMerge", new Config().setPutMethod(PutMethod.MERGE));
		Assert.assertEquals(result.getLeft(), "baz");
		Assert.assertEquals(result.getRight(), "qux");
	}

	private Pair<String,String> test(String testName, Config config){
		return test(testName, config, false, false);
	}

	private Pair<String,String> test(
			String testName,
			Config config,
			boolean expectedFirstCaught,
			boolean expectedSecondCaught){
		var bean = new PutOpTestBean(testName, "bar", "baz");
		var bean2 = new PutOpTestBean(testName, "bar", "qux");
		try{
			dao.put(bean, config);
			Assert.assertFalse(expectedFirstCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedFirstCaught);
		}
		String before = nullSafeGetC(dao.get(new PutOpTestBeanKey(testName, "bar")));
		try{
			dao.put(bean2, config);
			Assert.assertFalse(expectedSecondCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedSecondCaught);
		}
		String after = nullSafeGetC(dao.get(new PutOpTestBeanKey(testName, "bar")));

		return new Pair<>(before, after);
	}

	private String nullSafeGetC(PutOpTestBean bean){
		if(bean == null){
			return null;
		}
		return bean.getC();
	}

	@Test
	public void testMultiInsert(){
		int testBatchSize = 10;
		int totalCount = (int) (testBatchSize * 2.5);
		var config = new Config()
				.setInputBatchSize(testBatchSize)
				.setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE);
		List<PutOpTestBean> databeans = new ArrayList<>();
		for(int count = 0; count < totalCount; count++){
			databeans.add(new PutOpTestBean("testMultiInsert", randomString(), randomString()));
		}
		dao.putMulti(databeans, config);
		Assert.assertEquals(Scanner.of(databeans).map(Databean::getKey).listTo(dao::getMulti).size(), totalCount);
	}

	private static final String randomString(){
		return UUID.randomUUID().toString();
	}

}
